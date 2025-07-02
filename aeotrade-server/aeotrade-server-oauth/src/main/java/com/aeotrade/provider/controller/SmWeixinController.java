package com.aeotrade.provider.controller;

import com.aeotrade.annotation.Ex;
import com.aeotrade.base.constant.AeoConstant;
import com.aeotrade.base.constant.BizConstant;
import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.dto.QrInviteReq;
import com.aeotrade.provider.dto.UserDto;
import com.aeotrade.provider.event.AddStaffEvent;
import com.aeotrade.provider.model.*;
import com.aeotrade.provider.service.*;
import com.aeotrade.provider.service.async.UserConnectionService;
import com.aeotrade.provider.service.impl.SmweixinService;
import com.aeotrade.provider.util.HttpRequestUtils;
import com.aeotrade.provider.util.ValidateCode;
import com.aeotrade.provider.vo.RegisterOne;
import com.aeotrade.provider.vo.RegisterReturn;
import com.aeotrade.provider.vo.wxLogin;
import com.aeotrade.service.RedisService;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.RespResult;
import com.aeotrade.utlis.DateUtil;
import com.aeotrade.utlis.JacksonUtil;
import com.aeotrade.utlis.PassWordDecode;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/social")
public class SmWeixinController extends BaseController {
    @Value("${hmtx.login.gateway-url}")
    private String gatewayUrl;
    @Autowired
    private UacStaffService uacStaffService;
    @Autowired
    private ConsumerTokenServices consumerTokenServices;
    @Autowired
    private UserConnectionService userConnectionService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UacUserService uacUserService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private UacMemberStaffService uacMemberStaffService;
    @Autowired
    private SmweixinService smweixinService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UacMemberService uacMemberService;
    @Autowired
    private UacUserConnectionService uacUserConnectionService;

    @Autowired
    private UacAdminService uacAdminService;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private UawWorkbenchService uawWorkbenchService;
    @Autowired
    private UawVipTypeService uawVipTypeService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * 根据ID获取用户协议
     * @param id
     * @return
     * @throws IOException
     */
    @GetMapping("/protocol")
    public RespResult<Document> protocol(String id) throws IOException {
        Document byId = mongoTemplate.findById(id, Document.class, "hmm_protocol");
        return handleResult(byId);
    }

    //添加用户协议
    @PostMapping("/protocol")
    public RespResult protocol(@RequestBody Map<String, String> map) throws IOException {

        mongoTemplate.save(map, "hmm_protocol");
        return handleOK();
    }

    //获取图片验证码
    @RequestMapping("/code/image")
    public void imageCode(String userName, HttpServletResponse response) throws IOException {
        // 设置响应的类型格式为图片格式
        response.setContentType("image/jpeg");
        //禁止图像缓存。
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        ValidateCode vCode = new ValidateCode(120, 40, 5, 100);
        redisTemplate.opsForValue().set(AeoConstant.IMAGEREDIS_KEY + userName, vCode.getCode(), 60, TimeUnit.SECONDS);
        vCode.write(response.getOutputStream());

    }

    /**
     * 扫码URL：https://open.weixin.qq.com/connect/qrconnect?appid=wxa005a706f33a850d&redirect_uri=http%3A%2F%2Fwww.aeotrade.com%2Fsocial%2Fwechat%2Fcallback&response_type=code&scope=snsapi_login&state=STATE#wechat_redirect
     * 回调接口
     *
     * @param code
     * @param state
     * @param xpid
     * @return
     * @throws Exception
     */
    @Ex(value = "扫码登录回调", count = 1, timeUnit = TimeUnit.HOURS)
    @RequestMapping("/wechat/callback")
    public Callable<RespResult> callBack_new(String code, String state, Long xpid, String member, String loginType) throws IOException {
        log.debug("进入授权回调,code:{},state:{}", code, state);
        return smweixinService.callBack_new(code, state, xpid, member, loginType);
    }

