package com.aeotrade.provider.service.impl;

import com.aeotrade.annotation.Ex;
import com.aeotrade.base.constant.HeadPortraitConstant;
import com.aeotrade.base.constant.WorkBenchAppService;
import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.event.AddMemberEvent;
import com.aeotrade.provider.mapper.UacAdminMapper;
import com.aeotrade.provider.mapper.UacMemberSynchronizationMapper;
import com.aeotrade.provider.mapper.UacStaffMapper;
import com.aeotrade.provider.model.*;
import com.aeotrade.provider.service.*;
import com.aeotrade.provider.vo.*;
import com.aeotrade.service.MqSend;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.RespResult;
import com.aeotrade.utlis.DateUtil;
import com.aeotrade.utlis.HttpRequestUtils;
import com.aeotrade.utlis.ThreadPoolUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: 吴浩
 * @Date: 2021-08-18 18:00
 */
@Slf4j
@Service
public class SingleLoginService extends BaseController {
    @Autowired
    private UacStaffService uacStaffService;
    @Autowired
    private UacStaffMapper uacStaffMapper;
    @Autowired
    private UacMemberService uacMemberService;
    @Autowired
    private UacMemberSynchronizationMapper uacMemberSynchronizationMapper;
    @Autowired
    private UacAdminMapper uacAdminMapper;
    @Autowired
    private UacMemberStaffService uacMemberStaffService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UacUserService uacUserService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Value("${hmtx.tijianzhinang.url:}")
    private String url;
    @Value("${hmtx.get.token:}")
    private String getTokens;
    @Value("${hmtx.get.user:}")
    private String getUser;
    @Value("${hmtx.login.gateway-url}")
    private String gatewayUrl;
    @Autowired
    private MqSend mqSend;
    @Autowired
    private UawWorkbenchService uawWorkbenchService;
    @Autowired
    private UawVipTypeService uawVipTypeService;
    @Autowired
    private UawVipTypeGroupService uawVipTypeGroupService;

