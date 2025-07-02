package com.aeotrade.provider.admin.controller;


import com.aeotrade.base.constant.SgsConstant;
import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.admin.entiy.SgsApply;
import com.aeotrade.provider.admin.entiy.SgsCertInfo;
import com.aeotrade.provider.admin.entiy.SgsConfiguration;
import com.aeotrade.provider.admin.entiy.UacStaff;
import com.aeotrade.provider.admin.service.impl.*;
import com.aeotrade.provider.admin.uacVo.*;

import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.PageList;
import com.aeotrade.suppot.RespResult;
import com.aeotrade.suppot.RespResultMapper;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * UawResourceController  银行类型controller
 */
@RestController
@RequestMapping("/sgs/")
////@Api(tags = "认证接口")
@Slf4j
public class SgsController extends BaseController {

    @Autowired
    private SgsConfigurationServiceImpl sgsListService;
    @Autowired
    private SgsBankInfoServiceImpl sgsBankService;
    @Autowired
    private SgsSwInfoServiceImpl sgsMemberOnlyService;
    @Autowired
    private SgsCertInfoServiceImpl sgsMemberService;
    @Autowired
    private UacStaffServiceImpl uacStaffService;
    @Autowired
    private RedisTemplate<String,String> stringStringRedisTemplate;

