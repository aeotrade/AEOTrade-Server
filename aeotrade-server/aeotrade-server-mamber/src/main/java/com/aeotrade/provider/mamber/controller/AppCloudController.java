package com.aeotrade.provider.mamber.controller;

import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.mamber.entity.*;
import com.aeotrade.provider.mamber.service.*;
import com.aeotrade.service.MqSend;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.PageList;
import com.aeotrade.suppot.RespResult;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 * 应用表 前端控制器
 * </p>
 *
 * @author 叶威
 * @since 2022-05-30
 */
@RestController
@RequestMapping("/app/cloud")
public class AppCloudController extends BaseController {
    @Autowired
    private AppCloudService appCloudService;
    @Autowired
    private RedisTemplate<String, String> stringStringRedisTemplate;
    @Autowired
    private UacOauthClientDetailsService uacOauthClientDetailsService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MqSend mqSend;
    @Value("${hmtx.oauthurl:https://www.aeotrade.com/aeoapi/v1/oauth/authorize?client_id=}")
    private String oauthUrl;


    /**
     * 添加自建应用
     * @param appCloud
     * @return
     */
    @PostMapping(value = "/save", name = "添加自建应用")
    public RespResult saveApp(@RequestBody AppCloud appCloud) {
        try {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String clientId = UUID.nameUUIDFromBytes((appCloud.getAppName() + timestamp).getBytes()).toString().replace("-", "").substring(0, 9);
            String clientSecret = UUID.randomUUID().toString().replace("-", "");
            appCloud.setCreatedTime(LocalDateTime.now());
            appCloud.setUpdatedTime(LocalDateTime.now());
            appCloud.setRevision(0);
            appCloud.setPublishStatus(0);
            appCloud.setAppType(1);
            appCloud.setClientId(clientId);
            UacOauthClientDetails uacOauthClientDetails = new UacOauthClientDetails();
            uacOauthClientDetails.setClientId(clientId);
            uacOauthClientDetails.setResourceIds("oautn2-resource");
            uacOauthClientDetails.setClientSecret(passwordEncoder.encode(clientSecret));
            uacOauthClientDetails.setScope("read");
            uacOauthClientDetails.setAuthorizedGrantTypes("authorization_code");
            uacOauthClientDetails.setWebServerRedirectUri("1");
            uacOauthClientDetails.setAuthorities("ROLE_MEMBER");
            uacOauthClientDetails.setAccessTokenValidity(60L);
            uacOauthClientDetails.setRefreshTokenValidity(60L);
            uacOauthClientDetails.setAutoapprove("true");
            uacOauthClientDetailsService.save(uacOauthClientDetails);
            appCloudService.save(appCloud);
            stringStringRedisTemplate.opsForValue().set("clientSecret:" + clientId, clientSecret);
            Map<String, Object> map = new HashMap<>();
            map.put("id", appCloud.getId());
            map.put("clientId", clientId);

            return handleResult(map);
        } catch (Exception e) {
            return handleFail("添加失败");
        }
    }

    /**
     * 根据clientid重置密码
     * @param client
     * @return
     */
    @GetMapping("/secret")
    public RespResult secret(@RequestParam String client) {
        try {
            String oauthSecret = UUID.randomUUID().toString().replace("-", "");
            String encode = passwordEncoder.encode(oauthSecret);
            uacOauthClientDetailsService.lambdaUpdate().eq(UacOauthClientDetails::getClientId, client)
                    .set(UacOauthClientDetails::getClientSecret, encode).update();
            stringStringRedisTemplate.delete("oauth_client_details:" + client);
            return handleResult(oauthSecret);
        } catch (Exception e) {
            return handleFail("查询失败");
        }
    }

    /**
     * 根据clientid获取secret
     * @param client
     * @return
     */
    @GetMapping("/get/secret")
    public RespResult GetSecret(@RequestParam String client) {
        try {
            String Secret = stringStringRedisTemplate.opsForValue().get("clientSecret:" + client);
            stringStringRedisTemplate.delete("clientSecret:" + client);
            return handleResult(Secret);
        } catch (Exception e) {
            return handleFail("查询失败");
        }
    }

    /**
     * 根据id删除应用
     * @param id
     * @return
     */
    @GetMapping("/delete")
    public RespResult deleteById(@RequestParam Long id) {
        try {
            AppCloud appCloud = appCloudService.getById(id);
            if (appCloud == null) {
                return handleFail("根据ID未查询到应用");
            }
            appCloudService.removeById(appCloud);

            uacOauthClientDetailsService.lambdaUpdate().eq(UacOauthClientDetails::getClientId, appCloud.getClientId()).remove();

            stringStringRedisTemplate.delete("oauth_client_details:" + appCloud.getClientId());
            return handleOK();
        } catch (Exception e) {
            return handleFail("查询失败");
        }
    }