    @Transactional
    @Ex(value = "调用开通默认会员异常")
    public void loginMessage(Long id, int apply) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("apply", apply);
        log.info("http调用开始" + JSONObject.toJSONString(map));
        String http = HttpRequestUtils.httpGet(gatewayUrl+"/mam/uaw/VipMessage/loginMessage", map);
        RespResult respResult = JSONObject.parseObject(http, RespResult.class);
        if (respResult.getCode() != 200) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new AeotradeException("注册失败,请重试");
        }
    }

    @Transactional
    public RegisterReturn staffCreaet(String s, Long staffId, RegisterOne registerOne) throws Exception {
        UacStaff uacStaff = uacStaffMapper.selectById(staffId);
        Long instrer = this.creatMember(uacStaff.getId(), s, registerOne);
        UacMemberStaff memberStaff = new UacMemberStaff();
        memberStaff.setStaffId(uacStaff.getId());
        memberStaff.setMemberId(instrer);
        memberStaff.setIsAdmin(0);
        memberStaff.setKindId(0L);
        uacMemberStaffService.save(memberStaff);
//        this.loginMessage(instrer, 1);
        uacStaff.setStatus(0);
        uacStaff.setLastMemberId(instrer);
        List<UawWorkbench> lastWorkbench = uawWorkbenchService.lambdaQuery().eq(UawWorkbench::getId,
                uawVipTypeService.getById(registerOne.getVipTypeId()).getWorkbench()).list();
        if (lastWorkbench.size() > 0) {
            uacStaff.setLastWorkbenchId(lastWorkbench.get(0).getId());
            uacStaff.setChannelColumnsId(lastWorkbench.get(0).getChannelColumnsId());
        }
        uacStaffService.updateById(uacStaff);
        return new RegisterReturn(uacStaff, instrer);
    }

    @Transactional
    public RegisterReturn visitorCreaet(String s, RegisterOne registerOne) throws Exception {
        UacStaff uacStaff = new UacStaff();
        uacStaff.setStaffName(registerOne.getStaffName());
        uacStaff.setTel(registerOne.getPhone());
        uacStaff.setStaffType(0);
        uacStaff.setSourceMark(registerOne.getSourceMark());
        uacStaff.setChannelMark(registerOne.getChannelMark());
        uacStaff.setAuthStatus(0);
        uacStaff.setSgsStatus(0);
        uacStaff.setCreatedTime(DateUtil.getData().toLocalDateTime());
        uacStaff.setIsLogin(0);
        uacStaff.setWxLogo(HeadPortraitConstant.InputDataType.STAFF_HEAD.getValue());
        uacStaffMapper.insert(uacStaff);
        this.loginMessage(uacStaff.getId(), 0);
        Long instrer = this.creatMember(uacStaff.getId(), s, registerOne);
//        this.loginMessage(instrer, 1);
        uacStaff.setMemberId(instrer);
        uacStaff.setStatus(0);
        uacStaff.setRevision(0);
        uacStaff.setLastMemberId(instrer);
        List<UawWorkbench> lastWorkbench = uawWorkbenchService.lambdaQuery().list();
        if (!lastWorkbench.isEmpty()) {
            uacStaff.setLastWorkbenchId(lastWorkbench.get(0).getId());
            uacStaff.setChannelColumnsId(lastWorkbench.get(0).getChannelColumnsId());
        }
        uacStaffService.updateById(uacStaff);
        UacMemberStaff memberStaff = new UacMemberStaff();
        memberStaff.setStaffId(uacStaff.getId());
        memberStaff.setMemberId(instrer);
        memberStaff.setIsAdmin(0);
        memberStaff.setKindId(0L);
        uacMemberStaffService.save(memberStaff);
        uacUserService.bindingMobile(registerOne.getPhone(), uacStaff.getId(),null,null);
        return new RegisterReturn(uacStaff, instrer);
    }

    @Transactional
    public Long creatMember(Long staffId, String s, RegisterOne registerOne) throws Exception {
        UacMember uacMember = new UacMember();
        uacMember.setStaffId(staffId);
        uacMember.setMemberName(registerOne.getMemberName());
        uacMember.setKindId(1L);
        uacMember.setUscCode(s);
        uacMember.setMemberStatus(0);
        uacMember.setStaffName(registerOne.getStaffName());
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        uacMember.setCreatedTime(timestamp.toLocalDateTime());
        uacMember.setStasfTel(registerOne.getPhone());
        uacMember.setSgsStatus(0);
        uacMember.setStatus(0);
        uacMember.setAtpwStatus(0);
        uacMember.setLogoImg(HeadPortraitConstant.InputDataType.MEMBER_HEAD.getValue());
        boolean instrer = uacMemberService.save(uacMember);
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    tongbuerp(uacMember);
                    Map<String, Object> map = new HashMap<>();
                    map.put("memberName", registerOne.getMemberName());
                    map.put("uscCode", s);
                    map.put("staffName", registerOne.getStaffName());
                    map.put("mobile", registerOne.getPhone());
                    UawVipType vipType = uawVipTypeService.getById(registerOne.getVipTypeId());
                    map.put("role", vipType.getTypeName());
                    map.put("memberId",uacMember.getId());
                    map.put("userId",staffId);
                    //以事件方式处理邮件
                    AddMemberEvent addMemberEvent=new AddMemberEvent(this,map);
                    eventPublisher.publishEvent(addMemberEvent);

                    HashMap<String, String> chain = new HashMap<>();
                    chain.put("tenantId", String.valueOf(uacMember.getId()));
                    chain.put("tenantName", uacMember.getMemberName());
                    chain.put("uscc", uacMember.getUscCode());
                    SimpleDateFormat sDFormat = new SimpleDateFormat("MMddHHmmssSSS");
                    chain.put("creatTime", sDFormat.format(timestamp));
                    chain.put("userType", "员工");
                    chain.put("userId", String.valueOf(uacMember.getStaffId()));
                    chain.put("roleCodeRulesEnum", vipType.getRelevancyTypeId());
                    chain.put("chainId", "aeotradechain");
                    chain.put("userTypeEnum", "管理员");
                    mqSend.sendChain(JSONObject.toJSONString(chain), "chain");
                } catch (Exception e) {
                    log.warn(e.getMessage());
                }
            }
        });
        return uacMember.getId();
    }


    @Ex("同步体检智囊异常")
    public void tongbuerp(UacMember uacMember) throws Exception {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isNotEmpty(url) && StringUtils.isNotEmpty(uacMember.getMemberName()) && StringUtils.isNotEmpty(uacMember.getUscCode())) {
            map.put("company_name", uacMember.getMemberName());
            map.put("hmm_company_id", uacMember.getId());
            map.put("usc_code", uacMember.getUscCode());
            UacMemberSynchronization uacMemberSynchronization = new UacMemberSynchronization();
            uacMemberSynchronization.setMemberId(uacMember.getId());
            uacMemberSynchronization.setMemberNaem(uacMember.getMemberName());
            uacMemberSynchronization.setMemberUscc(uacMember.getUscCode());
            uacMemberSynchronization.setSynchronousStatus(1);
            String http = HttpRequestUtils.httpPost(url, map);
            RespResult httprespResult = JSONObject.parseObject(http, RespResult.class);
            uacMemberSynchronization.setReturnResult(httprespResult.toString());
            uacMemberSynchronizationMapper.insert(uacMemberSynchronization);
            if (httprespResult.getCode() != 200) {
                uacMemberSynchronization.setSynchronousStatus(0);
                uacMemberSynchronizationMapper.updateById(uacMemberSynchronization);
            }
        }
    }

    @Ex(value = "北京单一窗口注册登录")
    public RegisterReturn loginSingle(RegisterOne registerOne) throws Exception {
        List<UacMember> uacb = uacMemberService.lambdaQuery().eq(UacMember::getUscCode,registerOne.getUscCode())
                .notIn(UacMember::getKindId,88,99).list();
        LambdaQueryWrapper<UacAdmin> uacAdminLambdaQueryWrapper=new LambdaQueryWrapper<>();
        uacAdminLambdaQueryWrapper.eq(UacAdmin::getMobile,registerOne.getPhone()).eq(UacAdmin::getStatus,1)
                .eq(UacAdmin::getIsTab,1).orderByDesc(UacAdmin::getCreateTime);
        List<UacAdmin> uacAdmins = uacAdminMapper.selectList(uacAdminLambdaQueryWrapper);
        UacAdmin byMobile =uacAdmins.size()>0?uacAdmins.get(0):null;
        if (byMobile != null && uacb != null && uacb.size() != 0) {
            UacStaff uacStaff = uacStaffService.getById(byMobile.getStaffId());
            uacStaff.setLastMemberId(uacb.get(0).getId());
            if (registerOne.getVipTypeId() == null) {
                List<UawVipTypeGroup> list = uawVipTypeGroupService.lambdaQuery().eq(UawVipTypeGroup::getApply, 1)
                        .eq(UawVipTypeGroup::getIsDefaultVip, 1).list();
                List<UawVipType> defaultType = uawVipTypeService.lambdaQuery().eq(UawVipType::getGroupId, list.size()>0?list.get(0).getId():null).orderByAsc(UawVipType::getSort).list();
                registerOne.setVipTypeId(defaultType.get(0).getId());
            }
            List<UawVipType> uawVipTypes = uawVipTypeService.lambdaQuery().eq(UawVipType::getId, registerOne.getVipTypeId()).list();
            List<UawWorkbench> lastWorkbench = uawWorkbenchService.lambdaQuery()
                    .eq(UawWorkbench::getId, uawVipTypes.size()>0?uawVipTypes.get(0).getWorkbench():null).list();
            if (lastWorkbench.size() != 0) {
                uacStaff.setLastWorkbenchId(lastWorkbench.get(0).getId());
                uacStaff.setChannelColumnsId(lastWorkbench.get(0).getChannelColumnsId());
            }
            uacStaffService.updateById(uacStaff);
            LambdaQueryWrapper<UacMemberStaff> uacMemberStaffLambdaQueryWrapper=new LambdaQueryWrapper<>();
            uacMemberStaffLambdaQueryWrapper.eq(UacMemberStaff::getStaffId,uacStaff.getId()).eq(UacMemberStaff::getMemberId,uacb.get(0).getId());
            List<UacMemberStaff> uacMemberStaffList = uacMemberStaffService.list(uacMemberStaffLambdaQueryWrapper);
            if (uacMemberStaffList.isEmpty()) {
                uacMemberStaffService.initUacStaffMember(uacStaff.getId(), uacb.get(0).getId());
            }
            stringRedisTemplate.delete("MEMBER_WORKBENCH:" + uacStaff.getId() + uacb.get(0).getId());
            stringRedisTemplate.opsForValue().append("MEMBER_WORKBENCH:" + uacStaff.getId() + uacb.get(0).getId(), String.valueOf(uacStaff.getLastWorkbenchId()));
            return new RegisterReturn(uacStaffService.getById(byMobile.getStaffId()), uacb.get(0).getId());
        }
        if (byMobile == null && uacb != null && uacb.size() != 0) {
            UacMember uacMember = new UacMember();
            for (UacMember member : uacb) {
                if (member.getKindId() != 99) {
                    BeanUtils.copyProperties(member, uacMember);
                    break;
                }
            }
            UacStaff uacStaff = new UacStaff();
            uacStaff.setStaffName(registerOne.getStaffName());
            uacStaff.setTel(registerOne.getPhone());
            uacStaff.setStaffType(0);
            uacStaff.setSourceMark(registerOne.getSourceMark());
            uacStaff.setCreatedTime(DateUtil.getData().toLocalDateTime());
            uacStaff.setChannelMark(registerOne.getChannelMark());
            uacStaff.setAuthStatus(0);
            uacStaff.setSgsStatus(0);
            uacStaff.setIsLogin(0);
            uacStaff.setMemberId(uacMember.getId());
            uacStaff.setWxLogo(HeadPortraitConstant.InputDataType.STAFF_HEAD.getValue());
            uacStaff.setLastMemberId(uacMember.getId());
            List<UawVipType> list = uawVipTypeService.lambdaQuery().eq(UawVipType::getId, registerOne.getVipTypeId()).list();
            List<UawWorkbench> lastWorkbench = uawWorkbenchService.lambdaQuery().eq(UawWorkbench::getId,list.size()>0?list.get(0).getWorkbench():null).list();
            if (lastWorkbench.size() != 0) {
                uacStaff.setLastWorkbenchId(lastWorkbench.get(0).getId());
                uacStaff.setChannelColumnsId(lastWorkbench.get(0).getChannelColumnsId());
            }
            uacStaffMapper.insert(uacStaff);
            this.loginMessage(uacStaff.getId(), 0);
            uacUserService.bindingMobile(registerOne.getPhone(), uacStaff.getId(),null,null);
            UacMemberStaff memberStaff = new UacMemberStaff();
            memberStaff.setStaffId(uacStaff.getId());
            memberStaff.setMemberId(uacMember.getId());
            memberStaff.setIsAdmin(0);
            memberStaff.setKindId(0L);
            uacMemberStaffService.save(memberStaff);

            return new RegisterReturn(uacStaff, uacMember.getId());
        }
        if (byMobile != null && uacb != null && uacb.size() == 0) {
            return this.staffCreaet(registerOne.getUscCode(), byMobile.getStaffId(), registerOne);
        }
        if (byMobile == null && uacb != null && uacb.size() == 0) {
            return this.visitorCreaet(registerOne.getUscCode(), registerOne);
        }
        return null;

    }

