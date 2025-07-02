package com.aeotrade.provider.admin.controller;

import com.aeotrade.provider.admin.event.AddStaffEvent;
import com.aeotrade.provider.admin.service.*;
import com.aeotrade.provider.admin.service.impl.UacMemberServiceImpl;
import com.aeotrade.base.constant.AeoConstant;
import com.aeotrade.base.constant.BizConstant;
import com.aeotrade.entity.FebsResponse;
import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.admin.entiy.*;
import com.aeotrade.provider.admin.uacVo.*;

import com.aeotrade.provider.admin.service.impl.UacStaffServiceImpl;

import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.PageList;
import com.aeotrade.suppot.RespResult;
import com.aeotrade.suppot.RespResultMapper;
import com.aeotrade.utils.FebsUtil;
import com.aeotrade.utlis.HttpRequestUtils;
import com.aeotrade.utlis.NumberUtils;
import com.aeotrade.utlis.ThreadPoolUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限模块
 *
 * @Author: yewei
 * @Date: 2020/1/7 16:48
 */
@RestController
@RequestMapping(value = "/uac/manager/", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//@Api(value = "Web - UacManagerController", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@Slf4j
@CrossOrigin
public class UacManagerController extends BaseController {
    @Autowired
    private UacMemberServiceImpl uacMemberService;
    @Autowired
    private UacAdminService uacAdminService;
    @Autowired
    private UacStaffServiceImpl uacStaffService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private UacMemberStaffService uacMemberStaffService;
    @Autowired
    private UacAdminRoleService uacAdminRoleService;
    @Autowired
    private UacDeptStaffService uacDeptStaffService;
    @Autowired
    private UacRoleService uacRoleService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Value("${hmtx.uaaErpUri:}")
    private String uaaErpUri;
    @Value("${hmtx.login.gateway-url}")
    private String gatewayUrl;

    @GetMapping("page/member")
    //@ApiOperation(httpMethod = "GET", value = "Feign调用查询所有企业")
    public RespResult<PageList<UacMember>> findAllMemberPage(Integer pageSize, Integer pageNo) {
        try {
            return handleResult(uacMemberService.findAllMemberPage(pageSize, pageNo));
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    @GetMapping("logo")
    //@ApiOperation(httpMethod = "GET", value = "Feign调用查询企业")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "id", value = "企业ID")})
    public RespResult findMemberLogo(@RequestParam(value = "id") Long id) {
        try {
            if (null == id) {
                throw new AeotradeException("memberId为空");
            }
            UacMember uacMember = uacMemberService.get(id);
            if (uacMember != null && uacMember.getLogoImg() != null) {
                String logo = uacMember.getLogoImg();
                return handleResult(logo);
            }
            return handleResult(null);

        } catch (Exception e) {
            return handleFail(e);
        }
    }

    @GetMapping("find/memberStutas")
    //@ApiOperation(httpMethod = "GET", value = "Feign调用查询企业认证状态")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "id", value = "企业ID")})
    public RespResult findMemberStutas(@RequestParam(value = "id") Long id) {
        try {
            if (null == id) {
                throw new AeotradeException("memberId为空");
            }
            UacMember uacMember = uacMemberService.get(id);
            if (uacMember != null && uacMember.getSgsStatus() != null) {
                Integer sgsStatus = uacMember.getSgsStatus();
                return handleResult(sgsStatus);
            }
            return handleResult(null);

        } catch (Exception e) {
            return handleFail(e);
        }
    }

    @GetMapping("feign/member")
    //@ApiOperation(httpMethod = "GET", value = "Feign根据ID查询企业")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "memberId", value = "memberId")})
    public RespResult FeignmemberById(@RequestParam(value = "memberId") Long memberId) {
        if (null == memberId) {
            throw new AeotradeException("主键ID为空");
        }
        UacMember uacMember = uacMemberService.get(memberId);
        if (uacMember == null) {
            return null;
        } else {
            return handleResult(uacMember.getMemberName());
        }

    }

    @GetMapping("by/uscc")
    //@ApiOperation(httpMethod = "GET", value = "Feign调用根据UScc查询企业")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "uscc", value = "uscc")})
    public RespResult findMemberByUscc(@RequestParam(value = "uscc") String uscc) {
        try {
            if (StringUtils.isEmpty(uscc)) {
                throw new AeotradeException("uscc为空");
            }
            List<UacMember> uacMember = uacMemberService.findMemberByUscc(uscc);
            if (uacMember.size() == 0) {
                return handleResult(null);
            }
            return handleResult(uacMember.get(0));
        } catch (Exception e) {
            return handleOK();
        }
    }

    @GetMapping("by/id")
    //@ApiOperation(httpMethod = "GET", value = "Feign调用根据企业ID查询企业")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "id", value = "企业ID")})
    public RespResult findMemberByID(@RequestParam(value = "id") Long id) {
        try {
            if (null == id) {
                throw new AeotradeException("ID不能为空");
            }
            UacMember uacMember = uacMemberService.get(id);
            return handleResult(uacMember);
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    /**
     * 查询当前企业
     *
     * @param id
     * @return
     */
    @GetMapping("membe")
    //@ApiOperation(httpMethod = "GET", value = "查询当前企业")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "id", value = "员工ID")})
    public RespResult querMember(Long id) {
        try {
            if (null == id) {
                throw new AeotradeException("员工id不能为空");
            }
            MemberVO vo = uacMemberService.querMember(id);
            return handleResult(Optional.ofNullable(vo).orElseGet(() -> new MemberVO()));
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    /**
     * 查询当前企业认证状态
     *
     * @param id
     * @returnr
     */
    @GetMapping("memberStutas")
    //@ApiOperation(httpMethod = "GET", value = "查询当前企业")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "id", value = "企业ID")})
    public RespResult memberStutas(@RequestParam Long id) {
        try {
            if (null == id) {
                throw new AeotradeException("企业id不能为空");
            }
            UacMember uacMember = uacMemberService.get(id);
            return handleResult(uacMember);
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    /**
     * 根据员工id列出所有的企业列表
     *
     * @param pageSize
     * @param pageNo
     * @return
     */
    @GetMapping("list")
    //@ApiOperation(httpMethod = "GET", value = "列出所有的企业")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "id", value = "员工ID"),
            //@ApiImplicitParam(name = "pageSize", value = "每页多少条", defaultValue = "10"),
            //@ApiImplicitParam(name = "pageNo", value = "当前要查询的页码", defaultValue = "1")})
    public RespResult findMemberList(Long id,
                                     @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                     @RequestParam(name = "pageNo", required = false, defaultValue = "0") Integer pageNo,
                                     Integer isAtcl) {
        try {
            if (null == id) {
                throw new AeotradeException("员工id不能为空");
            }
            PageList<MemberVO> list = uacMemberService.findMemberList(id, pageSize, pageNo, isAtcl, null);
            return handleResultList(Optional.ofNullable(list).orElseGet(() -> new PageList<>()));
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    /**
     * 个人工作台根据员工id列出所有的企业列表
     * @return
     */
    @GetMapping("staff/member/list")
    //@ApiOperation(httpMethod = "GET", value = "个人工作台根据员工id列出所有的企业列表")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "id", value = "员工ID")})
    public RespResult findStaffMemberList(Long id) {
        try {
            if (null == id) {
                throw new AeotradeException("员工id不能为空");
            }
            return handleResult(uacMemberService.findStaffMemberList(id));
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    /**
     * 根据员工ID列出所有的企业列表
     *
     * @param pageSize
     * @param pageNo
     * @return
     */
    @GetMapping("lists")
    //@ApiOperation(httpMethod = "GET", value = "列出所有的企业")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "id", value = "员工ID"),
            //@ApiImplicitParam(name = "vipTypeId", value = "入驻类型id"),
            //@ApiImplicitParam(name = "pageSize", value = "每页多少条", defaultValue = "100"),
            //@ApiImplicitParam(name = "pageNo", value = "当前要查询的页码", defaultValue = "1")})
    public RespResult findMemberLists(Long id,
                                      @RequestParam(name = "pageSize", required = false, defaultValue = "100") Integer pageSize,
                                      @RequestParam(name = "pageNo", required = false, defaultValue = "0") Integer pageNo,
                                      Long vipTypeId) {
        try {
            if (null == id) {
                throw new AeotradeException("员工id不能为空");
            }
            PageList<MemberVO> list = uacMemberService.findMemberList(id,pageSize, pageNo, null, vipTypeId);

            if (list == null || list.getRecords() == null) {
                return handleOK();
            }

            List<MemberVO> member = list.getRecords().stream().filter(
                    vo -> vo.getKindId() != null
                            && vo.getKindId().intValue() != BizConstant.MemberKindEnum.VISITOR_KINDID.getValue().intValue()
                            && vo.getKindId().intValue() != BizConstant.MemberKindEnum.OPERATOR.getValue().intValue()).collect(Collectors.toList());
           /* for (MemberVO memberVO : member) {
                RespResult respResult = mamberFeign.loginMessage(memberVO.getId(), 1);
                System.out.println("列出所有企业调用返回" +respResult);
            }*/
            return handleResult(member);
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    /**
     * 切换企业
     *
     * @param memberId
     * @return
     */
    @GetMapping("switch/member")
    //@ApiOperation(httpMethod = "GET", value = "切换企业")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "id", value = "当前登录员工id")
    // @ApiImplicitParam(name = "id", value = "要切换的企业id")})
    public RespResult switchMember(Long id, Long memberId) {

        try {
            if (null == memberId || null == id) {
                throw new AeotradeException("企业id不能为空");
            }
            uacStaffService.switchMember(id, memberId);
            return handleOK();
        } catch (Exception e) {
            return handleFail(e);
        }
    }


    /**
     * 根据企业id查询企业信息
     *
     * @param memberId
     * @return
     */
    @GetMapping("member")
    //@ApiOperation(httpMethod = "GET", value = "根据memberId获取企业信息")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "memberId", value = "企业ID")})
    public RespResult getMember(Long memberId) {
        try {
            if (null == memberId) {
                throw new AeotradeException("企业id不能为空");
            }
            MemberVO vo = uacMemberService.getMember(memberId);
            return handleResult(Optional.ofNullable(vo).orElseGet(() -> new MemberVO()));
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    /**
     * 添加企业
     *
     * @param
     * @return
     */
    @PostMapping("add/member")
    //@ApiOperation(httpMethod = "POST", value = "添加企业")
    public RespResult insertMember(@RequestBody UacMemberDto uacMemberDto) {
        try {
            if (uacMemberDto.getId() == null) {
                throw new AeotradeException("当前登录企业 员工id不能为空");
            }
            if (StringUtils.isEmpty(uacMemberDto.getUscCode())) {
                throw new AeotradeException("社会信用代码不能为空");
            }
            if (null == uacMemberDto.getVipTypeId()) {
                throw new AeotradeException("企业角色不能为空");
            }
            if (StringUtils.isEmpty(uacMemberDto.getMemberName())) {
                throw new AeotradeException("企业名称不能为空");
            }
            checkVerificationCode(uacMemberDto.getStasfTel(), uacMemberDto.getCode());
            UacMember uacMember = uacMemberService.insertMember(uacMemberDto);
            if (uacMemberDto.getVipTypeId() != null) {
                RespResult respResult = sgsListSave(uacMemberDto, uacMember.getId());
                if (null == respResult || respResult.getCode() != 200) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    throw new AeotradeException("注册失败,请重试");
                }
            }
            ThreadPoolUtils.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Map<String, Object> map = new HashMap<>();
                        map.put("memberId", uacMember.getId());
//                        System.out.println("进入内部线程，请求参数为：" + map);
                        if (!org.springframework.util.StringUtils.isEmpty(uaaErpUri)) {
                            String http = HttpRequestUtils.httpGet(uaaErpUri, map);
                        }
//                        System.out.println("请求返回:" + http);
                    } catch (Exception e) {
                        log.warn(e.getMessage());
                    }
                }
            });
            return handleResult(uacMember);
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    /**
     * 重新向链服务发送企业注册消息
     * @param uacStaffDto
     */
    @PostMapping("send/chain/add/member")
    public RespResult sendChainMessageAddMember(@RequestBody UacStaffDto uacStaffDto) {
        if (uacStaffDto == null){
            return handleFail("参数不能为空");
        }
        if (uacStaffDto.getMemberId() == null){
            return handleFail("memberId不能为空");
        }
        if (uacStaffDto.getStaffId() == null){
            return handleFail("staffId不能为空");
        }
        try {
            uacMemberService.sendChainMessageAddMember(uacStaffDto);
        } catch (Exception e) {
            return handleFail(e);
        }
        return handleOK();
    }

    @GetMapping("/note")
    public RespResult noteVerify(@RequestParam(value = "phone") String phone, @RequestParam(value = "code") String code) {
        try {
            checkVerificationCode(phone, code);
            Map<String, String> map = new HashMap<>();
            map.put("phone", phone);
            map.put("code", code);
            return handleResult(map);
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    /**
     * 根据memberId删除企业
     *
     * @param memberId
     * @return
     */
    @GetMapping("delete/member")
    //@ApiOperation(httpMethod = "GET", value = "根据id删除企业")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "memberId", value = "企业ID")})
    public RespResult deleteMember(Long memberId) {
        try {
            if (null == memberId) {
                throw new AeotradeException("企业id不能为空");
            }
            uacMemberService.deleteMember(memberId);
            return handleOK();
        } catch (Exception e) {
            return handleFail(e);
        }

    }

    /**
     * 根据id修改企业信息
     *
     * @param uacMember
     * @return
     */
    @PostMapping("update/member")
    //@ApiOperation(httpMethod = "POST", value = "根据id修改企业")
    public RespResult updateMember(@RequestBody UacMember uacMember) {
        try {
            if (null == uacMember.getId()) {
                throw new AeotradeException("企业id不能为空");
            }
            uacMemberService.updateMember(uacMember);
            return handleOK();
        } catch (Exception e) {
            return handleFail(e);
        }
    }


    /**
     * 银行卡认证
     *
     * @param uacMember
     * @return
     */
    @PostMapping("bankSgs")
    //@ApiOperation(httpMethod = "POST", value = "银行卡认证")
    public RespResult bankSgs(@RequestBody UacMember uacMember) {
        try {
            if (null == uacMember.getId()) {
                throw new AeotradeException("企业id不能为空");
            }
            uacMemberService.bankSgs(uacMember);

            return handleOK();
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    /**
     * 金额校验
     *
     * @param bankMoney
     * @param memberId
     * @return
     */
    @GetMapping("bank/money")
    //@ApiOperation(httpMethod = "GET", value = "校验金额")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "bankMoney", value = "银行账号收到金额数值")})
    public RespResult findBankMoney(Double bankMoney, Long memberId) {

        try {
            if (null == bankMoney && null == memberId) {
                throw new AeotradeException("金额或memberId不能为空");
            }
            BigDecimal num = new BigDecimal(Double.toString(bankMoney));
            BigDecimal number100 = new BigDecimal(Double.toString(100));
            double value = num.multiply(number100).doubleValue();
            BigDecimal v = new BigDecimal(Double.toString(value));
            int i = v.intValue();
//            System.out.println(i);

            Map<String, Object> bank = uacMemberService.findBankMoney(i, memberId);
            return RespResultMapper.wrap(RespResult.SUCCESS_CODE, "", bank);
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    /**
     * 企业实名认证
     *
     * @param uacMember
     * @return
     */
    @PostMapping("CompanySgs")
    //@ApiOperation(httpMethod = "POST", value = "企业实名认证")
    public RespResult CompanySgs(@RequestBody UacMember uacMember) {
        try {
            if (null == uacMember.getId()) {
                throw new AeotradeException("企业id不能为空");
            }
            uacMemberService.CompanySgs(uacMember);
            return handleOK();
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    /**
     * 列出企业的所有员工
     *
     * @param memberId
     * @return
     */
    @GetMapping("staff")
    //@ApiOperation(httpMethod = "GET", value = "根据企业id列出企业的所有员工")
    //@ApiImplicitParams({
            //@ApiImplicitParam(name = "pageSize", value = "每页多少条", defaultValue = "10"),
            //@ApiImplicitParam(name = "pageNo", value = "当前要查询的页码", defaultValue = "1")})
    public RespResult findStaff(Long memberId,
                                @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                @RequestParam(name = "pageNo", required = false, defaultValue = "0") Integer pageNo) {
        try {
            if (null == memberId) {
                throw new AeotradeException("memberId不能为空");
            }
            PageList<StaffVO> list = uacMemberService.findStaff(memberId, pageSize, pageNo);
            return handleResultList(Optional.ofNullable(list).orElseGet(() -> new PageList<>()));
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    /**
     * 当前登录员工所有企业下的员工
     *
     * @param staffId
     * @return
     */
    @GetMapping("staf/list")
    //@ApiOperation(httpMethod = "GET", value = "当前登录员工所有企业下的员工")
    //@ApiImplicitParams({
            //@ApiImplicitParam(name = "pageSize", value = "每页多少条", defaultValue = "10"),
            //@ApiImplicitParam(name = "pageNo", value = "当前要查询的页码", defaultValue = "1")})
    public RespResult findStafList(Long staffId,
                                   @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                   @RequestParam(name = "pageNo", required = false, defaultValue = "0") Integer pageNo) {
        try {
            if (null == staffId) {
                throw new AeotradeException("staffId不能为空");
            }
            List<StaffVO> list = uacMemberService.findStafList(staffId, pageSize, pageNo);
            return handleResult(Optional.ofNullable(list).orElseGet(() -> new ArrayList<>()));
        } catch (Exception e) {
            return handleFail(e);
        }
    }
    /**
     * 根据员工id删除员工
     *
     * @return
     */
    @GetMapping("delete/staff")
    //@ApiOperation(httpMethod = "GET", value = "根据员工id删除员工")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "memberId", value = "企业id"),
            //@ApiImplicitParam(name = "staffId", value = "员工id")})
    public RespResult deleteStaff(Long memberId, Long staffId,@RequestHeader("Authorization") String token) {
        try {
            if (null == memberId || null == staffId) {
                throw new AeotradeException("员工Id不能为空");
            }
            uacStaffService.deleteStaff(memberId, staffId,token);
            return handleOK();
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    /**
     * 判断员工是否可以退出组织
     * @return
     */
    @GetMapping("quit/member")
    //@ApiOperation(httpMethod = "GET", value = "判断员工是否可以退出组织")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "memberId", value = "企业id"),
            //@ApiImplicitParam(name = "staffId", value = "员工id")})
    public RespResult quitMember(Long memberId, Long staffId) {
        try {
            if (null == memberId || null == staffId) {
                throw new AeotradeException("员工Id不能为空");
            }
            int quitMember = uacStaffService.quitMember(memberId, staffId);
            return handleResult(quitMember);
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    /**
     * 添加员工
     *
     * @param staffDto
     * @return
     */
    @PostMapping("insert/staff")
    //@ApiOperation(httpMethod = "POST", value = "添加员工")
    public RespResult insertStaff(@RequestBody StaffDto staffDto) {
        try {

            Long inviteId = uacStaffService.insertStaff(staffDto);
            return handleResult(inviteId);
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    ;

    /**
     * 编辑员工
     *
     * @param staffDto
     * @return
     */
    @PostMapping("update/staff")
    //@ApiOperation(httpMethod = "POST", value = "编辑员工(企业管理编辑员时memberId必传)")
    public RespResult updateStaff(@RequestBody StaffDto staffDto) {
        try {
            uacStaffService.updateStaff(staffDto);
            return handleOK();
        } catch (Exception e) {
            return handleFail(e);
        }

    }

    ;

    /**
     * 修改回显
     *
     * @param id
     * @return
     */
    @GetMapping("update/staff")
    //@ApiOperation(httpMethod = "GET", value = "员工修改回显")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "id", value = "员工id")})
    public RespResult findStaffById(Long id) {
        try {
            if (null == id) {
                throw new AeotradeException("员工Id不能为空");
            }
            MemberStaffVO vo = uacStaffService.findStaffById(id);
            return handleResult(Optional.ofNullable(vo).orElseGet(() -> new MemberStaffVO()));
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    /**
     * 查询通讯录
     *
     * @param staffId
     * @param pageSize
     * @param pageNo
     * @return
     */
    @GetMapping("staff/list")
    //@ApiOperation(httpMethod = "GET", value = "查询通讯录")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "staffId", value = "当前登录staffId"),
            //@ApiImplicitParam(name = "pageSize", value = "每页多少条", defaultValue = "10"),
            //@ApiImplicitParam(name = "pageNo", value = "当前要查询的页码", defaultValue = "1")})
    public RespResult findStaffId(Long staffId,
                                  @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                  @RequestParam(name = "pageNo", required = false, defaultValue = "0") Integer pageNo) {
        try {
            if (null == staffId) {
                throw new AeotradeException("staffId不能为空");
            }

            PageList<StaffIDVO> list = uacMemberService.findStaffId(staffId, pageSize, pageNo);
            return handleResultList(Optional.ofNullable(list).orElseGet(() -> new PageList<>()));
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    /**
     * 短信验证码校验
     *
     * @param phone
     * @param code
     * @throws Exception
     */
    private void checkVerificationCode(String phone, String code) throws Exception {
        //判断验证码格式
        if (!NumberUtils.testPhone(phone)) {
            throw new RuntimeException("请输入正确的手机号");
        }
        //判断数据库存的验证码是否正确
        String code1 = redisTemplate.opsForValue().get(AeoConstant.SMSREDIS_KEY + phone);
        if (code1 == null) {
            throw new RuntimeException("验证码不存在");
        }
        //验证的过期时间
        Long expire = redisTemplate.opsForValue().getOperations().getExpire(AeoConstant.SMSREDIS_KEY + phone);
        if (expire <= 0) {
            throw new RuntimeException(
                    "验证码已过期");
        }
        if (!code1.equals(code)) {
            throw new RuntimeException("验证码错误");
        }
//        redisTemplate.delete(AeoConstant.SMSREDIS_KEY + phone);
    }

    /**
     * 手机端回显
     *
     * @param staffId
     * @return
     */
    @GetMapping("tel/staff")
    //@ApiOperation(httpMethod = "GET", value = "手机端员工修改回显")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "staffId", value = "邀请人员工id"),
            //@ApiImplicitParam(name = "inviteId", value = "被邀请人员工id")})
    public RespResult findStaffByIdH5(Long staffId, Long inviteId) {
        try {
            if (null == staffId && null == inviteId) {
                throw new AeotradeException("主键Id不能为空");
            }
            MemberStaffTelVO vo = uacStaffService.findStaffByIdH5(staffId, inviteId);
            return handleResult(Optional.ofNullable(vo).orElseGet(() -> new MemberStaffTelVO()));
        } catch (Exception e) {
            return handleFail(e);
        }
    }


    /**
     * 手机端拒绝接受邀请
     *
     * @param id
     * @return
     */
    @GetMapping("tel/reject")
    //@ApiOperation(httpMethod = "GET", value = "手机端拒绝接受邀请")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "id", value = "员工id")})
    public RespResult rejectInvite(Long id, Long memberId) {
        try {
            if (null == id) {
                throw new AeotradeException("员工Id不能为空");
            }
            uacStaffService.rejectInvite(id);
            return handleOK();
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    /**
     * 用户接受邀请
     *
     * @param wxTokenDto
     * @return
     */
    @PostMapping("save/user")
    //@ApiOperation(httpMethod = "GET", value = "用户接受邀请")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "id", value = "员工id")})
    public RespResult saveUser(@RequestBody WxTokenDto wxTokenDto, @RequestBody UacStaff uacStaff) {
        try {
            if (null == wxTokenDto) {
                throw new AeotradeException("值不能为空");
            }
            if (null == wxTokenDto.getId()) {
                throw new AeotradeException("员工id不能为空");
            }
            uacStaffService.saveUser(wxTokenDto, uacStaff);
            return handleOK();
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    // feign 有在使用
    //@ApiOperation(httpMethod = "GET", value = "子管理员微信信息列表")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "staffId", value = "员工id")})
    @GetMapping("subAdmins")
    public RespResult subAdminList(Long staffId) {
        return handleResult(uacStaffService.subAdminList(staffId));
    }

    //@ApiOperation(httpMethod = "POST", value = "删除子管理员")
    @PostMapping("delSubAdmin")
    @Valid
    //@ApiParam(name = "子管理员对象")
    public RespResult delSubAdmin(@RequestBody SubAdminDto subAdminDto) {
        try {
            uacStaffService.delSubAdmin(subAdminDto.getMemberId(), subAdminDto.getStaffId());
        } catch (Exception e) {
            return handleFail("删除失败");
        }
        return handleOK();
    }

    @GetMapping(value = "details")
    public RespResult findMenmberDtisl(Long staffId, Long memberId, Integer type, HttpServletResponse response) throws Exception {

        try {
            if(type==2){
                UacMember uacMember = uacMemberService.getById(memberId);
                List<UacMemberStaff> list = uacMemberStaffService.lambdaQuery()
                        .eq(UacMemberStaff::getMemberId, memberId).eq(UacMemberStaff::getStaffId, staffId).list();
                if(uacMember.getStaffId()!=staffId && list.size()==0){
                    UacStaff uacStaff = uacStaffService.getById(staffId);
                    LambdaUpdateWrapper<UacStaff> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.set(UacStaff::getLastMemberId,null);
                    updateWrapper.set(UacStaff::getChannelColumnsId,0L);
                    updateWrapper.set(UacStaff::getLastWorkbenchId, 1L);
                    updateWrapper.eq(UacStaff::getId,staffId);
                    uacStaffService.update(uacStaff,updateWrapper);
                    FebsUtil.makeJsonResponse(response, 401, new FebsResponse().put("code","401").message("您已被最近登录的企业移除,请您重新登录"));
                    return null;
                }
            }
            return handleResult(uacMemberService.findMemberDetails(staffId, memberId, type));
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    /**
     * 判断企业实名认证状态并返回申请时间
     *
     * @param
     * @return
     */
    @GetMapping("apptime")
    //@ApiOperation(httpMethod = "GET", value = "判断企业实名状态并返回申请时间")
    public Boolean findTime(Long memberId) {
        if (memberId == null) {
            throw new AeotradeException("企业id不能为空");
        }
        return uacMemberService.findTime(memberId);
    }

    /**
     * 判断企业实名认证状态并返回申请时间
     *
     * @param
     * @return
     */
    @GetMapping("isAouth")
    //@ApiOperation(httpMethod = "GET", value = "判断企业实名状态并返回申请时间")
    public RespResult isAouth(Long memberId) {
        if (memberId == null) {
            throw new AeotradeException("企业id不能为空");
        }
        return handleResult(uacMemberService.findTime(memberId));
    }

    private RespResult sgsListSave(UacMemberDto uacMemberDto, Long instrer) throws Exception {
        if (uacMemberDto.getVipTypeId() != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("createdBy", uacMemberDto.getStaffName());
            map.put("createdById", uacMemberDto.getId());
            map.put("memberId", instrer);
            map.put("memberName", uacMemberDto.getMemberName());
            map.put("uscc", uacMemberDto.getUscCode());
            map.put("vipTypeId", uacMemberDto.getVipTypeId());
            String http = HttpRequestUtils.httpPost(gatewayUrl+"/mam/aptitude/save", map);
            return JSONObject.parseObject(http, RespResult.class);
        }

        return null;
    }

    /**
     * 管理员直接添加员工到企业
     */
    @PostMapping("/admin/add/staff")
    @Transactional(rollbackFor = Exception.class)
    public RespResult AdminAddStaff(@RequestBody AdminAddStaffParam adminAddStaffParam) {
        //校验参数
        if (adminAddStaffParam.getMemberId() == null) {
            return handleFail("企业标识不能为空");
        }
        if (adminAddStaffParam.getStaffId() == null) {
            return handleFail("当前操作者标识不能为空");
        }
        if (adminAddStaffParam.getRoleId() == null) {
            return handleFail("角色标识不能为空");
        }
        if (adminAddStaffParam.getDeptId() == null) {
            return handleFail("部门标识不能为空");
        }
        if (org.springframework.util.StringUtils.isEmpty(adminAddStaffParam.getName())) {
            return handleFail("姓名不能为空");
        }
        if (org.springframework.util.StringUtils.isEmpty(adminAddStaffParam.getMobile())) {
            return handleFail("手机号不能为空");
        }
        UacAdmin uacAdmin = null;
        UacStaff uacStaff = null;
        UacMember uacMember = uacMemberService.getById(adminAddStaffParam.getMemberId());
        //根据手机号查询是否存在
        List<UacAdmin> uacAdminList = uacAdminService.lambdaQuery().eq(UacAdmin::getMobile, adminAddStaffParam.getMobile()).list();
        if (uacAdminList.size() > 0) {
            uacAdmin = uacAdminList.get(0);
            uacStaff = uacStaffService.getById(uacAdmin.getStaffId());
            //存在，UacMemberStaff直接添加关联
            List<UacMemberStaff> uacMemberStaffs = uacMemberStaffService.lambdaQuery()
                    .eq(UacMemberStaff::getMemberId, adminAddStaffParam.getMemberId())
                    .eq(UacMemberStaff::getStaffId, uacAdmin.getStaffId())
                    .list();
            if (uacMemberStaffs.size() == 0) {
                UacMemberStaff uacMemberStaff = new UacMemberStaff();
                uacMemberStaff.setMemberId(Long.valueOf(adminAddStaffParam.getMemberId()));
                uacMemberStaff.setStaffId(uacAdmin.getStaffId());
                uacMemberStaff.setCreatedTime(LocalDateTime.now());
                uacMemberStaffService.save(uacMemberStaff);
            }
        } else {
            //不存在，添加账户再关联
            //添加staff信息
            uacStaff = new UacStaff();
            uacStaff.setTel(adminAddStaffParam.getMobile());
            uacStaff.setStaffName(adminAddStaffParam.getName());
            uacStaff.setMemberId(Long.valueOf(adminAddStaffParam.getMemberId()));
            uacStaff.setCreatedTime(LocalDateTime.now());
            uacStaff.setStatus(BizConstant.DEL_FLAG_NO);
            uacStaff.setStaffName(adminAddStaffParam.getName());
            uacStaff.setSgsStatus(0);
            uacStaff.setStaffType(BizConstant.StaffTypeEnum.PERSONAL.getValue());
            uacStaff.setSourceMark("管理员直接添加员工到企业");
            uacStaff.setLastWorkbenchId(1L);
            uacStaff.setChannelColumnsId(0L);
            uacStaff.setCreatedBy(adminAddStaffParam.getStaffId().toString());
            uacStaff.setStaffType(BizConstant.StaffTypeEnum.ENTERPRISE.getValue());
            uacStaff.setAuthStatus(0);
            uacStaff.setIsLogin(0);
            uacStaff.setRevision(0);
            //根据当前员工ID查询工作台展示信息
            UacStaff staff = uacStaffService.getById(adminAddStaffParam.getStaffId());
            if (staff != null && staff.getLastWorkbenchId() != null) {
                uacStaff.setLastWorkbenchId(staff.getLastWorkbenchId());
                uacStaff.setChannelColumnsId(staff.getChannelColumnsId());
            }
            uacStaff.setLastMemberId(Long.valueOf(adminAddStaffParam.getMemberId()));
            uacStaffService.save(uacStaff);

            //与当前企业建立关联
            UacMemberStaff uacMemberStaff = new UacMemberStaff();
            uacMemberStaff.setMemberId(Long.valueOf(adminAddStaffParam.getMemberId()));
            uacMemberStaff.setStaffId(uacStaff.getId());
            uacMemberStaff.setCreatedTime(LocalDateTime.now());
            uacMemberStaff.setIsAdmin(0);
            uacMemberStaffService.save(uacMemberStaff);

            //添加登录账号
            uacAdmin = new UacAdmin();
            uacAdmin.setId(null);
            uacAdmin.setCreateTime(LocalDateTime.now());
            uacAdmin.setDel((byte) 0);
            uacAdmin.setStatus(1);
            uacAdmin.setIsTab(1);
            uacAdmin.setMobile(adminAddStaffParam.getMobile());
            uacAdmin.setStaffId(uacStaff.getId());
            uacAdmin.setNickName(adminAddStaffParam.getName());
            uacAdminService.save(uacAdmin);

        }

        //建立角色关联
        for (String rid : adminAddStaffParam.getRoleId()) {
            UacAdminRole uacAdminRole = new UacAdminRole();
            uacAdminRole.setCreateTime(LocalDateTime.now());
            uacAdminRole.setAdminId(uacAdmin.getId());
            uacAdminRole.setRoleId(Long.valueOf(rid));
            uacAdminRole.setMemberId(Long.valueOf(adminAddStaffParam.getMemberId()));
            UacRole uacRole = uacRoleService.getById(rid);
            if (uacRole != null && uacRole.getPlatformId() != null) {
                uacAdminRole.setOrgi(uacRole.getPlatformId().toString());
            }
            List<UacAdminRole> uacAdminRoleList = uacAdminRoleService.lambdaQuery().eq(UacAdminRole::getAdminId, uacAdmin.getId())
                    .eq(UacAdminRole::getRoleId, Long.valueOf(rid)).list();
            if (uacAdminRoleList.size()==0) {
                uacAdminRoleService.save(uacAdminRole);
            }
        }

        //建立部门关联
        for (String did : adminAddStaffParam.getDeptId()) {
            UacDeptStaff uacDeptStaff = new UacDeptStaff();
            uacDeptStaff.setStaffId(uacStaff.getId());
            uacDeptStaff.setDeptId(Long.valueOf(did));
            List<UacDeptStaff> uacDeptStaffList = uacDeptStaffService.lambdaQuery().eq(UacDeptStaff::getStaffId, uacStaff.getId())
                    .eq(UacDeptStaff::getDeptId, Long.valueOf(did)).list();
            if (uacDeptStaffList.size()==0) {
                uacDeptStaffService.save(uacDeptStaff);
            }
        }

        //为员工生成证书
        AddStaffEvent addStaffEvent = new AddStaffEvent(this, uacMember, uacStaff,"员工");
        eventPublisher.publishEvent(addStaffEvent);

        return handleOK();
    }
}