    /**
     * 根据id获取应用信息返回重定向地址
     * @param id
     * @return
     */
    @GetMapping("/skip/url")
    public RespResult skip(@RequestParam Long id) {
        try {
            AppCloud appCloud = appCloudService.getById(id);
            if (null != appCloud.getIsRequertType()) {
                if (appCloud.getIsRequertType() == 1) {
                    return handleResult(appCloud.getUrl());
                }
                if (appCloud.getIsRequertType() == 2) {
                    if (StringUtils.isEmpty(appCloud.getClientId()) || StringUtils.isEmpty(appCloud.getUrl())) {
                        return handleFail("该应用未完成oauth2.0协议对接，暂无法使用");
                    }
                    String url =
                            oauthUrl + appCloud.getClientId() + "&redirect_uri="
                                    + URLEncoder.encode(appCloud.getUrl(), StandardCharsets.UTF_8)
                                    + "&response_type=code&scope=read";
                    if (StringUtils.isNotEmpty(appCloud.getState())) {
                        url = url + "&state=" + URLEncoder.encode(appCloud.getState(), StandardCharsets.UTF_8) + "?clientId=" + appCloud.getClientId();
                    }
                    return handleResult(url);
                }
            }
            return handleFail("非自建应用无法调用该接口");
        } catch (Exception e) {
            return handleFail("获取失败");
        }
    }

    /**
     * 获取当前企业所有自建应用
     * @param memberId
     * @param sort
     * @param name
     * @param pageSize
     * @param pageNo
     * @return
     */
    @GetMapping("/find/cloud")
    public RespResult findcloud(@RequestParam Long memberId, @RequestParam String sort, String name,
                                @RequestParam Integer pageSize, @RequestParam Integer pageNo) {
        try {
            LambdaQueryWrapper<AppCloud> appCloudLambdaQueryWrapper = new LambdaQueryWrapper<>();
            appCloudLambdaQueryWrapper.eq(AppCloud::getMemberId, memberId)
                    .eq(AppCloud::getAppType, 1)
                    .eq(AppCloud::getStatus, 0);
            if (org.springframework.util.StringUtils.hasText(name)) {
                appCloudLambdaQueryWrapper.like(AppCloud::getAppName, name);
            }
            if (org.springframework.util.StringUtils.hasText(sort) && sort.toLowerCase(Locale.ROOT).equals("desc")) {
                appCloudLambdaQueryWrapper.orderByDesc(AppCloud::getSort);
            } else {
                appCloudLambdaQueryWrapper.orderByAsc(AppCloud::getSort);
            }
            Page<AppCloud> page = appCloudService.page(new Page<>(pageNo, pageSize), appCloudLambdaQueryWrapper);

            PageList<AppCloud> list = new PageList<>();
            list.setTotalSize(page.getTotal());
            list.setRecords(page.getRecords());
            list.setSize(page.getSize());
            list.setCurrent(page.getCurrent());
            return handleResultList(list);
        } catch (Exception e) {
            return handleFail("获取失败");
        }
    }

    /**
     * 根据id获取应用信息
     * @param id
     * @return
     */
    @GetMapping("/findById")
    public RespResult findById(@RequestParam Long id) {
        try {
            AppCloud appCloud = appCloudService.getById(id);
            return handleResult(appCloud);
        } catch (Exception e) {
            return handleFail("查询失败");
        }
    }

    /**
     * 自建应用修改
     *
     * @param appCloud
     * @return
     */
    @PostMapping("/update")
    public RespResult updateApp(@RequestBody AppCloud appCloud) {
        if (appCloud == null || appCloud.getId() == null) {
            throw new AeotradeException("内容不能为空");
        }
        try {
            if (StringUtils.isNotEmpty(appCloud.getClientId()) && appCloud.getIsRequertType() == 2) {
                uacOauthClientDetailsService.lambdaUpdate().set(UacOauthClientDetails::getWebServerRedirectUri, appCloud.getUrl())
                        .eq(UacOauthClientDetails::getClientId, appCloud.getClientId()).update();
            }
            appCloud.setRevision(0);
            appCloud.setUpdatedTime(LocalDateTime.now());
            appCloudService.updateById(appCloud);
            return handleOK();
        } catch (Exception e) {
            return handleFail("修改失败");
        }
    }

}