    @Ex(value = "扫码回调绑定子管理员", count = 1, timeUnit = TimeUnit.HOURS)
    @RequestMapping("/wechat/addsubadmin")
    public Callable<RespResult> callBack_add_subadmin(
            String code, String state, Long pStaffId, Long pMemberId) {

        return smweixinService.callBack_add_subadmin(code, state, pStaffId, pMemberId);


    }

    @Ex(value = "扫码绑定微信", count = 1, timeUnit = TimeUnit.HOURS)
    @PostMapping("/wechat/build")
    public RespResult callBack_wx_build(@RequestBody Map<String, String> map) {
        if (map == null || map.get("code") == null || map.get("staffId") == null) {
            return handleFail("code , staffId is not null");
        }
        return smweixinService.callBack_wx_build(map.get("code"), map.get("state"), Long.valueOf(map.get("staffId")));

    }

    /**
     * 员工接受扫码邀请入驻企业
     * @param qrInviteReq
     * @return
     */
    @PostMapping("/wechat/invite")
    public RespResult scanQRCodeToInviteFriends(@RequestBody QrInviteReq qrInviteReq) {
        try {
            log.info("员工接受扫码邀请入驻企业" + JSON.toJSONString(qrInviteReq));
            // 1. 通过unionid添加用户是否注册
            List<UacUserConnection> list = uacUserConnectionService.lambdaQuery()
                    .eq(UacUserConnection::getProviderId, BizConstant.ClientEnum.WECHAT.toString().toLowerCase())
                    .eq(UacUserConnection::getUnionid, qrInviteReq.getUnionid()).list();
            UacUserConnection userConnection = list.size() > 0 ? list.get(0) : null;
            UacStaff uacStaff = null;
            if (userConnection != null && userConnection.getStaffId() != null) {
                uacStaff = uacStaffService.getById(userConnection.getStaffId());
                if (uacStaff != null && uacStaff.getStatus() != null && uacStaff.getStatus() == 1) { // 1.代表已经删除
                    uacStaff = null;
                }
            }

            if (uacStaff != null) {
                // 2. 如果注册，将用户staffId 与企业绑定，并设置角色
                uacMemberStaffService.buildStaffAndMember(uacStaff.getId(), qrInviteReq.getMemberId(), qrInviteReq.getName(), qrInviteReq.getRoleId(), qrInviteReq.getDeptId());

            } else {
                // 3. 如果没注册，自动完成注册，并绑定角色
                smweixinService.buildNewStaffAndMember(qrInviteReq.getPhoneNumber(), qrInviteReq.getUnionid(), qrInviteReq.getMemberId(), qrInviteReq.getName(), qrInviteReq.getRoleId(), qrInviteReq.getDeptId());
            }

            //为员工生成证书
            log.info("生成邀请员工事件");
            AddStaffEvent addStaffEvent = new AddStaffEvent(this, uacMemberService.getById(qrInviteReq.getMemberId()), uacStaff,"员工");
            eventPublisher.publishEvent(addStaffEvent);

            return handleOK();
        } catch (AeotradeException exception) {
            return handleResult(exception.getMessage());
        } catch (Exception e) {
            return handleFail(e.getMessage());
        }
    }

    /**
     * 补充员工证书
     */
    @PostMapping("/supplement/memberStaff/cert")
    public RespResult<String> supplementaryEnterpriseCertificate() {
        log.info("补充员工证书开始");
        List<UacMemberStaff> list = uacMemberStaffService.list();
        for (UacMemberStaff uacMemberStaff : list) {
            if (uacMemberStaff.getStaffId() != null&& uacMemberStaff.getMemberId() != null) {
                AddStaffEvent addStaffEvent = new AddStaffEvent(this, uacMemberService.getById(uacMemberStaff.getMemberId()), uacStaffService.getById(uacMemberStaff.getStaffId()), "员工");
                eventPublisher.publishEvent(addStaffEvent);
            }
        }
        log.info("补充员工证书结束");
        return handleResult("完成发送企业员工信息到生成证书队列中");
    }

