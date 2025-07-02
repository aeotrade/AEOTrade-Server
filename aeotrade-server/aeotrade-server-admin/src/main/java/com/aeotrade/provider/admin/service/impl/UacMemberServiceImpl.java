package com.aeotrade.provider.admin.service.impl;


import com.aeotrade.base.constant.BizConstant;
import com.aeotrade.configure.HmtxUserInfoTokenServices;
import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.admin.entiy.*;
import com.aeotrade.provider.admin.event.AddMemberEvent;
import com.aeotrade.provider.admin.feign.MamberFeign;
import com.aeotrade.provider.admin.mapper.UacMemberMapper;
import com.aeotrade.provider.admin.service.UacMemberService;
import com.aeotrade.provider.admin.service.UacStaffService;
import com.aeotrade.provider.admin.service.UawVipTypeService;
import com.aeotrade.provider.admin.uacVo.*;
import com.aeotrade.service.MqSend;
import com.aeotrade.suppot.PageList;
import com.aeotrade.suppot.RespResult;
import com.aeotrade.utlis.HttpRequestUtils;
import com.aeotrade.utlis.ThreadPoolUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.toolkit.MPJWrappers;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <p>
 * 企业店铺 服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-07
 */
@Service
public class UacMemberServiceImpl extends MPJBaseServiceImpl<UacMemberMapper, UacMember> implements UacMemberService {
    private static final String defaultImg = "https://www.aeotrade.com/aeoapi/img/oss/aeotrade-launch-advisor/f2abd58617f8974c19e58f20434797b7d2c427b8e907281bab4a07fa78b04d13.png";
    @Value("${hmtx.chain.id:aeotradechain}")
    private String chainId;
    @Autowired
    private UacUserConnectionServiceImpl uacUserConnectionMapper;
    @Autowired
    private UacStaffServiceImpl uacStaffMapper;
    @Autowired
    private UacMemberStaffServiceImpl uacMemberStaffMapper;
    @Autowired
    private MamberFeign mamberFeign;
    @Autowired
    private UacMemberSynchronizationServiceImpl uacMemberSynchronizationMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private AtciArchiveBusinessMemberLabelServiceImpl atciArchiveBusinessMemberLabelService;
    @Autowired
    private UawAptitudesServiceImpl uawAptitudesService;
    @Autowired
    private UawWorkbenchServiceImpl uawWorkbenchService;
    @Autowired
    private UawVipTypeService uawVipTypeService;
    @Autowired
    private UacAdminServiceImpl uacAdminService;
    @Autowired
    private UacAdminRoleServiceImpl uacAdminRoleService;
    @Autowired
    private UawVipMessageServiceImpl uawVipMessageService;
    @Autowired
    private HmtxUserInfoTokenServices hmtxUserInfoTokenServices;

    @Value("${hmtx.tijianzhinang.url:}")
    private String url;
    @Value("${hmtx.mamUri}")
    private String mamUri;
    @Autowired
    private MqSend mqSend;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private UacMemberService uacMemberService;
    @Autowired
    private UacStaffService uacStaffService;

    /**
     * 根据主键 [id] 获取一条记录(企业表)
     *
     * @param id 主键 ID
     * @return 返回主键对应的对象
     */
    public UacMember get(Long id) {
        return this.getById(id);
    }


    /**
     * 根据企业id查询企业列表
     *
     * @return
     */
    public PageList<MemberVO> findMemberList(Long staffId, Integer pageSize, Integer pageNo, Integer isAtcl, Long vipTypeId) throws RuntimeException {


        /**1.根据企业id查询员工表id*/
        UacStaff uacStaff = uacStaffMapper.getById(staffId);
        if (uacStaff == null || uacStaff.getId() == null) {
            throw new AeotradeException("企业未与微信绑定");
        }
        /**2.根据员工id查询关联表的企业*/
        LambdaQueryWrapper<UacMemberStaff> uacMemberStaffLambdaQueryWrapper = new LambdaQueryWrapper<>();
        uacMemberStaffLambdaQueryWrapper.eq(UacMemberStaff::getStaffId, uacStaff.getId());
        uacMemberStaffLambdaQueryWrapper.isNotNull(UacMemberStaff::getMemberId);
        uacMemberStaffLambdaQueryWrapper.isNotNull(UacMemberStaff::getStaffId);
        Page<UacMemberStaff> list = uacMemberStaffMapper.page(new Page<>(pageNo, pageSize), uacMemberStaffLambdaQueryWrapper);
        if (list.getTotal() == 0) {
            /*2.1直接查询企业表*/
            UacMember uacMember = this.getById(uacStaff.getMemberId());
            if (uacMember != null) {
                PageList<MemberVO> voList = new PageList<>();
                MemberVO vo = new MemberVO();
                BeanUtils.copyProperties(uacMember, vo);
                vo.setKindId(uacMember.getKindId());
                voList.add(vo);
                voList.setTotalSize(list.getTotal());
                if (vipTypeId != null) {
                    vo.setSettledStutas(this.findStatus(uacStaff.getMemberId(), vipTypeId));
                }
                //插入关联表
                UacMemberStaff uacMemberStaff = new UacMemberStaff();
                uacMemberStaff.setStaffId(uacStaff.getId());
                uacMemberStaff.setMemberId(uacStaff.getMemberId());
                uacMemberStaff.setIsAdmin(0);
                uacMemberStaff.setCreatedTime(LocalDateTime.now());
                uacMemberStaffMapper.save(uacMemberStaff);
                return voList;
            }
        } else if (list.getTotal() != 0) {
            /**3.根据企业id查询所有的企业列表*/
            Boolean flag = false;
            PageList<MemberVO> voList = new PageList<>();
            MemberVO vo = null;
            for (UacMemberStaff mstaff : list.getRecords()) {
                if (mstaff.getMemberId() == staffId) {
                    flag = true;
                }
                UacMember uacMember = this.getById(mstaff.getMemberId());
                vo = new MemberVO();
                if (uacMember != null) {
                    BeanUtils.copyProperties(uacMember, vo);
                    if (vipTypeId != null) {
                        vo.setSettledStutas(this.findStatus(uacMember.getId(), vipTypeId));
                    }
                    if (isAtcl != null) {
                        Long rows = atciArchiveBusinessMemberLabelService.lambdaQuery()
                                .eq(AtciArchiveBusinessMemberLabel::getMemberId, vo.getId()).count();
                        if (rows != 0) {
                            voList.add(vo);
                        }
                    } else {
                        voList.add(vo);
                    }
                }
            }
            if (!flag)
                voList.setTotalSize(list.getTotal());

            return voList;
        }
        return null;
    }