//    @Transactional
    @Ex(value = "单一窗口接收code换取token，token换取用户信息")
    public RegisterOne getToken(SingleClass singleClass) throws Exception {
        Map<String, Object> map = new HashMap<>();
        GetToken getToken = new GetToken();
        try {
            String http = HttpRequestUtils.httpPost(getTokens + "&code=" + singleClass.getCode(), map);
            getToken = JSONObject.parseObject(http, GetToken.class);
        } catch (Exception e) {
            throw new AeotradeException("获取用户信息的code已过期");
        }
        log.info("http调用返回" + JSONObject.toJSONString(getToken));
        if (StringUtils.isNotEmpty(getToken.getError())) {
            throw new AeotradeException("无效的授权");
        }
        map.put("access_token", getToken.getAccess_token());
        String httpToken = HttpRequestUtils.httpGet(getUser, map);
        log.info("获取到的用户信息字符串" + httpToken);
        SingleUser singleUser = JSONObject.parseObject(httpToken, SingleUser.class);
        log.info("获取到的用户信息" + JSONObject.toJSONString(singleUser));
        if (null == singleUser) {
            throw new AeotradeException("无效的用户信息");
        }
        if(StringUtils.isEmpty(singleUser.getAttributes().getSocial_credit_code())){
            throw new AeotradeException("没有获取到企业统一社会信用代码");
        }
        RegisterOne registerOne = new RegisterOne();
        registerOne.setStaffName(singleUser.getAttributes().getOp_name());
        registerOne.setPhone(singleUser.getAttributes().getMobile());
        registerOne.setMemberName(singleUser.getAttributes().getEtps_name());
        registerOne.setUscCode(singleUser.getAttributes().getSocial_credit_code());
        registerOne.setWorkMark(singleClass.getWorkMark());
        registerOne.setSourceMark("北京单一窗口PC端");
        registerOne.setChannelMark(WorkBenchAppService.findCode(singleClass.getWorkMark()));

        singleClass.setWorkMark("app");
        registerOne.setWorkMark("app");
        String no = singleUser.getAttributes().getCus_reg_no();
        if (StringUtils.isNotEmpty(no)) {
            registerOne.setHaiguanNum(no);
            String substring = no.substring(5, 6);
            if (!org.springframework.util.StringUtils.isEmpty(substring)&&substring.equals("8")) {
                singleClass.setWorkMark("HMLWL");
                registerOne.setWorkMark("HMLWL");
            }
        }
        List<UawVipType> vipType = uawVipTypeService.lambdaQuery().eq(UawVipType::getCode,singleClass.getWorkMark()).list();
        registerOne.setVipTypeId(vipType.size()>0?vipType.get(0).getId():null);
        return registerOne;
    }

    @Ex(value = "入驻资料提交")
    public RespResult sgsListSave(UacStaff uacStaff, Long memberId, String memberName
            , String uscCode, String workMark) throws Exception {
        if (StringUtils.isNotEmpty(workMark)) {
            Map<String, Object> map = new HashMap<>();
            map.put("createdBy", uacStaff.getStaffName());
            map.put("createdById", uacStaff.getId());
            map.put("memberId", memberId);
            map.put("memberName", memberName);
            map.put("uscc", uscCode);
            map.put("vipTypeName", workMark);
            map.put("vipTypeId", 0);
            String http = HttpRequestUtils.httpPost(gatewayUrl+"/mam/aptitude/save", map);
            RespResult respResult = JSONObject.parseObject(http, RespResult.class);
            return respResult;
        }
        return null;
    }

}