    /**
     * 退出登录
     *
     * @param request
     * @return
     */
    @PostMapping("signout")
    public Object signout(HttpServletRequest request) {
        try {
            String authorization = request.getHeader("Authorization");
            String token = StringUtils.replace(authorization, "bearer ", "");
            token = StringUtils.replace(token, "bearer ", "");
            if (!consumerTokenServices.revokeToken(token)) {
                handleFail(new RuntimeException("退出登录失败"));
            }
            redisService.set(token, "1", 86400L);//限制一天时间
            return handleResult("退出成功");
        } catch (Exception e) {
            return handleOK();
        }
    }

    /**
     * 检查验证码逻辑
     *
     * @param code
     * @param requestId
     * @return
     */
    private String validateCode(String code, String requestId) {
        // 如果vcode为空，跳过验证码验证,主要是适配之前的业务
        if (StringUtils.isBlank(code)) {
            return "验证码不能为空";
        }
        // 如果有参数vcode,但没有U-ID，进行提醒，这一般是调接口时发生的错误
        if (StringUtils.isNotBlank(code) && StringUtils.isBlank(requestId)) {
            return "缺少参数，U-ID";
        }
        // 符合参数验证条件
        String s = redisTemplate.opsForValue().get(AeoConstant.IMAGEREDIS_KEY + "_" + requestId);
        if (StringUtils.isBlank(s)) {
            return "验证码已失效";
        }
        if (!StringUtils.equalsIgnoreCase(s, code)) {
            return "验证码错误";
        }
        return "";
    }
// 为使用授权码登录，现将该方法实现成 SocialLoginFilter，返回数据放在了AuthorizationCodeSuccessHandler 处

    //密码登录模式
    @PostMapping("/login")
    public Object login(@RequestBody Map<String, String> maps, @RequestHeader(value = "U-ID", required = false) String requestId) {
        try {
            String vmessage = validateCode(maps.get("vcode"), requestId);
            if (StringUtils.isNotBlank(vmessage)) {
                return handleFail(vmessage);
            }
            String username = maps.get("username");
            if (StringUtils.isBlank(username)) {
                return handleFail("账户不能为空");
            }
            String password = maps.get("password");
            if (StringUtils.isBlank(password)) {
                return handleFail("密码不能为空");
            }

            List<UacAdmin> uacUserOptionals = uacAdminService.lambdaQuery().eq(UacAdmin::getUsername, username).list();
            UacAdmin uacUserOptional = !uacUserOptionals.isEmpty() ? uacUserOptionals.get(0) : null;
            if (null == uacUserOptional || null == uacUserOptional.getStaffId()) {
                return handleFail("未找到该用户，请确认用户名称是否正确");
            } else {
                OAuth2AccessToken oAuth2AccessToken = userConnectionService.findOAuth2AccessTokenBy(String.valueOf(uacUserOptional.getStaffId()), maps.get("loginType"), username, password);
                Map<String, Object> map = JacksonUtil.parseJson(JacksonUtil.toJsonWithFormat(oAuth2AccessToken), Map.class);
                UacUserConnection uacUser = userConnectionService.findUacUserConnectionBy(uacUserOptional.getStaffId());
                UacStaff uacStaff = uacStaffService.getById(uacUserOptional.getStaffId());
                map = smweixinService.loginjson(map, uacUser, uacStaff);
                return handleResult(map);
            }
        } catch (Exception e) {
            log.info(e.getMessage());
            return handleFail("用户名或密码错误,请核对后再重新登录");
        }
    }

    @Ex(value = "扫码添加员工", count = 1, timeUnit = TimeUnit.HOURS)
    @RequestMapping("/staff/callback")
    @Transactional
    public Callable<RespResult> callBack_staff(String code, String state, Long memberId/*,String member*/) {

        return smweixinService.callBack_staff(code, state, memberId);


    }


