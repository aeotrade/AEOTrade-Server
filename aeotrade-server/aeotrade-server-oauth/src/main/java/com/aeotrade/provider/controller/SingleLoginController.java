package com.aeotrade.provider.controller;

import com.aeotrade.base.constant.AeoConstant;
import com.aeotrade.base.constant.SgsConstant;
import com.aeotrade.base.model.ChainApplyCredential;
import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.mapper.SgsConfigurationMapper;
import com.aeotrade.provider.model.SgsConfiguration;
import com.aeotrade.provider.service.impl.SingleLoginService;
import com.aeotrade.provider.service.impl.SmweixinService;
import com.aeotrade.provider.vo.IssuerConfigVO;
import com.aeotrade.provider.vo.RegisterOne;
import com.aeotrade.provider.vo.RegisterReturn;
import com.aeotrade.provider.vo.SingleClass;
import com.aeotrade.service.MqSend;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.RespResult;
import com.aeotrade.utlis.HttpRequestUtils;
import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: 吴浩
 * @Date: 2021-08-18 17:58
 */
@Slf4j
@RestController
@RequestMapping("/single")
public class SingleLoginController extends BaseController {
    @Value("${hmtx.login.gateway-url}")
    private String gatewayUrl;
    @Autowired
    private SgsConfigurationMapper sgsConfigurationMapper;
    @Autowired
    private SingleLoginService singleLoginService;
    @Autowired
    private SmweixinService smweixinService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private MqSend mqSend;

    /**
     * 北京单一窗口注册登录
     * @param registerOne
     * @return
     */
    @PostMapping("/member")
    public RespResult loginSingle(@RequestBody RegisterOne registerOne) {
        try {
            if (null == registerOne) {
                return handleFail("注册信息不能为空");
            }
            if (StringUtils.isEmpty(registerOne.getMemberName())) {
                return handleFail("企业名称不能为空");
            }
            if (StringUtils.isEmpty(registerOne.getUscCode())) {
                return handleFail("企业统一社会信用代码缺失请补全");
            }
            if (StringUtils.isEmpty(registerOne.getPhone())) {
                return handleFail("手机号不能为空");
            }
            RegisterReturn registerReturn = singleLoginService.loginSingle(registerOne);
            if (registerReturn != null) {
                singleLoginService.sgsListSave(registerReturn.getUacStaff(), registerReturn.getMemberId(), registerOne.getMemberName()
                        , registerOne.getUscCode(), registerOne.getWorkMark());
                redisTemplate.opsForValue().set(AeoConstant.SMSREDIS_KEY + registerOne.getPhone(), "888888", 80, TimeUnit.SECONDS);
                return handleResult(smweixinService.loginMobile("web", registerOne.getPhone(), String.valueOf(888888)));

            } else {
                throw new AeotradeException("注册失败");
            }
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    /**
     * 根据code换用户信息
     * @param singleClass
     * @param request
     * @return
     */
    @PostMapping("/get/user")
    public RespResult getUser(@RequestBody SingleClass singleClass, HttpServletRequest request) {
        try {
            String token = stringRedisTemplate.opsForValue().get("SINGLECODE:" + singleClass.getCode());
            if (StringUtils.isNotEmpty(token)) {
                log.debug("第二次请求进来返回OK");
                Thread.sleep(2000);
                String login = stringRedisTemplate.opsForValue().get("SINGLECODE:" + singleClass.getCode());
                return handleResult(JSONObject.parseObject(login, Object.class));
            } else {
                log.debug("第一次请求进来返回OK");
                stringRedisTemplate.opsForValue().set("SINGLECODE:" + singleClass.getCode(), "1", 10, TimeUnit.MINUTES);
                RegisterOne registerOne = singleLoginService.getToken(singleClass);

                if(StringUtils.isNotEmpty(singleClass.getMemberId())){
                    String post= sendOnly(singleClass, registerOne);
                    RespResult respResult = JSONObject.parseObject(post, RespResult.class);
                    if(respResult.getCode()==200){
                        // 以判断VC资质参数，如果有则进行申请
                        if (StringUtils.isNotEmpty(singleClass.getStaffId())
                                && StringUtils.isNotEmpty(singleClass.getSgsConfigId())) {
                            SgsConfiguration sgsConfig = sgsConfigurationMapper.selectById(singleClass.getSgsConfigId());
                            if (sgsConfig != null) {
                                IssuerConfigVO issuerConfigVO = JSONObject.parseObject(sgsConfig.getIssuerConfig(), IssuerConfigVO.class);
                                // 申请VC
                                ChainApplyCredential chainApplyCredential = new ChainApplyCredential();
                                int result = (int)respResult.getResult();
                                if (result==1){
                                    chainApplyCredential.setIsSign(true);
                                    chainApplyCredential.setApplyStatus("认证成功");
                                }else {
                                    chainApplyCredential.setIsSign(false);
                                    chainApplyCredential.setApplyStatus("认证失败");
                                }

                                chainApplyCredential.setCreateAt(LocalDateTime.now().atZone(
                                        ZoneId.of("Asia/Shanghai")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                                chainApplyCredential.setMemberId(singleClass.getMemberId() != null ? Long.parseLong(singleClass.getMemberId()) : 0);
                                chainApplyCredential.setMemberName(registerOne.getMemberName());
                                chainApplyCredential.setMemberUscc(registerOne.getUscCode());
                                chainApplyCredential.setVcTemplateId(issuerConfigVO.getVcTemplateId());
                                chainApplyCredential.setCredentialName(issuerConfigVO.getCredentialName());
                                chainApplyCredential.setIssuerId(issuerConfigVO.getIssuerId());
                                chainApplyCredential.setIssuerName(issuerConfigVO.getIssuerName());
                                chainApplyCredential.setStaffId(singleClass.getStaffId());
                                chainApplyCredential.setSgsId(Long.parseLong(singleClass.getSgsConfigId()));
                                chainApplyCredential.setSgsName(sgsConfig.getSgsName());
                                chainApplyCredential.setSgsLogo(sgsConfig.getIco());

                                mqSend.sendChain(JSONObject.toJSONString(chainApplyCredential), SgsConstant.VCSIGN_BJSINGLEWINDOW);
                            }

                        }
                    }
                    stringRedisTemplate.opsForValue().set("SINGLECODE:" + singleClass.getCode(), JSONUtils.toJSONString(respResult.getResult()), 10, TimeUnit.MINUTES);
                    return respResult;
                }else{
                    RespResult respResult = loginSingle(registerOne);
                    sendOnly(singleClass, registerOne);
                    stringRedisTemplate.opsForValue().set("SINGLECODE:" + singleClass.getCode(), JSONUtils.toJSONString(respResult.getResult()), 10, TimeUnit.MINUTES);
                    return respResult;
                }
            }
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    private String sendOnly(@RequestBody SingleClass singleClass, RegisterOne registerOne) throws Exception {
        Map<String,Object> map=new HashMap<>();
        map.put("uscc",registerOne.getUscCode());
        map.put("memberName",registerOne.getMemberName());
        map.put("memberId",singleClass.getMemberId());
        String post = HttpRequestUtils.httpPost(gatewayUrl+"/uac/sgs/member/only", map);
        return post;
    }


}
