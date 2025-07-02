package com.aeotrade.provider.admin.controller;


import com.aeotrade.base.business.sendFormData;
import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.admin.adminVo.*;

import com.aeotrade.provider.admin.common.CommonPage;
import com.aeotrade.provider.admin.common.CommonResult;
import com.aeotrade.provider.admin.config.MD5;
import com.aeotrade.provider.admin.config.ValidateCode;
import com.aeotrade.provider.admin.entiy.UacAdmin;
import com.aeotrade.provider.admin.entiy.UacRole;
import com.aeotrade.provider.admin.entiy.UacWhiteList;
import com.aeotrade.provider.admin.service.UacAdminService;
import com.aeotrade.provider.admin.service.UacRoleService;
import com.aeotrade.provider.admin.service.UacWhiteListService;
import com.aeotrade.provider.admin.service.UawWorkbenchMenuService;
import com.aeotrade.utlis.AutologinUtil;
import com.aeotrade.utlis.CommonUtil;
import com.aeotrade.utlis.HttpRequestUtils;
import com.aeotrade.utlis.ThreadPoolUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Base64Utils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 后台用户管理Controller
 * Created by hmm on 2018/4/26.
 */
@RestController
//@Api(tags = "UmsAdminController", description = "后台用户管理")
@RequestMapping("/admin")
@Slf4j
public class UacAdminEntityController {
    @Autowired
    LoadBalancerClient loadBalancerClient;
    @Value("${hmtx.oauth.token-url}")
    private String authUrl;
    @Value("${jwt.tokenHead}")
    private String tokenHead;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private UawWorkbenchMenuService uacMenuEntityService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private UacAdminService uacAdminService;
    @Autowired
    private UacRoleService uacRoleService;
    @Autowired
    private UacWhiteListService uacWhiteListService;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    @Value("${hmtx.crm.url:}")
    private String crmurl;

    //@ApiOperation(httpMethod = "GET", value = "获取图片验证码")
    @RequestMapping("/code/image")
    public void imageCode(String userName, HttpServletResponse response) throws IOException {
        // 设置响应的类型格式为图片格式
        response.setContentType("image/jpeg");
        //禁止图像缓存。
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        ValidateCode vCode = new ValidateCode(120, 40, 5, 100);
        redisTemplate.opsForValue().set("AEOTRADE_IMAGE" + userName, vCode.getCode(), 60, TimeUnit.SECONDS);
        vCode.write(response.getOutputStream());
        log.info(vCode.getCode() + ">>>>>>>>>>>>>>>>>>");
    }

    //@ApiOperation(httpMethod = "GET", value = "校验链上数据，获取图片验证码")
    @RequestMapping("/chain/code/image")
    public void chainCheckImageCode(HttpServletResponse response) throws IOException {
        // 设置响应的类型格式为图片格式
        response.setContentType("image/jpeg");
        //禁止图像缓存。
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        ValidateCode vCode = new ValidateCode(120, 40, 5, 100);
        MD5 md5 = new MD5();
        response.setHeader("vcode", md5.getMD5ofStr(vCode.getCode().toLowerCase()));
        vCode.write(response.getOutputStream());
        log.info(vCode.getCode() + ">>>>>>>>>>>>>>>>>>");
    }

    //@ApiOperation(httpMethod = "GET", value = "获取图片验证码,随机生成对应标识，请求时需要传回服务端,有效时间 60s")
    @RequestMapping("/code/random/image")
    public void imageRandomCode(HttpServletResponse response) throws IOException {
        // 设置响应的类型格式为图片格式
        response.setContentType("image/jpeg");
        //禁止图像缓存。
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        ValidateCode vCode = new ValidateCode(120, 40, 5, 100);

        // 生成请求唯一标识
        //1.UUID生成32位数
        String uuid32 = UUID.randomUUID().toString().replace("-", "");
        //2.然后截取前面或后面16位
        String uuid16 = uuid32.substring(0, 16);

        redisTemplate.opsForValue().set("AEOTRADE_IMAGE_" + uuid16, vCode.getCode(), 360, TimeUnit.SECONDS);

        response.setHeader("U-ID", uuid16);
        vCode.write(response.getOutputStream());
    }