    private Integer findStatus(Long memberId, Long vipTypeId) {
        List<UawAptitudes> uawAptitudes = uawAptitudesService.lambdaQuery().eq(UawAptitudes::getStatus, 0)
                .eq(UawAptitudes::getMemberId, memberId).eq(UawAptitudes::getVipTypeId, vipTypeId).list();
        UawAptitudes entity = uawAptitudes.size() > 0 ? uawAptitudes.get(0) : null;
        if (entity == null) {
            return 0;
        }
        return entity.getSgsStatus();
    }

    /**
     * 新增企业
     *
     * @param uacMemberDto
     */
    public UacMember insertMember(UacMemberDto uacMemberDto) throws Exception {
        String s = uacMemberDto.getUscCode().replaceAll(" ", "");
        List<UacMember> uacb = this.lambdaQuery().eq(UacMember::getUscCode, s).eq(UacMember::getStatus, 0).list();
        if (uacb != null && uacb.size() != 0) {
            throw new AeotradeException("该企业已存在，请前去登录");
        }
        UacStaff idByMemberId = uacStaffMapper.getById(uacMemberDto.getId());
        UacMember ber = this.getById(idByMemberId.getMemberId());
        /**1.添加到企业表*/
        UacMember uacMember = new UacMember();
        BeanUtils.copyProperties(uacMemberDto, uacMember);
        if (s.length() != 18) throw new AeotradeException("请输入正确的统一社会信用代码（长度必须为18位）");
        uacMember.setUscCode(s);
        uacMember.setId(null);
        uacMember.setCreatedTime(LocalDateTime.now());
        uacMember.setAtpwStatus(0);
        uacMember.setKindId(1L);
        uacMember.setCreatedBy(idByMemberId.getId());
        uacMember.setStaffId(idByMemberId.getId());
        uacMember.setSgsStatus(0);
        uacMember.setStatus(0);
        uacMember.setRevision(0);
        this.save(uacMember);

        /**2.添加到关系表*/
        if (idByMemberId == null || idByMemberId.getId() == null) {
            throw new AeotradeException("未查到员工表数据");
        }
        UacMemberStaff uacMemberStaff = new UacMemberStaff();
        uacMemberStaff.setMemberId(uacMember.getId());
        uacMemberStaff.setStaffId(idByMemberId.getId());
        uacMemberStaff.setIsAdmin(0);
        uacMemberStaff.setCreatedTime(LocalDateTime.now());
        uacMemberStaffMapper.save(uacMemberStaff);
        UawVipType vipType = uawVipTypeService.getById(uacMemberDto.getVipTypeId());
        UawWorkbench workbench = uawWorkbenchService.getById(vipType.getWorkbench());
        idByMemberId.setLastMemberId(uacMember.getId());
        idByMemberId.setLastWorkbenchId(workbench.getId());
        idByMemberId.setChannelColumnsId(workbench.getChannelColumnsId());
        uacStaffMapper.updateById(idByMemberId);
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                AddMemberEvent addMemberEvent=new AddMemberEvent(this,uacMember,vipType);
                eventPublisher.publishEvent(addMemberEvent);
                try {
                    HashMap<String, String> chain = new HashMap<>();
                    chain.put("tenantId", String.valueOf(uacMember.getId()));
                    chain.put("tenantName", uacMember.getMemberName());
                    chain.put("uscc", uacMember.getUscCode());
                    chain.put("creatTime", uacMember.getCreatedTime().format(DateTimeFormatter.ofPattern("MMddHHmmssSSS")));
                    chain.put("userType", "员工");
                    chain.put("userId", String.valueOf(uacMember.getStaffId()));
                    chain.put("roleCodeRulesEnum", vipType.getRelevancyTypeId());
                    chain.put("chainId", chainId);
                    chain.put("userTypeEnum", "管理员");
                    mqSend.sendChain(JSONObject.toJSONString(chain), "chain");
                    tongbuerp(uacMember);
                } catch (Exception e) {
                    log.warn(e.getMessage());
                }
            }
        });

        return uacMember;
    }

    public Boolean sendChainMessageAddMember(UacStaffDto uacStaffDto) throws Exception {
        UacMember member = uacMemberService.getById(uacStaffDto.getMemberId());
        UacStaff uacStaff = uacStaffService.getById(uacStaffDto.getStaffId());
        Objects.requireNonNull(member, "未查到企业表数据");
        Objects.requireNonNull(uacStaff, "未查到员工表数据");
        List<UawVipType> uawVipTypeList = uawVipTypeService.lambdaQuery().eq(UawVipType::getWorkbench, uacStaff.getLastWorkbenchId()).list();
        if (uawVipTypeList.isEmpty()) {
            throw new AeotradeException("未查到会员类型");
        }
        UawVipType uawVipType = uawVipTypeList.get(0);
        HashMap<String, String> chain = new HashMap<>();
        chain.put("tenantId", String.valueOf(member.getId()));
        chain.put("tenantName", member.getMemberName());
        chain.put("uscc", member.getUscCode());
        chain.put("creatTime", member.getCreatedTime().format(DateTimeFormatter.ofPattern("MMddHHmmssSSS")));
        chain.put("userType", "员工");
        chain.put("userId", String.valueOf(uacStaffDto.getStaffId()));
        chain.put("roleCodeRulesEnum", uawVipType.getRelevancyTypeId());
        chain.put("chainId", chainId);
        chain.put("userTypeEnum", "管理员");
        mqSend.sendChain(JSONObject.toJSONString(chain), "chain");
        return true;
    }

    public void tongbuerp(UacMember uacMember) throws Exception {

        Map<String, Object> map = new HashMap<>();
        map.put("company_name", uacMember.getMemberName());
        map.put("hmm_company_id", uacMember.getId());
        map.put("usc_code", uacMember.getUscCode());
        UacMemberSynchronization uacMemberSynchronization = new UacMemberSynchronization();
        uacMemberSynchronization.setMemberId(uacMember.getId());
        uacMemberSynchronization.setMemberNaem(uacMember.getMemberName());
        uacMemberSynchronization.setMemberUscc(uacMember.getUscCode());
        uacMemberSynchronization.setSynchronousStatus(1);
        if (org.springframework.util.StringUtils.isEmpty(url)){
            return;
        }
        String http = HttpRequestUtils.httpPost(url, map);
        RespResult httprespResult = JSONObject.parseObject(http, RespResult.class);
        uacMemberSynchronization.setReturnResult(httprespResult.toString());
        uacMemberSynchronizationMapper.save(uacMemberSynchronization);
        if (httprespResult.getCode() != 200) {
            uacMemberSynchronization.setSynchronousStatus(0);
            uacMemberSynchronizationMapper.updateById(uacMemberSynchronization);
        }

    }

    /**
     * 根据id删除企业
     *
     * @param memberId
     */
    @Transactional
    public void deleteMember(Long memberId) {
        UacMember uacMember = this.getById(memberId);
        if (uacMember.getSgsStatus().equals(BizConstant.MemberStateEum.AUTHENTICATED.getValue())) {
            throw new AeotradeException("已经认证企业不能删除");
        }
        /**1.删除企业表数据*/
        uacMember.setStatus(0);
        this.updateById(uacMember);
        /**2.删除关联表数据*/
        /*2.1查询主键id*/
        List<UacMemberStaff> uacMemberStaffList = uacMemberStaffMapper.lambdaQuery().eq(UacMemberStaff::getMemberId, memberId).list();
        for (UacMemberStaff uac : uacMemberStaffList) {
            if (uac.getId() != null) {
                uacMemberStaffMapper.removeById(uac.getId());
            }
        }


    }

    /**
     * 根据企业id查询企业信息
     *
     * @param memberId
     * @return
     */
    public MemberVO getMember(Long memberId) {
        UacMember uacMember = this.getById(memberId);
        MemberVO vo = new MemberVO();
        UacStaff uacStaff = uacStaffMapper.getById(uacMember.getStaffId());
        BeanUtils.copyProperties(uacMember, vo);
        if (uacStaff != null) {
            vo.setAdminName(uacStaff.getStaffName());
            if (StringUtils.isNotBlank(uacStaff.getTel())) {
                vo.setAdminTel(uacStaff.getTel());
            }
        }
        return vo;
    }

    /**
     * 根据id修改企业信息
     *
     * @param uacMember
     */
    public void updateMember(UacMember uacMember) {
        String s = uacMember.getUscCode().replaceAll(" ", "");
        List<UacMember> uacb = this.lambdaQuery().eq(UacMember::getUscCode, s).eq(UacMember::getStatus, 0).list();
        if (uacb != null && uacb.size() != 0) {
            throw new AeotradeException("社会信用代码已存在,该企业已经注册");
        }
        if (s.length() != 18) throw new AeotradeException("请输入正确的统一社会信用代码（长度必须为18位）");

        uacMember.setUpdatedTime(LocalDateTime.now());
        uacMember.setRevision(1);
        this.updateById(uacMember);
    }

    /**
     * 银行卡认证
     *
     * @param uacMember
     */
    public void bankSgs(UacMember uacMember) throws Exception {

        /**1.添加银行卡信息*/
        uacMember.setRevision(1);
        this.updateById(uacMember);
    }

    public void CompanySgs(UacMember uacMember) {
        uacMember.setRevision(0);
        this.updateById(uacMember);
    }

    /**
     * 金额校验
     *
     * @param bankMoney
     * @param memberId
     * @return
     */
    public Map<String, Object> findBankMoney(Integer bankMoney, Long memberId) throws RuntimeException {
        UacMember uacMember = this.getById(memberId);
        Map<String, Object> map = new HashMap<>();
        if (uacMember != null) {
            /*if(uacMember.getBankStatus()==1){
                throw new AeotradeException("已经认证通过");
            }*/

          /*  if (uacMember.getBankMoney() == bankMoney) {
                //认证成功
               // uacMember.setBankStatus(BizConstant.MemberStateEum.AUTHENTICATED.getValue());
                uacMemberMapper.updateByPrimaryKey(uacMember);
                map.put("bank",0 );
                map.put("uscCode",uacMember.getUscCode());
                return map;
            } else {
                //认证失败
               // uacMember.setBankStatus(BizConstant.MemberStateEum.AUTHENTICATION_FAILED.getValue());
                uacMemberMapper.updateByPrimaryKey(uacMember);
                map.put("bank",1 );
                return map;
            }*/
        }
        return null;
    }

    /**
     * 列出企业的所有员工
     *
     * @return
     */
    public PageList<StaffVO> findStaff(Long memberId, Integer pageSize, Integer pageNo) {
        /**1.根据企业id查询中间表的staffId,*/
        LambdaQueryWrapper<UacMemberStaff> uacMemberStaffLambdaQueryWrapper = new LambdaQueryWrapper<>();
        uacMemberStaffLambdaQueryWrapper.eq(UacMemberStaff::getMemberId, memberId);
        uacMemberStaffLambdaQueryWrapper.isNotNull(UacMemberStaff::getStaffId);
        Page<UacMemberStaff> pageList = uacMemberStaffMapper.page(new Page<>(pageNo, pageSize), uacMemberStaffLambdaQueryWrapper);
        /**2.根据staffId查询员工名称 部门*/
        PageList<StaffVO> list = new PageList<>();
        StaffVO vo = null;

        for (UacMemberStaff uacMemberStaff : pageList.getRecords()) {
            vo = new StaffVO();
            UacStaff uacStaff = uacStaffMapper.getById(uacMemberStaff.getStaffId());
            if (uacStaff != null) {
                BeanUtils.copyProperties(uacStaff, vo);

                /*2.1根据staffId查询中间表企业id*/
                List<UacMemberStaff> staffList = uacMemberStaffMapper.lambdaQuery().eq(UacMemberStaff::getStaffId, uacStaff.getId()).list();
                List memberName = new ArrayList();
                /*2.2根据企业id查询企业名称*/
                for (UacMemberStaff staff : staffList) {
                    if (staff.getMemberId() != null) {
                        UacMember uacMember = this.getById(staff.getMemberId());
                        memberName.add(uacMember.getMemberName());
                    }
                }
                vo.setMemberName(memberName);
                list.add(vo);
            }
        }
        list.setTotalSize(pageList.getTotal());
        return list;

    }


    /**
     * 查询通讯录
     *
     * @return
     */
    public PageList<StaffIDVO> findStaffId(Long staffId, Integer pageSize, Integer pageNo) {

        List<UacMemberStaff> memberList = uacMemberStaffMapper.lambdaQuery().eq(UacMemberStaff::getStaffId, staffId).list();


        List<UacMemberStaff> staffList = uacMemberStaffMapper.lambdaQuery()
                .in(UacMemberStaff::getMemberId, memberList.stream().map(UacMemberStaff::getMemberId).distinct().collect(Collectors.toList())).list();
        Set<Long> longSet = staffList.stream().map(UacMemberStaff::getStaffId).distinct().collect(Collectors.toSet());

        LambdaQueryWrapper<UacStaff> uacStaffLambdaQueryWrapper = new LambdaQueryWrapper<>();
        uacStaffLambdaQueryWrapper.eq(UacStaff::getSgsStatus, BizConstant.StaffBindStateEum.BOUND.getValue());
        uacStaffLambdaQueryWrapper.eq(UacStaff::getStatus, 0);
        uacStaffLambdaQueryWrapper.in(UacStaff::getId, longSet);
        Page<UacStaff> uacStaffPageList = uacStaffMapper.page(new Page<>(pageNo, pageSize), uacStaffLambdaQueryWrapper);

        StaffIDVO staffIDVO;
        List<StaffIDVO> list = new ArrayList<>();
        for (UacStaff staff : uacStaffPageList.getRecords()) {
            staffIDVO = new StaffIDVO();
            BeanUtils.copyProperties(staff, staffIDVO);
            list.add(staffIDVO);
        }

        PageList<StaffIDVO> pageList = new PageList<>();
        pageList.setRecords(list);
        pageList.setTotalSize(uacStaffPageList.getTotal());

        return pageList;
    }

    /**
     * 查询当前企业
     *
     * @param id
     * @return
     */
    public MemberVO querMember(Long id) {
        UacStaff uacStaff = uacStaffMapper.getById(id);
        if (uacStaff != null && uacStaff.getId() != null) {
            UacMember uacMember = this.getById(uacStaff.getId());
            if (uacMember != null) {
                MemberVO vo = new MemberVO();
                BeanUtils.copyProperties(uacMember, vo);
                return vo;
            }
        }
        return null;
    }

    /**
     * 当前登录员工所有企业下的员工
     *
     * @param staffId
     * @return
     */
    public List<StaffVO> findStafList(Long staffId, Integer pageSize, Integer pageNo) {
        /**1.根据员工id查询中间表*/
        List<UacMemberStaff> uacMemberStaff = uacMemberStaffMapper.lambdaQuery().eq(UacMemberStaff::getStaffId, staffId).list();
        List<Long> id = new ArrayList<>();
        /**2.根据企业查询中间表企业下的所有员工id*/
        for (UacMemberStaff memberStaff : uacMemberStaff) {
            if (memberStaff != null && memberStaff.getId() != null) {
                List<UacMemberStaff> uacMemberStaffs = uacMemberStaffMapper.lambdaQuery()
                        .eq(UacMemberStaff::getMemberId, memberStaff.getMemberId()).orderByDesc(UacMemberStaff::getId).list();
                if (uacMemberStaffs.size() != 0) {
                    for (UacMemberStaff uac : uacMemberStaffs) {
                        id.add(uac.getStaffId());
                    }
                }
            }
        }
        List<Long> newList = id.stream().distinct().collect(Collectors.toList());
        List<StaffVO> voList = new ArrayList<>();
        StaffVO vo = null;
        for (Long sId : newList) {
            /**3查询员工信息*/
            UacStaff uacStaff = uacStaffMapper.getById(sId);
            if (uacStaff != null) {
                vo = new StaffVO();
                BeanUtils.copyProperties(uacStaff, vo);
                /*2.1根据staffId查询中间表企业id*/
//                List<UacMemberStaff> staffList = uacMemberStaffMapper.findStaff(sId);
                List<UacMemberStaff> staffList = uacMemberStaffMapper.lambdaQuery().eq(UacMemberStaff::getStaffId, sId).list();
                List memberName = new ArrayList();
                /*2.2根据企业id查询企业名称*/
                for (UacMemberStaff staff : staffList) {
                    if (staff.getMemberId() != null) {
                        UacMember uacMember = this.getById(staff.getMemberId());
                        if (uacMember != null) {
                            memberName.add(uacMember.getMemberName());
                        }
                    }
                }
                vo.setMemberName(memberName);
                voList.add(vo);
            }
        }
        return voList;
    }

    public List<UacMember> findMemberByUscc(String uscc) {
        List<UacMember> uacb = this.lambdaQuery().eq(UacMember::getUscCode, uscc).eq(UacMember::getStatus, 0).eq(UacMember::getKindId, 1).list();
        return uacb;
    }


    public Map<String, Object> findMemberDetails(Long staffId, Long memberId, Integer type) throws Exception {

        /**1为个人 , 2为企业*/
        if (type == 2) {
            if (staffId == null) {
                throw new AeotradeException("用户id不能为空");
            }
            if (memberId == null) {
                throw new AeotradeException("企业id不能为空");
            }
            stringRedisTemplate.opsForValue().set("AEOTRADE_USER:" + staffId, String.valueOf(memberId), 1, TimeUnit.DAYS);
            ThreadPoolUtils.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        List<UacMember> uacMembers = lambdaQuery().eq(UacMember::getStaffId, staffId).eq(UacMember::getId, memberId).eq(UacMember::getStatus, 0).list();
                        UacMember uacMember = uacMembers.size() > 0 ? uacMembers.get(0) : null;
                        if (null != uacMember) {
                            stringRedisTemplate.opsForValue().set("AEOTRADE_ROLE:" + staffId + memberId, String.valueOf(memberId));
                        } else {
                            List<UacAdminRole> uacAdminRoles = uacAdminRoleService.selectJoinList(UacAdminRole.class,
                                    MPJWrappers.<UacAdminRole>lambdaJoin().disableSubLogicDel().disableLogicDel()
                                            .selectAll(UacAdminRole.class)
                                            .leftJoin(UacAdmin.class, UacAdmin::getId, UacAdminRole::getAdminId)
                                            .eq(UacAdmin::getStaffId, staffId)
                                            .eq(UacAdminRole::getMemberId, memberId)
                                            .eq(UacAdmin::getStatus, 1)
                            );
                            Set<Long> longSet = uacAdminRoles.stream().map(UacAdminRole::getRoleId).distinct().collect(Collectors.toSet());
                            if (longSet.size()!=0) {
                                stringRedisTemplate.opsForValue().set("AEOTRADE_ROLE:" + staffId + memberId, StringUtils.join(longSet.toArray(), ","));
                            }
                        }
                    } catch (Exception e) {
                        log.warn(e.getMessage());
                    }
                }
            });
            loginVIp(memberId);
            return findMember(memberId, staffId);
        } else {
            UacStaff uacStaff = uacStaffMapper.getById(staffId);
            if (uacStaff == null) throw new AeotradeException("无员工信息");
            List<UacUserConnection> list = uacUserConnectionMapper.lambdaQuery().eq(UacUserConnection::getStaffId, staffId).list();
            UacUserConnection connection = list.size() > 0 ? list.get(0) : null;
            if (uacStaff != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("memberid", StringUtils.EMPTY);//企业Id
                map.put("staffid", StringUtils.EMPTY);//员工id
                map.put("staffname", StringUtils.EMPTY);//员工名称
                map.put("kindid", StringUtils.EMPTY);
                map.put("isatff", 0);
                map.put("uscc", StringUtils.EMPTY);//uscc
                map.put("membername", StringUtils.EMPTY);//企业名称
                map.put("memberimg", StringUtils.EMPTY);//企业logo
                map.put("sgsStatus", 0);//单一窗口认证状态
                map.put("memberstatus", 0);//企业状态
                map.put("stasfTel", StringUtils.EMPTY);//电话
                map.put("email", StringUtils.EMPTY);//邮箱
                map.put("staffimg", StringUtils.EMPTY);//用户微信头像
                map.put("openid", StringUtils.EMPTY);//openId
                map.put("email", StringUtils.EMPTY);//邮件
                map.put("qq", StringUtils.EMPTY);//QQ
                map.put("weixin", StringUtils.EMPTY);//微信
                map.put("weixinqr", StringUtils.EMPTY);//微信二维码
                map.put("orgChainId", StringUtils.EMPTY);//当前企业的组织标识
                map.put("userChainId", StringUtils.EMPTY);//个人在当前企业的用户标识
                map.put("memberimg", defaultImg);


                map.put("memberid", uacStaff.getMemberId());//企业Id
                map.put("staffid", uacStaff.getId());//员工id
                map.put("isLogin", uacStaff.getIsLogin());

                map.put("kindid", 99L);
                map.put("isatff", 0);

                map.put("sgsStatus", 0);//单一窗口认证状态
                map.put("memberstatus", 0);//企业状态
                if (StringUtils.isNotBlank(uacStaff.getTel())) {
                    map.put("stasfTel", uacStaff.getTel());//电话
                } else {
                    List<UacAdmin> uacAdmins = uacAdminService.lambdaQuery().eq(UacAdmin::getStaffId, uacStaff.getId()).list();
                    String uacAdmin = uacAdmins.size() > 0 ? uacAdmins.get(0).getMobile() : null;
                    if (StringUtils.isNotBlank(uacAdmin)) {
                        map.put("stasfTel", uacAdmin);//电话
                        uacStaff.setTel(uacAdmin);
                        uacStaffMapper.updateById(uacStaff);
                    }
                }
                map.put("staffimg", uacStaff.getWxLogo());//用户微信头像

                map.put("email", StringUtils.isEmpty(uacStaff.getContactEmail()) ? "" : uacStaff.getContactEmail());//邮件
                map.put("qq", StringUtils.isEmpty(uacStaff.getContactQq()) ? "" : uacStaff.getContactQq());//QQ
                map.put("weixin", StringUtils.isEmpty(uacStaff.getContactWeixin()) ? "" : uacStaff.getContactWeixin());//微信
                map.put("weixinqr", StringUtils.isEmpty(uacStaff.getContactWeixinqr()) ? "" : uacStaff.getContactWeixinqr());//微信二维码
                if (connection != null) {
                    map.put("staffname", StringUtils.isEmpty(uacStaff.getStaffName()) ? connection.getDisplayName() : uacStaff.getStaffName());//员工名称
                    map.put("memberimg", StringUtils.isEmpty(uacStaff.getWxLogo()) ? defaultImg : uacStaff.getWxLogo());//企业logo
                    map.put("openid", connection.getProviderUserId());//openId
                } else {
                    map.put("staffname", uacStaff.getStaffName());//员工名称
                    map.put("memberimg", StringUtils.isEmpty(uacStaff.getWxLogo()) ? defaultImg : uacStaff.getWxLogo());//企业logo
                    map.put("openid", uacStaff.getWxOpenid());//openId
                }

                map.put("vipType", StringUtils.EMPTY);
                map.put("vipIco", StringUtils.EMPTY);
                Map<String, Object> user = new HashMap<>();
                user.put("sgsStatus", 0);//单一窗口认证状态
                user.put("membername", StringUtils.EMPTY);//企业名称
                user.put("memberid", 0L);//企业Id
                user.put("kindid", 0L);
                user.put("staffname", StringUtils.EMPTY);//员工名称
                user.put("userAuthStatus", 1);
                user.put("memberimg", defaultImg);
                user.put("userAuthStatus", uacStaff.getAuthStatus());
                if (connection != null) {
                    user.put("memberimg", StringUtils.isEmpty(uacStaff.getWxLogo()) ? defaultImg : uacStaff.getWxLogo());//企业logo
                    user.put("staffname", StringUtils.isEmpty(uacStaff.getStaffName()) ? connection.getDisplayName() : uacStaff.getStaffName());//员工名称
                } else {
                    user.put("memberimg", StringUtils.isEmpty(uacStaff.getWxLogo()) ? defaultImg : uacStaff.getWxLogo());//企业logo
                    user.put("staffname", uacStaff.getStaffName());//员工名称
                }
                if (StringUtils.isEmpty(user.get("memberimg").toString())) {
                    user.put("memberimg", defaultImg);
                }
                user.put("sgsStatus", 0);//单一窗口认证状态
                user.put("membername", uacStaff.getStaffName());//企业名称
                user.put("memberid", uacStaff.getMemberId());//企业Id
                user.put("kindid", 99L);

                Map<String, Object> workMap = new HashMap<>();
                workMap.put("bench", new ArrayList<>());

                Map<String, Object> rMap = new HashMap<>();
                if (connection != null && !org.springframework.util.StringUtils.isEmpty(url)) {
                    rMap.put("provideruserid", connection.getProviderUserId());
                    rMap.put("providermqid", connection.getProviderMpId());
                    rMap.put("unionid", connection.getUnionid());
                    rMap.put("nickname", connection.getImageUrl());
                    rMap.put("headimgurl", StringUtils.isEmpty(uacStaff.getWxLogo()) ? defaultImg : uacStaff.getWxLogo());
                } else {
                    rMap.put("provideruserid", uacStaff.getWxOpenid());
                    rMap.put("providermqid", null);
                    rMap.put("unionid", uacStaff.getWxUnionid());
                    rMap.put("nickname", uacStaff.getStaffName());
                    rMap.put("headimgurl", StringUtils.isEmpty(uacStaff.getWxLogo()) ? defaultImg : uacStaff.getWxLogo());
                }
                rMap.put("isbind", true);
                rMap.put("bind", map);
                rMap.put("xpid", 0);
                rMap.put("member", "");
                rMap.put("user", user);
                rMap.put("workMap", workMap);
                return rMap;
            }
            return null;
        }

    }



    private void loginVIp(Long id) {
        try {
            Map<String, Object> getMap = new HashMap<>();
            getMap.put("id", id);
            getMap.put("apply", 1);

            String s = HttpRequestUtils.httpGet(mamUri, getMap, null);
            RespResult respResult = JSONObject.parseObject(s, RespResult.class);
            if (respResult.getCode() != 200) {
                RespResult respResult1 = mamberFeign.loginMessage(id, 1);
            }
        } catch (Exception e) {
            log.warn(e.getMessage());

        }
    }

    public Map<String, Object> findMember(Long memberId, Long staffId) throws Exception {

        UacMember uacMember = this.getById(memberId);
        if (uacMember == null) throw new AeotradeException("无企业信息");
        if (uacMember.getStaffId() == null) throw new AeotradeException("为查询到员工信息");
        UacStaff uacStaff = uacStaffMapper.getById(staffId);
        if (uacStaff == null) throw new AeotradeException("无员工信息");
        List<UacUserConnection> uacUserConnections = uacUserConnectionMapper.lambdaQuery().eq(UacUserConnection::getStaffId, staffId).list();
        UacUserConnection connection = uacUserConnections.size() > 0 ? uacUserConnections.get(0) : null;
        if (uacStaff != null && uacMember != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("memberid", 0L);//企业Id
            map.put("membername", StringUtils.EMPTY);//企业名称
            map.put("kindid", StringUtils.EMPTY);//企业类型
            map.put("memberstatus", 0);//企业状态
            map.put("staffid", StringUtils.EMPTY);//企业联系人
            map.put("staffname", StringUtils.EMPTY);//企业联系人名称
            map.put("createdby", StringUtils.EMPTY);//创建人
            map.put("updateby", StringUtils.EMPTY);//更新人
            map.put("updatetime", StringUtils.EMPTY);//更新时间
            map.put("stasfTel", StringUtils.EMPTY);//电话
            map.put("remark", StringUtils.EMPTY);//备注
            map.put("memberimg", StringUtils.EMPTY);//企业logo
            map.put("email", StringUtils.EMPTY);//邮箱
            map.put("weixinqr", StringUtils.EMPTY);//微信二维码
            map.put("datetime", StringUtils.EMPTY);//企业创建时间
            map.put("staffimg", StringUtils.EMPTY);//用户微信头像
            map.put("openid", StringUtils.EMPTY);//openId
            map.put("email", StringUtils.EMPTY);//邮件
            map.put("adminName", StringUtils.EMPTY);//主管理员名称
            map.put("qq", StringUtils.EMPTY);//QQ
            map.put("weixin", StringUtils.EMPTY);//微信
            map.put("uscc", StringUtils.EMPTY);//统一社会信用代码
            map.put("orgChainId", StringUtils.EMPTY);//当前企业的组织标识
            map.put("userChainId", StringUtils.EMPTY);//个人在当前企业的用户标识
            ThreadPoolUtils.execute(new Runnable() {
                @Override
                public void run() {
                    Query query = new Query();
                    query.addCriteria(Criteria.where("tenantId").is(memberId))
                            .addCriteria(Criteria.where("userId").is(staffId));
                    List<Document> documents = mongoTemplate.find(query, Document.class, "chain_cert_user_member");
                    if (null != documents && documents.size() != 0) {
                        Object certId = documents.get(0).get("signCertId");
                        Document chain = mongoTemplate.findById(certId, Document.class, "chain_sdk_cert");
                        if (null != chain) {
                            map.put("userChainId", chain.getString("certName"));
                            map.put("orgChainId", chain.getString("orgId"));
                        }
                    }
                }
            });
            map.put("memberid", uacMember.getId());//企业Id
            map.put("membername", uacMember.getMemberName());//企业名称
            map.put("kindid", uacMember.getKindId());//企业类型
            map.put("memberstatus", uacMember.getMemberStatus());//企业状态
            map.put("staffid", staffId);//企业联系人
            map.put("staffname", uacMember.getStaffName());//企业联系人名称
            map.put("createdby", uacMember.getCreatedBy());//创建人
            map.put("updateby", uacMember.getUpdatedBy());//更新人
            map.put("updatetime", uacMember.getUpdatedTime());//更新时间
            map.put("stasfTel", uacMember.getStasfTel());//电话
            map.put("remark", uacMember.getRemark());//备注
            map.put("uscc", uacMember.getUscCode());//统一社会信用代码

            map.put("email", uacMember.getEmail());//邮箱
            map.put("weixinqr", StringUtils.isEmpty(uacStaff.getContactWeixinqr()) ? "" : uacStaff.getContactWeixinqr());//微信二维码
            map.put("datetime", uacMember.getDateTime());//企业创建时间
            map.put("staffimg", uacStaff.getWxLogo());//用户微信头像

            map.put("qq", StringUtils.isEmpty(uacStaff.getContactQq()) ? "" : uacStaff.getContactQq());//QQ
            map.put("weixin", StringUtils.isEmpty(uacStaff.getContactWeixin()) ? "" : uacStaff.getContactWeixin());//微信
            List<Long> list = uawVipMessageService.lambdaQuery()
                    .eq(UawVipMessage::getVipStatus, 1).eq(UawVipMessage::getUserType, 1)
                    .eq(UawVipMessage::getStatus, 0)
                    .eq(UawVipMessage::getMemberId, uacMember.getId()).list().
                    stream().map(UawVipMessage::getTypeId).collect(Collectors.toList());
            map.put("vipType", list);
            map.put("vipIco", StringUtils.EMPTY);
            map.put("userAuthStatus", 1);
            if (connection != null) {
                map.put("memberimg", StringUtils.isBlank(uacMember.getLogoImg()) ? uacStaff.getWxLogo() : uacMember.getLogoImg());//企业logo
                map.put("openid", connection.getProviderUserId());//openId
            } else {
                map.put("memberimg", uacMember.getLogoImg());//企业logo
                map.put("openid", uacStaff.getWxOpenid());//openId
            }
            map.put("userAuthStatus", uacStaff.getAuthStatus());
            if(uacMember.getStaffId()==uacStaff.getId()){
                map.put("adminName", uacStaff.getStaffName());
            }else{
                UacStaff admin = uacStaffMapper.getById(uacMember.getStaffId());
                map.put("adminName", admin.getStaffName());
            }
            Map<String, Object> user = new HashMap<>();
            user.put("memberimg", StringUtils.EMPTY);//企业logo
            user.put("sgsStatus", 0);//单一窗口认证状态
            user.put("membername", StringUtils.EMPTY);//企业名称
            user.put("memberid", StringUtils.EMPTY);//企业Id
            user.put("kindid", StringUtils.EMPTY);
            user.put("staffname", StringUtils.EMPTY);//员工名称


            user.put("sgsStatus", uacMember.getSgsStatus());//单一窗口认证状态
            user.put("membername", uacMember.getMemberName());//企业名称
            user.put("memberid", uacMember.getId());//企业Id
            user.put("kindid", uacMember.getKindId());
            user.put("isTest", "0");//是否参与灰度测试
            user.put("jumpAddress", StringUtils.EMPTY);//跳转地址
            if (uacMember.getIsTest() != 0) {
                user.put("isTest", "1");//是否参与灰度测试
                user.put("jumpAddress", uacMember.getJumpAddress());//跳转地址
            }

            if (connection != null) {
                user.put("memberimg", StringUtils.isBlank(uacMember.getLogoImg()) ? uacStaff.getWxLogo() : uacMember.getLogoImg());//企业logo
                user.put("staffname", StringUtils.isEmpty(uacStaff.getStaffName()) ? connection.getDisplayName() : uacStaff.getStaffName());//员工名称
            } else {
                user.put("memberimg", uacMember.getLogoImg());//企业logo
                user.put("staffname", uacStaff.getStaffName());//openId
            }
            Map<String, Object> workMap = new HashMap<>();
            workMap.put("bench", new ArrayList<>());
            Map<String, Object> rMap = new HashMap<>();
            if (connection != null) {
                rMap.put("provideruserid", connection.getProviderUserId());
                rMap.put("providermqid", connection.getProviderMpId());
                rMap.put("unionid", connection.getUnionid());
                rMap.put("nickname", uacStaff.getStaffName());
                rMap.put("headimgurl", uacStaff.getWxLogo());
            } else {
                rMap.put("provideruserid", uacStaff.getWxOpenid());
                rMap.put("providermqid", null);
                rMap.put("unionid", uacStaff.getWxUnionid());
                rMap.put("nickname", uacStaff.getStaffName());
                rMap.put("headimgurl", uacStaff.getWxLogo());
            }
            rMap.put("isbind", true);
            rMap.put("bind", map);
            rMap.put("xpid", 0);
            rMap.put("member", "");
            rMap.put("user", user);
            rMap.put("workMap", workMap);
            return rMap;
        }
        return null;
    }

    public Boolean findTime(Long memberId) {
        UacMember uacMember = this.getById(memberId);
        Integer sgsStatus = uacMember.getSgsStatus();
        return sgsStatus == 1;
    }


    public PageList<UacMember> findAllMemberPage(Integer pageSize, Integer pageNo) {
//        return uacMemberMapper.listUacMemberPage(page);
        LambdaQueryWrapper<UacMember> uacMemberLambdaQueryWrapper = new LambdaQueryWrapper<>();
        uacMemberLambdaQueryWrapper.ne(UacMember::getKindId, 88L);
        uacMemberLambdaQueryWrapper.ne(UacMember::getKindId, 99L);
        uacMemberLambdaQueryWrapper.eq(UacMember::getStatus, 0);
        Page<UacMember> uacMemberPageList = this.page(new Page<>(pageNo, pageSize), uacMemberLambdaQueryWrapper);
        PageList<UacMember> pageList = new PageList<>();
        pageList.setRecords(uacMemberPageList.getRecords());
        pageList.setTotalSize(uacMemberPageList.getTotal());
        return pageList;
    }

    public Map<String, List<UacMember>> findStaffMemberList(Long id) {
        Map<String, List<UacMember>> map = new HashMap<>();
        map.put("admin", null);
        map.put("staff", null);
        /*1.根据企业id查询员工表id*/
        UacStaff uacStaff = uacStaffMapper.getById(id);
        if (uacStaff == null || uacStaff.getId() == null) {
            throw new AeotradeException("企业未与微信绑定");
        }
        /*2.根据员工id查询关联表的企业*/
        List<UacMember> uacMembers = new ArrayList<>();
        List<UacMemberStaff> list = uacMemberStaffMapper.selectJoinList(UacMemberStaff.class,
                MPJWrappers.<UacMemberStaff>lambdaJoin().disableSubLogicDel().disableLogicDel()
                        .selectAll(UacMemberStaff.class)
                        .leftJoin(UacStaff.class,UacStaff::getId,UacMemberStaff::getStaffId)
                        .leftJoin(UacMember.class,UacMember::getId,UacMemberStaff::getMemberId)
                        .eq(UacMemberStaff::getStaffId,id)
                        .ne(UacMember::getStaffId,UacStaff::getId)
        );
        for (UacMemberStaff mstaff : list) {
            List<UacMember> members = this.lambdaQuery()
                    .eq(UacMember::getId, mstaff.getMemberId())
                    .eq(UacMember::getStatus, 0).list();
            UacMember uacMember = members.size() > 0 ? members.get(0) : null;
            if (uacMember != null) {
                uacMember.setCreatedTime(mstaff.getCreatedTime());
                uacMembers.add(uacMember);
            }
        }
        List<UacMember> listAdminMenber = this.lambdaQuery()
                .ne(UacMember::getKindId, 88L)
                .ne(UacMember::getKindId, 99L)
                .eq(UacMember::getStatus, 0)
                .eq(UacMember::getStaffId, id).list();
        if (listAdminMenber.size() != 0) {
            map.put("admin", listAdminMenber);
        }
        if (uacMembers.size() != 0) {
            map.put("staff", uacMembers);
        }
        return map;
    }

    public List<String> listMembersByStaffId(Long staffId) {
        return this.selectJoinList(UacMember.class,
                MPJWrappers.<UacMember>lambdaJoin().disableSubLogicDel().disableLogicDel()
                        .selectAll(UacMember.class)
                        .leftJoin(UacMemberStaff.class, UacMemberStaff::getMemberId, UacMember::getId)
                        .eq(UacMember::getStatus, 0)
                        .eq(UacMemberStaff::getStaffId, staffId)
                        .or().eq(UacMember::getStaffId, staffId)
                        .ne(UacMember::getKindId, 88L)
                        .ne(UacMember::getKindId, 99L)
        ).stream().map(UacMember::getMemberName).collect(Collectors.toList());
    }

    public List<UacMember> listMemberByName(String name) {
        return this.lambdaQuery().like(UacMember::getMemberName, name)
                .or().like(UacMember::getLegalPerson, name)
                .eq(UacMember::getKindId, 1L).list();
    }

    public Boolean getToken(String token, Long memberId) {
        OAuth2Authentication oAuth2Authentication = hmtxUserInfoTokenServices.loadAuthentication(token);
        String s = oAuth2Authentication.getPrincipal().toString();
//        System.out.println("获取用户信息：" + s);
        Map<String, Object> username = JSONObject.parseObject(s, Map.class);
//        System.out.println("用户信息转换后：" + username.toString());
        if (null != username.get("staffId")) {
            List<UacMemberStaff> staffId = uacMemberStaffMapper.lambdaQuery()
                    .eq(UacMemberStaff::getMemberId, memberId)
                    .eq(UacMemberStaff::getStaffId, username.get("staffId")).list();
            return staffId.size() != 0;
        }
        return false;
    }

    public List<UacMember> listMemberAll() {
        return this.lambdaQuery().ne(UacMember::getKindId, 99L)
                .ne(UacMember::getKindId, 88L).eq(UacMember::getStatus, 0).list();
    }

    public List<UacMember> listMemberAllForUsccOrName(String uscc) {
        if (hasChinese(uscc)) {
            return this.lambdaQuery()
                    .ne(UacMember::getKindId, 99L)
                    .ne(UacMember::getKindId, 88L)
                    .eq(UacMember::getStatus, 0)
                    .like(UacMember::getMemberName, uscc).list();

        } else {
            return this.lambdaQuery()
                    .ne(UacMember::getKindId, 99L)
                    .ne(UacMember::getKindId, 88L)
                    .eq(UacMember::getStatus, 0)
                    .like(UacMember::getUscCode, uscc).list();
        }
    }


    private boolean hasChinese(String value) {
        // 汉字的Unicode取值范围
        String regex = "[\u4e00-\u9fa5]";
        Pattern pattern = Pattern.compile(regex);
        Matcher match = pattern.matcher(value);
        return match.find();
    }
}