    /**慧贸OS扫码登录-----------------------------------------------------------------------------------------------*/
    /**
     * 扫码URL：https://open.weixin.qq.com/connect/qrconnect?appid=wxa005a706f33a850d&redirect_uri=http%3A%2F%2Fwww.aeotrade.com%2Fsocial%2Fwechat%2Fcallback&response_type=code&scope=snsapi_login&state=STATE#wechat_redirect
     * 回调接口
     *
     * @param code
     * @param state
     * @param xpid
     * @return
     * @throws Exception
     */
    @Ex(value = "扫码登录回调", count = 1, timeUnit = TimeUnit.HOURS)
    @RequestMapping("/hmm/wechat/callback")
    public Callable<RespResult> hmmCallBack_new(String code, String state, Long xpid, String member) throws IOException {

        return smweixinService.hmmCallBack_new(code, state, xpid, member);


    }


    //查询是否有同名的用户
    @RequestMapping("/find/user")
    public RespResult findUserByName(String name) {

        int size = smweixinService.findUserByName(name);
        return handleResult(size);
    }

    //查询是否有同名的用户
    @GetMapping("/find/user/staff/id")
    public RespResult findUserByStaffId(Long staffId) {

        UacAdmin uacUser = smweixinService.findUserByStaffId(staffId);
        UacUser user = new UacUser();
        if (uacUser != null) {
            user.setUserId(uacUser.getId());
            user.setUsername(uacUser.getUsername());
            user.setPassword(uacUser.getPassword());
            user.setStatus(uacUser.getStatus());
            user.setMobile(uacUser.getMobile());
            user.setModifyTime(uacUser.getUpdateTime());
            user.setStaffId(uacUser.getStaffId());
            user.setCreateTime(uacUser.getCreateTime());
        }
        return handleResult(Optional.ofNullable(user).orElseGet(() -> new UacUser()));
    }

    //绑定用户名密码
    @PostMapping("binding/user")
    public RespResult upadteAdmin(@RequestBody UserDto userDto) {

        try {
            String decode = PassWordDecode.decode(userDto.getPassWord());
            String encode = passwordEncoder.encode(decode);
            userDto.setPassWord(encode);
            log.info(decode);
            if (userDto == null) {
                throw new AeotradeException("参数不能为空");
            }
            uacMemberStaffService.upadteAdmin(userDto);
            return handleOK();
        } catch (Exception e) {
            return handleFail(e);
        }
    }