    //@ApiOperation(value = "登录以后返回token")
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public CommonResult login(@Validated @RequestBody UacAdminLoginParam umsAdminLoginParam) throws Exception {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(umsAdminLoginParam.getUsername());
        if (m.find()) {
            return CommonResult.failed("中文???????");
        }
        //判断数据库存的验证码是否正确
        String code1 = redisTemplate.opsForValue().get(com.aeotrade.base.constant.AeoConstant.IMAGEREDIS_KEY + umsAdminLoginParam.getUsername());
        if (code1 == null) {
            return CommonResult.failed("验证码不存在");
        }
        //验证的过期时间
        Long expire = redisTemplate.opsForValue().getOperations().getExpire(com.aeotrade.base.constant.AeoConstant.IMAGEREDIS_KEY + umsAdminLoginParam.getUsername());
        if (expire <= 0) {
            return CommonResult.failed("验证码已过期");
        }
        if (!code1.equals(umsAdminLoginParam.getCode().toUpperCase())) {
            return CommonResult.failed("验证码错误");
        }
        if (!umsAdminLoginParam.getUsername().equals("admin")) {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String ip = "";
            if (request.getHeader("x-forwarded-for") == null) {
                ip = request.getRemoteAddr();
            } else {
                ip = request.getHeader("x-forwarded-for");
            }
//            System.out.println("IP:" + ip);
            UacWhiteList uacWhiteList = uacWhiteListService.getById(1L);
            if (uacWhiteList.getStatus() == 1 && !uacWhiteList.getIp().contains(ip.split(",")[0])) {
                return CommonResult.failed("您登录的IP不在白名单,请联系管理员！");
            }
        }
        String count = redisTemplate.opsForValue().get("LOGINERROR:" + umsAdminLoginParam.getUsername());
        if (StringUtils.isNotEmpty(count) && count.equals("5")) {
            Long time = redisTemplate.getExpire("LOGINERROR:" + umsAdminLoginParam.getUsername());
            return CommonResult.failed("你的账号已冻结，请" + getTime(time) + "后重试");
        }
        String byUserName = uacAdminService.findByUserName(umsAdminLoginParam.getUsername());
        if (StringUtils.isEmpty(byUserName)) {
            return new CommonResult(403, "请联系管理员创建用户或分配角色", null);
        }
        ResponseEntity<Map> exchange = getToken(umsAdminLoginParam);
        Map bodyMap = exchange.getBody();
        Map<String, String> tokenMap = new HashMap<>();
        if (bodyMap.containsKey("error")) {
            if (bodyMap.get("error").equals("invalid_grant")) {
                String error = redisTemplate.opsForValue().get("LOGINERROR:" + umsAdminLoginParam.getUsername());
                if (StringUtils.isNotEmpty(error)) {
                    redisTemplate.opsForValue().set("LOGINERROR:" + umsAdminLoginParam.getUsername(), String.valueOf(Long.valueOf(error) + 1), 30, TimeUnit.MINUTES);
                } else {
                    redisTemplate.opsForValue().set("LOGINERROR:" + umsAdminLoginParam.getUsername(), "1", 30, TimeUnit.MINUTES);
                }
            }
            return new CommonResult(403, bodyMap.get("error").equals("invalid_grant") ? "密码错误" :
                    "该用户不存在", null);
        } else {
            tokenMap.put("token", bodyMap.get("access_token").toString());
            tokenMap.put("tokenHead", tokenHead);
        }
        List<UacRole> uacRoleList = uacAdminService.findRoleByUserName(umsAdminLoginParam.getUsername(), "客服主管", "客服专员", "超级管理员");
        AutologinUtil autologinUtil = new AutologinUtil();
        String userroles = userroles(uacRoleList);
        String isAdminUser = isAdminUser(uacRoleList);
        String encode = autologinUtil.encode(
                umsAdminLoginParam.getUsername(),
                umsAdminLoginParam.getPassword(),
                StringUtils.isNotBlank(userroles) ? userroles : (isAdminUser.equals("1") ? "客服主管" : StringUtils.SPACE),
                isAdminUser);
        tokenMap.put("res", URLEncoder.encode(encode, "UTF-8"));
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                /**保存到日志*/
                UserAdminLogInfo busDocLogs = new UserAdminLogInfo();
                busDocLogs.setType("登录");
                busDocLogs.setCpagename("登录页面");
                busDocLogs.setCurl("/login");
                busDocLogs.setDurationtime(1L);
                busDocLogs.setFpagename("无");
                busDocLogs.setFurl("无");
                busDocLogs.setName(byUserName);
                busDocLogs.setUsername(umsAdminLoginParam.getUsername());
                busDocLogs.setTime(System.currentTimeMillis());
                List<String> idList = uacRoleList.stream().map(UacRole::getName).collect(Collectors.toList());
                String deptIds = StringUtils.join(idList.toArray(), ",");
                busDocLogs.setRole(deptIds);
                rabbitTemplate.convertAndSend("LOG_STAT_ADMIN", JSONObject.toJSONString(busDocLogs));
            }
        });
        return CommonResult.success(tokenMap);
    }



    public String getTime(Long time) {
        String timeStr = "";
        if (time == null) {
            return null;
        }
        //时
        Long hour = time / 60 / 60;
        //分
        Long minutes = time / 60 % 60;
        //秒
        Long remainingSeconds = time % 60;
        //判断时分秒是否小于10……
        if (hour < 10) {
            timeStr = minutes + "分" + remainingSeconds + "秒";
        } else if (minutes < 10) {
            timeStr = minutes + "分" + remainingSeconds + "秒";
        } else if (remainingSeconds < 10) {
            timeStr = minutes + "分" + "0" + remainingSeconds + "秒";
        } else {
            timeStr = minutes + "分" + remainingSeconds + "秒";
        }
        return timeStr;
    }

    //@Ex(value = "运营后台登录获取token调用接口", count = 1, timeUnit = TimeUnit.HOURS)
    public ResponseEntity<Map> getToken(UacAdminLoginParam umsAdminLoginParam) throws Exception {
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password_code");
        body.add("code", umsAdminLoginParam.getCode());
        body.add("username", umsAdminLoginParam.getUsername());
        body.add("password", umsAdminLoginParam.getPassword());
        //定义header
        LinkedMultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        String httpBasic = getHttpBasic("password_code", "secret");
        header.add("Authorization", httpBasic);
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(body, header);

        String uriString = UriComponentsBuilder.fromHttpUrl(authUrl)
                .queryParam("grant_type", "password_code")
                .queryParam("username", umsAdminLoginParam.getUsername())
                .queryParam("password", umsAdminLoginParam.getPassword())
                .queryParam("code", umsAdminLoginParam.getCode())
                .toUriString();
        //申请令牌信息
        ResponseEntity<Map> exchange = restTemplate.exchange(uriString, HttpMethod.POST, httpEntity, Map.class);
        return exchange;
    }

    private String userroles(List<UacRole> uacRoleList) {
        if (uacRoleList == null) {
            return StringUtils.EMPTY;
        }
        String rolestr = uacRoleList.stream().map(UacRole::getName).filter(r -> StringUtils.contains(r, "客服")).collect(Collectors.joining("|"));
        if (StringUtils.isBlank(rolestr)) {
            return StringUtils.EMPTY;
        }
        return rolestr;
    }

    private String isAdminUser(List<UacRole> uacRoleList) {
        if (uacRoleList == null) {
            return "0";
        }
        Optional<String> isadmin = uacRoleList.stream().map(UacRole::getName).filter(r -> r.equals("超级管理员")).findFirst();
        return isadmin.isPresent() ? "1" : "0";
    }


    //@ApiOperation(value = "获取当前登录用户信息NEW")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public CommonResult getAdminInfo(Principal principal) {
        if (principal == null) {
            return CommonResult.unauthorized(null);
        }
        String username = principal.getName();
        UacAdminDto uacAdminDto = JSON.parseObject(username, UacAdminDto.class);
        UacAdmin umsAdmin = uacAdminService.getAdminByUsername(uacAdminDto.getUsername());
        if (umsAdmin==null){
            return CommonResult.failed("请重新登录");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("memberId", umsAdmin.getId());
        data.put("username", umsAdmin.getUsername());
        data.put("name", umsAdmin.getNickName());
        List<UacMenuNode> uacMenuNodes = uacMenuEntityService.ListMenuUser(umsAdmin.getId());
        if (!CommonUtil.isEmpty(uacMenuNodes)) {
            data.put("menus", uacMenuNodes);
        } else {
            data.put("menus", "");
        }
        data.put("icon", umsAdmin.getIcon());
        List<UacRole> roleList = uacAdminService.getRoleList(umsAdmin.getId());
        if (roleList.size() != 0) {
            List<String> roles = roleList.stream().map(UacRole::getName).collect(Collectors.toList());
            if (CommonUtil.isEmpty(roles)) {
                throw new AeotradeException("请联系管理员分配角色");
            }
            data.put("roles", roles);
        }
        return CommonResult.success(data);
    }

    private String getHttpBasic(String clientId, String clientSecret) {
        String string = clientId + ":" + clientSecret;
        //将串进行base64编码
        byte[] encode = Base64Utils.encode(string.getBytes());
        return "Basic " + new String(encode);
    }

    //@ApiOperation(value = "运营平台用户操作日志")
    @RequestMapping(value = "/log/info", method = RequestMethod.POST)
    public CommonResult sendOperatLog(@Validated @RequestBody UserAdminLogInfos logString) throws Exception {

        for (UserAdminLogInfo adminLogInfo : logString.getUserAdminLogInfoList()) {
            if (StringUtils.isEmpty(adminLogInfo.getType())) {
                adminLogInfo.setType("访问");
            }
            rabbitTemplate.convertAndSend("LOG_STAT_ADMIN", JSONObject.toJSONString(adminLogInfo));
        }
        return CommonResult.success("ok");
    }

    //@ApiOperation(value = "获取当前登录用户信息")
    @RequestMapping(value = "/infos", method = RequestMethod.GET)
    public CommonResult getAdminInfos(Principal principal) {
        if (principal == null) {
            return CommonResult.unauthorized(null);
        }
        String username = principal.getName();
        UacAdmin umsAdmin = uacAdminService.getAdminByUsername(username);
        Map<String, Object> data = new HashMap<>();
        data.put("memberId", umsAdmin.getId());
        data.put("username", umsAdmin.getUsername());
        data.put("menus", uacRoleService.getMenuList(umsAdmin.getId()));
        List<UacRole> roleList = uacAdminService.getRoleList(umsAdmin.getId());
        if (roleList.size() != 0) {
            List<String> roles = roleList.stream().map(UacRole::getName).collect(Collectors.toList());
            data.put("roles", roles);
        }
        return CommonResult.success(data);
    }

    //@ApiOperation(value = "用户注册")
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public CommonResult<UacAdmin> register(@Validated @RequestBody UacAdminParam uacAdminParam) throws Exception {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(uacAdminParam.getUsername());
        if (m.find()) {
            return CommonResult.failed("中文???????");
        }
        if (uacAdminParam.getPassword().length() < 8) {
            return CommonResult.failed("密码最短长度8位");
        }
        if (!checkPasswordRule(uacAdminParam.getPassword())) {
            return CommonResult.failed("密码必须包含大小写字母、数字、特殊字符其中的三种或三种以上");
        }
        UacAdmin umsAdmin = uacAdminService.register(uacAdminParam);
        if (umsAdmin == null) {
            return CommonResult.failed("用户名重复");
        }
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!crmurl.equals("http://crm-train.aeotrade.com")) {
                        UacAdmin admin = uacAdminService.getAdminByUsername(umsAdmin.getUsername());
                        Map<String, String> map = new HashMap<>();
                        map.put("hmm_staff_id", String.valueOf(admin.getId()));
                        map.put("staff_name", admin.getNickName());
                        map.put("login", admin.getUsername());
                        if (!org.springframework.util.StringUtils.isEmpty(crmurl)) {
                            sendFormData.erpBus(map, crmurl + "/web/register_and_update_user");
                        }
                    }
                } catch (Exception e) {
                    log.warn(e.getMessage());
                }
            }
        });
        return CommonResult.success(umsAdmin);
    }


    public boolean checkPasswordRule(String password) {
        //数字
        String REG_NUMBER = ".*\\d+.*";
        //大写字母
        String REG_UPPERCASE = ".*[A-Z]+.*";
        //小写字母
        String REG_LOWERCASE = ".*[a-z]+.*";
        //特殊符号
        String REG_SYMBOL = ".*[~!@#$%^&*()_+|<>,.?/:;'\\[\\]{}\"]+.*";
        int i = 0;
        if (password.matches(REG_NUMBER)) i++;
        if (password.matches(REG_LOWERCASE) && password.matches(REG_UPPERCASE)) i++;
        if (password.matches(REG_SYMBOL)) i++;

        if (i != 3) return false;

        return true;
    }

    //@ApiOperation("根据用户名或姓名分页获取用户列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<UacAdminDto>> list(@RequestParam(value = "keyword", required = false) String keyword,
                                                      @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                                      @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        Page<UacAdmin> adminList = uacAdminService.findAdminlist(keyword, pageSize, pageNum);
