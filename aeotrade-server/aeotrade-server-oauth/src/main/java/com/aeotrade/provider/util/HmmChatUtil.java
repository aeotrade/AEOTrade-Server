package com.aeotrade.provider.util;

import com.aeotrade.provider.dto.WxTokenDto;
import com.aeotrade.provider.dto.WxUserinfoDto;
import com.aeotrade.utlis.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @Author: yewei
 * @Date: 2020/6/29 14:45
 */
@Slf4j
@Component
public class HmmChatUtil {
    /**
     * 回调域名 member.aeotrade.com
     * */
    private static final String appid ="wxa005a706f33a850d";

    private static final String callBack ="";

    private static final String scope ="";

    private static final String appsecret ="828d9a6d6b8ebeea0cd67eddfd4cca05";

    private static String WX_QRCONNECT_URL="https://open.weixin.qq.com/connect/qrconnect?appid=APPID&redirect_uri=REDIRECT_URI&response_type=code&scope=SCOPE&state=STATE#wechat_redirect";
    private static String WX_ACCESS_TOKEN_URL_="https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
    private static String WX_USERINFO_URL="https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID";

    public  WxTokenDto getWxTokenDto(String code) throws Exception {
        log.info("获取access_token-------------------------------------------------------------");
        log.info("appID"+appid);
        log.info("appsecret"+appsecret);
        //1.通过code获取access_token
        String url = WX_ACCESS_TOKEN_URL_.replace("APPID",appid).replace("SECRET",appsecret).replace("CODE",code);
        String tokenInfoStr =  HttpRequestUtils.httpGet(url,null,null);
        if (!StringUtils.containsAny(tokenInfoStr,"openid")){
            throw new RuntimeException("code 过期，请重试。。。");
        }
        return JacksonUtil.parseJson(tokenInfoStr, WxTokenDto.class);
    }

    public WxUserinfoDto getWxUserinfoDto(String accessToken, String openid) throws Exception{
        log.info("获取用户信息------------------------------------------------------------------");
        log.info("appID"+appid);
        log.info("appsecret"+appsecret);
        //2.通过access_token和openid获取用户信息
        String userInfoUrl = WX_USERINFO_URL.replace("ACCESS_TOKEN",accessToken).replace("OPENID",openid);
        String userInfoStr =  HttpRequestUtils.httpGet(userInfoUrl,null,null);
        return JacksonUtil.parseJson(userInfoStr, WxUserinfoDto.class);
        // {"openid":"ovgOr0uEuhZun0bFrU5Dtso4VERo","nickname":"收割机","sex":1,"language":"zh_CN","city":"Chaoyang","province":"Beijing","country":"CN","headimgurl":"http:\/\/thirdwx.qlogo.cn\/mmopen\/vi_32\/mj81bLic2bRV977HxKoS8FARaAvodicw2m1WaicI9DSl67PRFgj22dkicyBKGFtOqwXua9peMhr4bXVw4XiaWtT9cyw\/132","privilege":[],"unionid":"oX7qnt7l5wvVmsfmqT12Z3vXSuSA"}
        //log.info("userInfoObject:{}",userInfoStr);
    }
}