    //修改用户名密码
    @PostMapping("update/user")
    public RespResult upadteAdminById(@RequestBody UacUser uacUser) {
        try {
            String decode = PassWordDecode.decode(uacUser.getPassword());
            uacUser.setPassword(decode);
            log.info(decode);

            if (uacUser == null) {
                throw new AeotradeException("参数不能为空");
            }
            if (uacUser.getUserId() == null) {
                throw new AeotradeException("主键ID不能为空");
            }
            UacAdmin user = new UacAdmin();
            user.setId(uacUser.getUserId());
            user.setUsername(uacUser.getUsername());
            user.setPassword(uacUser.getPassword());
            user.setStatus(1);
            user.setUpdateTime(new Timestamp(System.currentTimeMillis()).toLocalDateTime());
            user.setStaffId(uacUser.getStaffId());
            //@Update("UPDATE aeotrade_admin.uac_admin SET password = #{password},update_time=#{data} where id = #{userId} and is_tab=1")
            String encode = passwordEncoder.encode(uacUser.getPassword());
            uacUser.setPassword(encode);
            uacAdminService.lambdaUpdate().set(UacAdmin::getPassword, encode).set(UacAdmin::getUpdateTime, DateUtil.getData())
                    .eq(UacAdmin::getId, user.getId()).update();
            return handleOK();
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    //查询手机是否已经绑定
    @GetMapping("/find/mobile")
    public RespResult findMobile(String phone) {
        Integer rows = uacUserService.findByMobile(phone);
        return handleResult(rows);
    }

    //根据手机查询用户并绑定企业
    @GetMapping("/member/mobile")
    public RespResult memberMobile(Long memberId, String phone) {
        int mobile = uacUserService.memberMobile(memberId, phone);
        if (mobile == 3) {
            throw new AeotradeException("手机号不能为空");
        }
        return handleResult(mobile);
    }

    //绑定手机号
    @GetMapping("/binding/mobile")
    public RespResult bindingMobile(String mobile, Long staffId) {
        try {
            if (mobile == null) {
                throw new AeotradeException("手机号不能为空");
            }
            if (staffId == null) {
                throw new AeotradeException("员工ID不能为空");
            }
            uacUserService.bindingMobile(mobile, staffId, null, null);
            return handleOK();
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    //登录页面绑定手机号
    @GetMapping("/login/bangding")
    public RespResult LoginBanding(String mobile, Long staffId, String staffname, String url) {
        if (mobile == null) {
            throw new AeotradeException("手机号不能为空");
        }
        if (staffId == null) {
            throw new AeotradeException("员工ID不能为空");
        }
        if (StringUtils.isEmpty(url)) {
            url = "/e71120c3-0102-4e4d-8120-8c413e869633.png";
        }
        try {
            uacUserService.LoginBanding(mobile, staffId, staffname, url);
            return handleOK();
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    // 为使用授权码登录，现将该方法实现成 SmsLoginFilter
    //手机号登录
    @GetMapping("/login/mobile")
    public RespResult loginMobile(String mobile, String code, String loginType) {
        try {
            if (mobile == null) {
                throw new AeotradeException("手机号不能为空");
            }
            if (code == null) {
                throw new AeotradeException("验证码不能为空");
            }
            return handleResult(smweixinService.loginMobile(loginType, mobile, code));
        } catch (Exception e) {
            return handleFail(e);
        }
    }



    //游客身份注册登录
    @PostMapping("/register")
    public RespResult register(@RequestBody RegisterOne registerOne) throws Exception {
        if (null == registerOne) {
            throw new AeotradeException("注册信息不能为空");
        }
        if (StringUtils.isEmpty(registerOne.getMemberName())) {
            throw new AeotradeException("企业名称不能为空");
        }
        if (StringUtils.isEmpty(registerOne.getUscCode())) {
            throw new AeotradeException("统一社会信用代码不能为空");
        }
        if (null == registerOne.getVipTypeId()) {
            throw new AeotradeException("企业角色不能为空");
        }
        registerOne.setSourceMark("慧贸OSPC端");

        UawWorkbench lastWorkbench = null;
        UawVipType uawVipType = uawVipTypeService.getById(registerOne.getVipTypeId());
        if (uawVipType != null) {
            lastWorkbench = uawWorkbenchService.getById(uawVipType.getWorkbench());
        }
        RegisterReturn register = smweixinService.register(registerOne, lastWorkbench);
        if (register != null) {
            try {
                sgsListSave(registerOne, register.getUacStaff(), register.getMemberId());
            } catch (Exception e) {
                log.warn(e.getMessage());
            }
            if (StringUtils.isNotEmpty(registerOne.getCode())) {
                return handleResult(smweixinService.loginMobile("web", registerOne.getPhone(), registerOne.getCode()));
            } else {
                redisTemplate.opsForValue().set(AeoConstant.SMSREDIS_KEY + registerOne.getPhone(), "888888", 80, TimeUnit.SECONDS);
                return handleResult(smweixinService.loginMobile("web", registerOne.getPhone(), String.valueOf(888888)));
            }
        } else {
            throw new AeotradeException("注册失败");
        }
    }



    //设置默认企业
    @GetMapping("/defult/member")
    public RespResult SetDefultMember(Long staffId, Long memberId) {
        if (null == memberId) {
            throw new RuntimeException("企业id不能为空");
        }
        if (null == staffId) {
            throw new RuntimeException("个人id不能为空");
        }
        int i = uacStaffService.SetDefultMember(staffId, memberId);
        if (i == 1) {
            return handleOK();
        }
        return handleFail("设置默认企业失败");
    }

    @Ex(value = "入驻资料提交")
    private void sgsListSave(RegisterOne registerOne, UacStaff uacStaff, Long instrer) throws Exception {

        if (registerOne.getVipTypeId() != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("createdBy", uacStaff.getStaffName());
            map.put("createdById", uacStaff.getId());
            map.put("memberId", instrer);
            map.put("memberName", registerOne.getMemberName());
            map.put("uscc", registerOne.getUscCode());
            map.put("vipTypeId", registerOne.getVipTypeId());
            log.debug("http调用开始" + JSONObject.toJSONString(map));
            String http = HttpRequestUtils.httpPost(gatewayUrl+"/mam/aptitude/save", map);
            log.debug("http调用结果" + http);
            RespResult respResult = JSONObject.parseObject(http, RespResult.class);
            log.debug("JSON转换结果" + respResult);
        }
    }


    //扫码登录每秒轮询获取登录数据接口
    @GetMapping("/login/scan/ck")
    public RespResult loginScanCheck(@RequestParam String sceneValue) {
        String loginInfo = redisTemplate.opsForValue().get("AEOTRADE_WX_LOGIN:" + sceneValue);
        if (loginInfo == null) {
            return handleOK();
        }
        return handleResult(JSON.parseObject(loginInfo));
    }

    //扫码登录用户注册登录接口
    @PostMapping("/login/wx/user")
    public RespResult loginWxUser(@RequestBody wxLogin wxLogin) {
        Map map = smweixinService.loginWxUser(wxLogin.getWxUser(), wxLogin.getLoginType());
        if (null != map) {
            redisTemplate.opsForValue().set("AEOTRADE_WX_LOGIN:" + wxLogin.getSceneValue(), JSON.toJSONString(map), 1, TimeUnit.MINUTES);
            return handleOK();
        } else {
            return handleResult(500, "微信扫码登录注册失败");
        }
    }

    /**
     * 微信小程序通过手机号登录
     *
     */
    @PostMapping("/wx/mini/login")
    public RespResult wxMiniLogin(@RequestBody wxLogin wxLogin) {
        if (wxLogin.getLoginType() == null || !org.springframework.util.StringUtils.hasText(wxLogin.getLoginType())) {
            wxLogin.setLoginType("client");
        }
        Map map = smweixinService.loginWxUser(wxLogin.getWxUser(), wxLogin.getLoginType());
        if (null != map) {
            if (map.containsKey("jti")){
                map.remove("jti");
                map.remove("provideruserid");
                map.remove("providermqid");
                map.remove("unionid");
            }
            return handleResult(map);
        } else {
            return handleResult(500, "登录注册失败");
        }
    }

    /**
     * 获取慧贸平台用户登录信息 ,兼容旧的登录逻辑
     *
     * @param token
     * @return
     */
    @PostMapping("/login/user")
    public RespResult loginUserInfo(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null) {
            return handleFail("未发现token");
        }

        token = token.substring("Bearer ".length());
        Jwt jwt = JwtHelper.decode(token);
        Map checkedToken = JSON.parseObject(jwt.getClaims(), Map.class);
        String userName = checkedToken.get("user_name").toString();
        UacStaff uacStaff = null;
        if (JSON.isValid(userName)) {
            JSONObject jsonObject = JSON.parseObject(userName);
            if (jsonObject.containsKey("staffId")) {
                uacStaff = uacStaffService.getById(jsonObject.getString("staffId"));
            }
        } else {
            uacStaff = uacStaffService.getById(userName);
        }
        Map loginmap = smweixinService.loginjson(checkedToken, null, uacStaff);
        loginmap.remove("user_name");
        loginmap.remove("aud");
        loginmap.remove("scope");
        loginmap.remove("client_id");
        loginmap.remove("exp");
        loginmap.remove("jti");
        return handleResult(loginmap);
    }

}