//        CommonPage<UacAdmin> uac = CommonPage.restPage(adminList);
        if (!CommonUtil.isEmpty(adminList)) {
            List<UacAdminDto> list = new ArrayList<>();
            adminList.getRecords().forEach(admin -> {
                List<String> roleName = uacAdminService.findRoleName(admin.getId());
                UacAdminDto uacAdminDto = new UacAdminDto();
                BeanUtils.copyProperties(admin, uacAdminDto);
                if (!CommonUtil.isEmpty(roleName)) {
                    uacAdminDto.setRoleName(String.join(",", roleName));
                }
                list.add(uacAdminDto);
            });
            CommonPage<UacAdminDto> uacAdminDtoCommonPage = new CommonPage<>();
            uacAdminDtoCommonPage.setTotal(adminList.getTotal());
            uacAdminDtoCommonPage.setList(list);
            return CommonResult.success(uacAdminDtoCommonPage);
        }
        return CommonResult.success(new CommonPage<>());
    }

    //@ApiOperation("获取指定用户信息")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public CommonResult<UacAdmin> getItem(@PathVariable Long id) {
        UacAdmin admin = uacAdminService.getItem(id);
        return CommonResult.success(admin);
    }

    //@ApiOperation("修改指定用户信息")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    public CommonResult update(@PathVariable Long id, @RequestBody UacAdmin admin) throws Exception {
        int count = uacAdminService.updateAdmin(id, admin);
        if (count == 3) {
            return CommonResult.failed("用户名重复");
        }
        if (count > 0) {
            ThreadPoolUtils.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!crmurl.equals("http://crm-train.aeotrade.com")) {
                            UacAdmin item = uacAdminService.getItem(id);
                            Map<String, String> map = new HashMap<>();
                            map.put("hmm_staff_id", String.valueOf(item.getId()));
                            map.put("staff_name", item.getNickName());
                            map.put("login", item.getUsername());
                            sendFormData.erpBus(map, crmurl + "/web/register_and_update_user");
                        }
                    } catch (Exception e) {
                        log.warn(e.getMessage());
                    }
                }
            });
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    //@ApiOperation("修改指定用户密码")
    @RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
    public CommonResult updatePassword(@Validated @RequestBody UpdateAdminPasswordParam updatePasswordParam) {
        int status = uacAdminService.updatePasswords(updatePasswordParam);
        if (status > 0) {
            return CommonResult.success(status);
        } else if (status == -1) {
            return CommonResult.failed("提交参数不合法");
        } else if (status == -2) {
            return CommonResult.failed("找不到该用户");
        } else if (status == -3) {
            return CommonResult.failed("旧密码错误");
        } else {
            return CommonResult.failed();
        }
    }

    //@ApiOperation("删除指定用户信息")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    public CommonResult delete(@PathVariable Long id) {
        int count = uacAdminService.delete(id);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    //@ApiOperation("修改帐号状态")
    @RequestMapping(value = "/updateStatus/{id}", method = RequestMethod.POST)
    public CommonResult updateStatus(@PathVariable Long id, @RequestParam(value = "status") Integer status) {
        UacAdmin uacAdmin = new UacAdmin();
        uacAdmin.setId(id);
        uacAdmin.setStatus(status);
        Boolean i = uacAdminService.updateById(uacAdmin);
        if (i) {
            return CommonResult.success(i);
        }
        return CommonResult.failed();
    }

    //@ApiOperation("给用户分配角色")
    @RequestMapping(value = "/role/update", method = RequestMethod.POST)
    public CommonResult updateRole(@RequestParam("adminId") Long adminId,
                                   @RequestParam("roleIds") List<Long> roleIds, @RequestParam("workBnechId") Long workBnechId,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {
//        System.out.println("work" + workBnechId);
        int count = uacAdminService.updateRole(adminId, roleIds, workBnechId);
        if (count >= 0) {
            String res = saveResForCooke(request, response, adminId, roleIds);
            CommonResult commonResult = CommonResult.success(count);
            commonResult.setRes(res);
            return commonResult;
        }
        return CommonResult.failed();
    }

    //@ApiOperation("给用户分配角色")
    @RequestMapping(value = "/role/update/new", method = RequestMethod.POST)
    public CommonResult updateRoleNew(@RequestParam("adminId") Long adminId,
                                      @RequestParam("roleIds") Long roleIds,
                                      @RequestParam("workBnechId") Long workBnechId) {
//        System.out.println("work" + workBnechId);
        List<Long> longs = new ArrayList<>();
        longs.add(roleIds);
        int count = uacAdminService.updateRole(adminId, longs, workBnechId);
        return CommonResult.success(count);
    }

    private String saveResForCooke(HttpServletRequest request,
                                   HttpServletResponse response, Long adminId, List<Long> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return StringUtils.EMPTY;
        }
        String username;
        String password;
        try {
            String res = getCookie(request, "resam");
            AutologinUtil autologinUtil = new AutologinUtil();

            if (StringUtils.isBlank(res)) {
                UacAdmin uacAdminEntity = uacAdminService.getItem(adminId);
                username = uacAdminEntity.getUsername();
                password = uacAdminEntity.getUsername();
            } else {
                username = autologinUtil.decodeUsername(res);
                password = autologinUtil.decodePassword(res);
            }

            List<UacRole> uacRoleEntityList = uacRoleService.listAll(roleIds);
            List<UacRole> uacRoleList = uacRoleEntityList.stream().map(r -> {
                UacRole role = new UacRole();
                role.setId(r.getId());
                role.setName(r.getName());
                return role;
            }).collect(Collectors.toList());

            String userroles = userroles(uacRoleList);
            String isAdminUser = isAdminUser(uacRoleList);
            String encode = autologinUtil.encode(
                    username,
                    password,
                    StringUtils.isNotBlank(userroles) ? userroles : (isAdminUser.equals("1") ? "客服主管" : StringUtils.SPACE),
                    isAdminUser);
            String urlencode = URLEncoder.encode(encode, "UTF-8");
            Cookie c = new Cookie("resam", urlencode);
            response.addCookie(c);
            return urlencode;
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        return StringUtils.SPACE;
    }

    private String getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    //@ApiOperation("获取指定用户的角色")
    @RequestMapping(value = "/role/{adminId}", method = RequestMethod.GET)
    public CommonResult<List<UacRole>> getRoleList(@PathVariable Long adminId) {
        List<UacRole> roleList = uacAdminService.getRoleList(adminId);
        return CommonResult.success(roleList);
    }

    //@ApiOperation("服务平台获取指定用户的角色")
    @RequestMapping(value = "/hmm/role", method = RequestMethod.GET)
    public CommonResult<List<UacRole>> getHmmRoleList(@RequestParam Long adminId, Long organ, @RequestParam Long workBnechId, @RequestParam Long memberId) {
        List<UacRole> roleList = uacAdminService.getHmmRoleList(adminId, organ, workBnechId, memberId);
        return CommonResult.success(roleList);
    }

    //@ApiOperation("服务平台给员工分配角色")
    @RequestMapping(value = "/staff/update", method = RequestMethod.GET)
    public CommonResult staffUpdate(@RequestParam Long staffId, String deptId, String roleId, @RequestParam("workBnechId") Long workBnechId, @RequestParam Long memberId) {
        UacAdmin byStaffId = uacAdminService.findByStaffId(staffId);
        if (null == byStaffId) {
            return CommonResult.failed("该员工没有登录过官网，不能被分配角色");
        }

        uacAdminService.delectAdminRole(byStaffId.getId(), memberId, workBnechId);
        uacAdminService.staffUpdate(byStaffId.getId(), roleId, workBnechId, memberId, deptId);
        return CommonResult.success("ok");
    }


}
