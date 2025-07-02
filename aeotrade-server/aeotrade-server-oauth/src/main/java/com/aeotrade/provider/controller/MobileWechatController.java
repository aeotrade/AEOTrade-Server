package com.aeotrade.provider.controller;

import com.aeotrade.base.constant.BizConstant;
import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.dto.WxTokenDto;
import com.aeotrade.provider.dto.WxUserinfoDto;
import com.aeotrade.provider.model.UacUserConnection;
import com.aeotrade.provider.service.impl.SmweixinService;
import com.aeotrade.provider.service.UacUserConnectionService;
import com.aeotrade.provider.vo.wxUser;
import com.aeotrade.utlis.GetPostUtil;
import com.aeotrade.utlis.JacksonUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 移动应用微信相关接口
 */
@RestController
public class MobileWechatController {

    @Value("${hmtx.weixin.app.appid:wx13dba2ccb22bbd87}")
    private String APPID;
    @Value("${hmtx.weixin.app.appsecret:668802ef1fce1b6a3dbd83eb63172f75}")
    private String SECRET;
    @Autowired
    private UacUserConnectionService uacUserConnectionService;
    @Autowired
    private SmweixinService smweixinService;

    /**
     * 获取微信的access_token
     * 获取第一步的 code 后，请求以下链接获取 access_token：
     * https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
     * 文档地址：https://developers.weixin.qq.com/doc/oplatform/Mobile_App/WeChat_Login/Development_Guide.html
     *
     * @return access_token
     */
    private WxTokenDto weixinAccessToken(String code) throws Exception {
        Map wxAccessToken = JacksonUtil.parseJson(GetPostUtil.sendGet("https://api.weixin.qq.com/sns/oauth2/access_token",
                "appid=" + APPID + "&secret=" + SECRET + "&code=" + code + "&grant_type=authorization_code"), Map.class);
        Object object = wxAccessToken.get("access_token");
        if (object != null) {
            return JacksonUtil.parseJson(JacksonUtil.toJson(wxAccessToken), WxTokenDto.class);
        } else {
            Object errcode = wxAccessToken.get("errcode");
            Object errmsg = wxAccessToken.get("errmsg");
            throw new AeotradeException("获取微信access_token失败: " + errcode + " - " + errmsg);
        }
    }

    /**
     * 获取用户信息
     * 请注意，在用户修改微信头像后，旧的微信头像 URL 将会失效，因此开发者应该自己在获取用户信息后，
     * 将头像图片保存下来，避免微信头像 URL 失效后的异常情况
     *
     * @param access_token
     * @param openid
     * @return WxUserinfoDto
     * @throws Exception
     */
    private WxUserinfoDto weixinUserInfo(String access_token, String openid) throws Exception {
        Map wxUserInfo = JacksonUtil.parseJson(GetPostUtil.sendGet("https://api.weixin.qq.com/sns/userinfo",
                "access_token=" + access_token + "&openid=" + openid), Map.class);
        Object unionid = wxUserInfo.get("unionid");
        if (unionid != null) {
            return JacksonUtil.parseJson(JacksonUtil.toJson(wxUserInfo), WxUserinfoDto.class);
        } else {
            Object errcode = wxUserInfo.get("errcode");
            Object errmsg = wxUserInfo.get("errmsg");
            throw new AeotradeException("获取微信用户信息失败: " + errcode + " - " + errmsg);
        }
    }

    /**
     * 根据微信移动应用开发模式，通过微信code获取慧贸的access_token
     *
     * @param params
     * @return AccessToken
     * @throws Exception
     */
    @PostMapping("/token/wx/code")
    public Object aeotradeAccessToken(@RequestBody Map<String, String> params) throws Exception {
        if (params != null && !params.containsKey("code")) {
            throw new AeotradeException("微信code不能为空");
        }
        String code = params.get("code");
        // 获取微信access_token
        WxTokenDto wxTokenDto = weixinAccessToken(code);
        // 检查当前微信用户是否已经注册
        OAuth2AccessToken oAuth2AccessToken = getAeotradeUser(wxTokenDto);
        // 返回AccessToken

        return oAuth2AccessToken;
    }

    private OAuth2AccessToken getAeotradeUser(WxTokenDto wxTokenDto) throws Exception {
        // 1. 通过unionid添加用户是否注册
        List<UacUserConnection> list = uacUserConnectionService.lambdaQuery()
                .eq(UacUserConnection::getProviderId, BizConstant.ClientEnum.WECHAT.toString().toLowerCase())
                .eq(UacUserConnection::getUnionid, wxTokenDto.getUnionid()).list();
        UacUserConnection userConnection = list.size() > 0 ? list.get(0) : null;
        wxUser wxU = new wxUser();
        if (userConnection == null) {
            // 获取微信用户信息
            WxUserinfoDto wxUserinfoDto = weixinUserInfo(wxTokenDto.getAccess_token(), wxTokenDto.getOpenid());
            // 注册aeotrade用户
            wxU = new wxUser();
            BeanUtils.copyProperties(wxUserinfoDto, wxU);
        }else {
            wxU.setUnionid(userConnection.getUnionid());
            wxU.setOpenid(userConnection.getProviderUserId());
        }
        OAuth2AccessToken oAuth2AccessToken = smweixinService.mobileWeixinLogin(wxU, "wxMobileLogin");
        if (oAuth2AccessToken == null) {
            throw new AeotradeException("获取慧贸账户信息失败");
        }
        return oAuth2AccessToken;
    }
}