    @GetMapping("list/all")
//    //@ApiOperation(httpMethod = "GET", value = "根据类型查询认证列表,type 0个人,1企业")
//    //@ApiImplicitParams({//@ApiImplicitParam(name = "type", value = "type 0个人,1企业")})
    public RespResult findListSort(Integer type) {
        try {
            if (null == type) {
                throw new AeotradeException("type不能为空");
            }

            List<SgsConfiguration> list = sgsListService.findListSort(type);

            return handleResult(list.stream().map(sc -> {
                if (sc.getAuthToChain() != null && sc.getAuthToChain() > 0 && sc.getIssuerConfig() != null) {
                    SgsConfigurationVO sgsConfigurationVO = new SgsConfigurationVO();
                    BeanUtils.copyProperties(sc, sgsConfigurationVO);
                    IssuerConfigVO issuerConfigVO = JSONObject.parseObject(sc.getIssuerConfig(), IssuerConfigVO.class);
                    BeanUtils.copyProperties(issuerConfigVO, sgsConfigurationVO);
                    return sgsConfigurationVO;
                } else {
                    return sc;
                }
            }).collect(Collectors.toList()));
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    @GetMapping("list/query")
    //@ApiOperation(httpMethod = "GET", value = "根据ID查询详情(修改回显)")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "id", value = "主键ID")})
    public RespResult listGet(Long id) {
        try {
            if (null == id) {
                throw new AeotradeException("id不能为空");
            }
            SgsConfiguration sc = sgsListService.getById(id);
            if (sc.getAuthToChain() != null && sc.getAuthToChain() > 0) {
                SgsConfigurationVO sgsConfigurationVO = new SgsConfigurationVO();
                BeanUtils.copyProperties(sc, sgsConfigurationVO);
                IssuerConfigVO issuerConfigVO = JSONObject.parseObject(sc.getIssuerConfig(), IssuerConfigVO.class);
                BeanUtils.copyProperties(issuerConfigVO, sgsConfigurationVO);
                return handleResult(sgsConfigurationVO);
            }
            return handleResult(Optional.ofNullable(sc).orElseGet(SgsConfiguration::new));
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    @GetMapping("list/del")
    //@ApiOperation(httpMethod = "GET", value = "根据ID删除")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "id", value = "主键ID")})
    public RespResult listDel(Long id) {
        try {
            if (null == id) {
                throw new AeotradeException("id不能为空");
            }
            SgsConfiguration del = new SgsConfiguration();
            del.setId(id);
            del.setStatus(1);
            sgsListService.updateById(del);
            return handleOK();
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    @PostMapping("list/save")
    //@ApiOperation(httpMethod = "POST", value = "个人/企业,认证类型添加")
    public RespResult sgsListSave(@RequestBody SgsConfigurationVO sgsConfigurationVO) {
        try {
            if (sgsConfigurationVO.getUserType() == null) {
                throw new AeotradeException("type不能为空");
            }
            SgsConfiguration sgsConfiguration = new SgsConfiguration();
            BeanUtils.copyProperties(sgsConfigurationVO, sgsConfiguration);
            sgsConfiguration.setStatus(0);
            sgsConfiguration.setSgsStatus(1);
            sgsConfiguration.setRevision(0);
            sgsConfiguration.setCreatedTime(LocalDateTime.now());
            sgsConfiguration.setUpdatedTime(LocalDateTime.now());
            sgsConfiguration.setSgsType(sgsListService.lambdaQuery().list().size() + 1);
            //链上认证机构配置
            if (sgsConfiguration.getAuthToChain() != null && sgsConfiguration.getAuthToChain() > 0) {
                if (sgsConfiguration.getIssuerConfig() == null) {
                    return handleFail("链上认证机构配置必填");
                }
                sgsConfiguration.setIssuerConfig(JSONObject.toJSONString(
                        new IssuerConfigVO(sgsConfigurationVO.getIssuerId(), sgsConfigurationVO.getCredentialName(), sgsConfigurationVO.getVcTemplateId(), sgsConfigurationVO.getIssuerName())
                ));
            }
            sgsListService.save(sgsConfiguration);

            return handleResult(sgsConfigurationVO);
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    @PostMapping("list/update")
    //@ApiOperation(httpMethod = "POST", value = "个人/企业,认证类型修改")
    public RespResult sgsListUpdate(@RequestBody SgsConfigurationVO sgsConfigurationVO) {
        try {
            if (null == sgsConfigurationVO) {
                throw new AeotradeException("值不能为空");
            }
            if (null == sgsConfigurationVO.getId()) {
                throw new AeotradeException("ID不能为空");
            }
            SgsConfiguration sgsConfiguration=new SgsConfiguration();
            BeanUtils.copyProperties(sgsConfigurationVO,sgsConfiguration);
            sgsConfiguration.setRevision(1);
            sgsConfiguration.setUpdatedTime(LocalDateTime.now());
            if (sgsConfigurationVO.getAuthToChain() != null && sgsConfigurationVO.getAuthToChain() > 0
                    && sgsConfigurationVO.getIssuerId() != null) {
                sgsConfiguration.setIssuerConfig(JSONObject.toJSONString(
                        new IssuerConfigVO(sgsConfigurationVO.getIssuerId(), sgsConfigurationVO.getCredentialName(),
                                sgsConfigurationVO.getVcTemplateId(), sgsConfigurationVO.getIssuerName())
                ));
            }
            sgsListService.updateById(sgsConfiguration);
            return handleResult(sgsConfigurationVO);
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    @GetMapping("list/all/front")
    //@ApiOperation(httpMethod = "GET", value = "前台 根据类型查询认证列表,type 0个人,1企业")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "type", value = "type 0个人,1企业")
    //@ApiImplicitParam(name = "id", value = "企业ID/员工ID")})
    public RespResult findListSort(Long id, Integer type) {
        try {
            if (null == id) {
                throw new AeotradeException("memberId不能为空");
            }

            List<SgsConfigurationVO> list = sgsListService.findListStatus(id, type);
            return handleResult(Optional.ofNullable(list).orElseGet(ArrayList::new));
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    //@ApiOperation(httpMethod = "POST", value = "银行卡认证")
    @PostMapping("bank/save")
    public RespResult sgsBankSave(@RequestBody SgsBankDto sgsBankDto) {
        try {
            if (sgsBankDto == null) {
                throw new AeotradeException("参数不能为空");
            }
            String sgs = stringStringRedisTemplate.opsForValue().get("AEROTRADE_BANK_SGS:" + sgsBankDto.getMemberId());
            if(StringUtils.isEmpty(sgs) || !sgs.equals("2")){
                SgsBankDto sgsBankDto1 = sgsBankService.sgsBankSave(sgsBankDto);
                stringStringRedisTemplate.opsForValue().set("AEROTRADE_BANK_SGS:"+sgsBankDto.getMemberId(),StringUtils.isEmpty(sgs)?"1":"2",
                        getRemainSecondsOneDay(new Date(System.currentTimeMillis())),TimeUnit.SECONDS);
                return handleResult(sgsBankDto1);
            }
            throw new AeotradeException("每天只可发起2次银行卡对公账户认证，请明天再发起认证");
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    @GetMapping("member/query")
    //@ApiOperation(httpMethod = "GET", value = "企业银行认证信息回显")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "id", value = "企业ID")})
    public RespResult memberQuery(Long id) {
        try {
            if (null == id) {
                new AeotradeException("Id不能为空");
            }

            MemberSgsVO list = sgsListService.memberQuery(id);
            return handleResult(Optional.ofNullable(list).orElseGet(() -> new MemberSgsVO()));
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
    public RespResult findBankMoney(String bankMoney, Long memberId
                                    ,String vcTemplateId,
                                    String credentialName,
                                    String issuerId,
                                    String issuerName,
                                    String staffId,
                                    String sgsConfigId
    ) {

        try {
            if (null == bankMoney && null == memberId) {
                throw new AeotradeException("金额或memberId不能为空");
            }
            Map<String, Object> bank = sgsBankService.findBankMoney(bankMoney, memberId,vcTemplateId, credentialName, issuerId, issuerName, staffId,sgsConfigId);
            return RespResultMapper.wrap(RespResult.SUCCESS_CODE, "认证结果",bank);
        } catch (Exception e) {
            return RespResultMapper.wrap(RespResult.SUCCESS_CODE,"认证结果",500);
        }
    }

    /**
     * 补充链上银行卡认证
     */
    @PostMapping("/supplementary/bankMoney/auth")
    public RespResult supplementaryBankMoneyAuthentication(String vcTemplateId,
                                                           String credentialName,
                                                           String issuerId,
                                                           String issuerName,
                                                           String sgsConfigId,
                                                           Integer sgsType,
                                                           String msgType){
        log.info("补充链上银行卡认证开始");
        sgsBankService.supplementaryBankMoneyAuthentication(vcTemplateId,credentialName,issuerId,issuerName,sgsConfigId,sgsType,msgType);
        log.info("补充链上银行卡认证结束");
        return handleResult("完成发送银行卡认证信息到生成证书队列中");
    }





    //@ApiOperation(httpMethod = "GET", value = "认证管理企业实名认证申请列表")
    @GetMapping("member/sgs/list")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "pageSize", value = "每页多少条", defaultValue = "10"),
            //@ApiImplicitParam(name = "pageNo", value = "当前要查询的页码", defaultValue = "1")})
    public RespResult memberSgsList(Integer pageSize, Integer pageNo, String value) {
        try {
            PageList<SgsApply> sgsApplies = sgsMemberService.memberSgsLisst(pageSize, pageNo, value, SgsConstant.SgsStatus.WEIRENZ.getValue());
            return handleResultList(Optional.ofNullable(sgsApplies).orElseGet(() -> new PageList<SgsApply>()));
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    //@ApiOperation(httpMethod = "GET", value = "认证管理企业实名认证申请通过列表")
    @GetMapping("member/pass/list")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "pageSize", value = "每页多少条", defaultValue = "10"),
            //@ApiImplicitParam(name = "pageNo", value = "当前要查询的页码", defaultValue = "1"),
            //@ApiImplicitParam(name = "startTime", value = "开始时间", defaultValue = "2000-01-01 00:00:00"),
            //@ApiImplicitParam(name = "endTime", value = "结束时间", defaultValue = "3000-01-01 00:00:00")})
    public RespResult memberPassList(Integer pageSize, Integer pageNo, String value, @RequestParam String startTime, @RequestParam String endTime) {
        try {
            PageList<SgsApply> sgsBankDto1 = sgsMemberService.memberPassLisst(pageSize, pageNo, value,
                    SgsConstant.SgsStatus.TONGGUO.getValue(), Timestamp.valueOf(startTime), Timestamp.valueOf(endTime));
            return handleResultList(Optional.ofNullable(sgsBankDto1).orElseGet(() -> new PageList<>()));
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    //@ApiOperation(httpMethod = "GET", value = "企业实名认证通过认证")
    @GetMapping("member/idc/pass")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "sgsStatus", value = "2:审核通过 , 3:不通过")})
    public RespResult memberIdPass(Long memberId, Integer sgsStatus,String remark,Integer sgsType) {
        try {
            if (memberId == null) {
                throw new AeotradeException("memberId不能为空");
            }
            sgsMemberService.memberIdPass( memberId, sgsStatus, remark,sgsType);
            return handleOK();
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    @GetMapping("member/find/info")
    //@ApiOperation(httpMethod = "GET", value = "查询企业认证详情")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "id", value = "企业ID")})
    public RespResult memberfindInfo(Long id,Integer sgsType) {
        try {
            if (null == id) {
                new AeotradeException("Id不能为空");
            }
            return handleResult(sgsMemberService.memberfindInfo(id,sgsType));
        } catch (Exception e) {
            return handleFail(e);
        }
    }


    //@ApiOperation(httpMethod = "GET", value = "个人用户列表")
    @GetMapping("staff/list")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "vlaue", value = "用户名/手机号"),
            //@ApiImplicitParam(name = "pageSize", value = "每页多少条", defaultValue = "10"),
            //@ApiImplicitParam(name = "pageNo", value = "当前要查询的页码", defaultValue = "1")})
    public RespResult staffList(String vlaue, Integer pageSize, Integer pageNo, String sourceMark) {
        try {
            if (StringUtils.isEmpty(sourceMark)) {
                sourceMark = "";
            }
            if (sourceMark.equals("0")) {
                sourceMark = "慧贸OSPC端";
            }
            if (sourceMark.equals("1")) {
                sourceMark = "北京单一窗口PC端";
            }
            PageList<UacStaff> allPage = uacStaffService.findAllPage(pageSize, pageNo, vlaue, sourceMark);
            return handleResultList(allPage);
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    //@ApiOperation(httpMethod = "GET", value = "企业用户列表")
    @GetMapping("me/list")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "vlaue", value = "用户名/手机号"),
            //@ApiImplicitParam(name = "pageSize", value = "每页多少条", defaultValue = "10"),
            //@ApiImplicitParam(name = "pageNo", value = "当前要查询的页码", defaultValue = "1")})
    public RespResult memberListPage(String vlaue, Integer pageSize, Integer pageNo, String remark, String startTime, String endTime) {
        try {
            if (StringUtils.isEmpty(remark)) {
                remark = null;
            }
            if (StringUtils.isEmpty(startTime)) {
                startTime = null;
            }
            if (StringUtils.isEmpty(endTime)) {
                endTime = null;
            }
            PageList<UacMemberVO> allPage = sgsMemberService.memberListPage(pageSize,pageNo, vlaue, remark, startTime, endTime);
            return handleResultList(Optional.ofNullable(allPage).orElseGet(() -> new PageList<>()));
        } catch (Exception e) {
            return handleFail(e);
        }
    }


    //@ApiOperation(httpMethod = "GET", value = "企业重新申请实名认证")
    @GetMapping("member/update/sgs/status")
    public RespResult memberUpdateSgsStatus(Long memberId) {
        try {
            if (memberId == null) {
                throw new AeotradeException("memberId不能为空");
            }
            sgsMemberService.memberUpdateSgsStatus(memberId);
            return handleOK();
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    public static Integer getRemainSecondsOneDay(Date currentDate) {
        LocalDateTime midnight = LocalDateTime.ofInstant(currentDate.toInstant(),
                ZoneId.systemDefault()).plusDays(1).withHour(0).withMinute(0)
                .withSecond(0).withNano(0);
        LocalDateTime currentDateTime = LocalDateTime.ofInstant(currentDate.toInstant(),
                ZoneId.systemDefault());
        long seconds = ChronoUnit.SECONDS.between(currentDateTime, midnight);
        return (int) seconds;
    }
}