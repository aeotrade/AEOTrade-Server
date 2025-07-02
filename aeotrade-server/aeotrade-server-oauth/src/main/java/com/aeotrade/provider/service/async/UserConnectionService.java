package com.aeotrade.provider.service.async;

import com.aeotrade.base.constant.AeoConstant;
import com.aeotrade.base.constant.BizConstant;
import com.aeotrade.provider.dto.WxTokenDto;
import com.aeotrade.provider.dto.WxUserinfoDto;
import com.aeotrade.provider.dto.WxTencentDto;
import com.aeotrade.provider.mapper.UacAdminMapper;
import com.aeotrade.provider.mapper.UacUserConnectionMapper;
import com.aeotrade.provider.model.UacAdmin;
import com.aeotrade.provider.model.UacStaff;
import com.aeotrade.provider.model.UacUserConnection;
import com.aeotrade.provider.oauth.single.SingleTokenGranter;
import com.aeotrade.provider.oauth.sms.MobileTokenGranter;
import com.aeotrade.provider.oauth.wx.OpenIdGranter;
import com.aeotrade.provider.service.UacMemberStaffService;
import com.aeotrade.provider.service.UacUserConnectionService;
import com.aeotrade.provider.service.feign.MamberFeign;
import com.aeotrade.service.RedisService;
import com.aeotrade.utlis.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.password.ResourceOwnerPasswordTokenGranter;
import org.springframework.stereotype.Service;

import javax.annotation.Generated;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Generated("titan.lightbatis.web.generate.ServiceBeanSerializer")
@Service
@Slf4j
public class UserConnectionService {
    @Autowired
    private UacUserConnectionService uacUserConnectionService;
    @Autowired
    private ClientDetailsService clientDetailsService;
    @Autowired
    private ResourceOwnerPasswordTokenGranter granter;

    @Autowired
    private MobileTokenGranter mgranter;
    @Autowired
    private SingleTokenGranter singleTokenGranter;
    @Autowired
    private OpenIdGranter openIdGranter;
    @Autowired
    private UacUserConnectionMapper uacUserConnectionMapper;
    @Autowired
    private UacAdminMapper uacAdminMapper;
    @Autowired
    private MamberFeign mamberFeign;
    @Autowired
    private UacMemberStaffService uacMemberStaffService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Async(AeoConstant.ASYNC_POOL)
    public Future<UacUserConnection> inorupdateUacUserConnection(WxTokenDto wxTokenDto, WxUserinfoDto wxUserinfoDto, UacUserConnection userConnection){
        log.info("微信数据结果______________________________________________"+userConnection);
        //abstractTokenGranter.grant()
        if (userConnection==null){
            userConnection=new UacUserConnection();
            userConnection.setProviderUserId(wxTokenDto.getOpenid());
            userConnection.setProviderId(BizConstant.ClientEnum.WECHAT.toString().toLowerCase());
            userConnection.setDisplayName(wxUserinfoDto.getNickname());
            userConnection.setImageUrl(wxUserinfoDto.getHeadimgurl());
            userConnection.setUnionid(wxUserinfoDto.getUnionid());
            userConnection.setAccessToken(wxTokenDto.getAccess_token());
            userConnection.setRefreshToken(wxTokenDto.getRefresh_token());
            userConnection.setExpireTime(DateUtil.addSecondToLocalDateTime(wxTokenDto.getExpires_in()));
            uacUserConnectionService.save(userConnection);
            /**新用户添加*/
            UacStaff uacStaff=uacMemberStaffService.initUacStaffAndUacMember(userConnection,null,null,null);
            userConnection.setStaffId(uacStaff.getId());

        }else {
            UacUserConnection uccn=new UacUserConnection();
            BeanUtils.copyProperties(userConnection,uccn);
            uccn.setId(userConnection.getId());
            uccn.setProviderUserId(wxTokenDto.getOpenid());
            uccn.setProviderId(BizConstant.ClientEnum.WECHAT.toString().toLowerCase());
            uccn.setDisplayName(wxUserinfoDto.getNickname());
            uccn.setImageUrl(wxUserinfoDto.getHeadimgurl());
            uccn.setUnionid(wxUserinfoDto.getUnionid());
            uccn.setAccessToken(wxTokenDto.getAccess_token());
            uccn.setRefreshToken(wxTokenDto.getRefresh_token());
            uccn.setExpireTime(DateUtil.addSecondToLocalDateTime(wxTokenDto.getExpires_in()));
            uacUserConnectionService.updateById(uccn);
            userConnection=uccn;
        }
        return new AsyncResult<UacUserConnection>(userConnection);
    }

    @Async(AeoConstant.ASYNC_POOL)
    public Future<UacUserConnection> subUserConnection(WxTokenDto wxTokenDto,WxUserinfoDto wxUserinfoDto,
                                                       UacUserConnection userConnection,
                                                       Long pStaffId,Long pMemberId){
        if (userConnection==null){
            userConnection=new UacUserConnection();
            userConnection.setProviderUserId(wxUserinfoDto.getOpenid());
            userConnection.setProviderId(BizConstant.ClientEnum.WECHAT.toString().toLowerCase());
            userConnection.setDisplayName(wxUserinfoDto.getNickname());
            userConnection.setImageUrl(wxUserinfoDto.getHeadimgurl());
            userConnection.setUnionid(wxUserinfoDto.getUnionid());
            userConnection.setAccessToken(wxTokenDto.getAccess_token());
            userConnection.setRefreshToken(wxTokenDto.getRefresh_token());
            userConnection.setExpireTime(DateUtil.addSecondToLocalDateTime(wxTokenDto.getExpires_in()));
            uacUserConnectionService.save(userConnection);
            UacStaff uacStaff=uacMemberStaffService.initUacStaffAndUacMember(userConnection,pStaffId,pMemberId,null);
            userConnection.setStaffId(uacStaff.getId());

        }else {
            UacUserConnection uccn=new UacUserConnection();
            BeanUtils.copyProperties(userConnection,uccn);
            uccn.setId(userConnection.getId());
            uccn.setProviderUserId(wxTokenDto.getOpenid());
            uccn.setProviderId(BizConstant.ClientEnum.WECHAT.toString().toLowerCase());
            uccn.setDisplayName(wxUserinfoDto.getNickname());
            uccn.setImageUrl(wxUserinfoDto.getHeadimgurl());
            uccn.setUnionid(wxUserinfoDto.getUnionid());
            uccn.setAccessToken(wxTokenDto.getAccess_token());
            uccn.setRefreshToken(wxTokenDto.getRefresh_token());
            uccn.setExpireTime(DateUtil.addSecondToLocalDateTime(wxTokenDto.getExpires_in()));
            uccn.setCreatedTime(LocalDateTime.now());
            uacUserConnectionService.updateById(uccn);
            uacMemberStaffService.initUacStaffMember(userConnection.getStaffId(),pMemberId);
            userConnection=uccn;
        }
        return new AsyncResult<UacUserConnection>(userConnection);
    }

    @Async(AeoConstant.ASYNC_POOL)
    public Future<OAuth2AccessToken> getOAuth2AccessToken(){
        ClientDetails clientDetails=clientDetailsService.loadClientByClientId("wechat");

        Map<String, String> requestParameters = new HashMap<>(3);
        requestParameters.put("grant_type", "password");
        requestParameters.put("username", "admin");
        requestParameters.put("password", "123456");
        String grantTypes = String.join(",", clientDetails.getAuthorizedGrantTypes());
        TokenRequest tokenRequest = new TokenRequest(requestParameters, clientDetails.getClientId(), clientDetails.getScope(), grantTypes);
        return new AsyncResult<OAuth2AccessToken>(redisOauth2Token("1","web",granter.grant("password", tokenRequest)));
    }


    public OAuth2AccessToken findOAuth2AccessTokenBy(String staffId,String LoginType,String username,String password){
        ClientDetails clientDetails=clientDetailsService.loadClientByClientId("wechat");
        Map<String, String> requestParameters = new HashMap<>(3);
        requestParameters.put("grant_type", "password");
        requestParameters.put("username", username);
        requestParameters.put("password", password);
        String grantTypes = String.join(",", clientDetails.getAuthorizedGrantTypes());
        TokenRequest tokenRequest = new TokenRequest(requestParameters, clientDetails.getClientId(), clientDetails.getScope(), grantTypes);

        OAuth2AccessToken oAuth2AccessToken = granter.grant("password", tokenRequest);

        return redisOauth2Token(staffId,LoginType,oAuth2AccessToken);
    }
    public Future<OAuth2AccessToken> findMobile(String staffId,String LoginType){
        LambdaQueryWrapper<UacAdmin> uacAdminLambdaQueryWrapper=new LambdaQueryWrapper<>();
        uacAdminLambdaQueryWrapper.eq(UacAdmin::getStaffId,staffId).eq(UacAdmin::getStatus,1).eq(UacAdmin::getIsTab,1);
        UacAdmin byStaffId = uacAdminMapper.selectList(uacAdminLambdaQueryWrapper).get(0);
        redisTemplate.opsForValue().set(AeoConstant.SMSREDIS_KEY + byStaffId.getMobile(), "888888", 80, TimeUnit.SECONDS);
        ClientDetails clientDetails=clientDetailsService.loadClientByClientId("mobile");
        Map<String, String> requestParameters = new HashMap<>(3);
        requestParameters.put("grant_type", "cms_code");
        requestParameters.put("mobile", byStaffId.getMobile());
        requestParameters.put("cms_code", "888888");
        requestParameters.put("client_id", "mobile");
        requestParameters.put("client_secret", "secret");
        String grantTypes = "cms_code";
        TokenRequest tokenRequest = new TokenRequest(requestParameters, clientDetails.getClientId(), clientDetails.getScope(), grantTypes);
        //return granter.grant("password", tokenRequest);
        OAuth2AccessToken grant = mgranter.grant("cms_code", tokenRequest);
        return new AsyncResult<OAuth2AccessToken>(grant);
    }
    public OAuth2AccessToken findByMobile(String staffId,String LoginType,String mobile,String code){
        ClientDetails clientDetails=clientDetailsService.loadClientByClientId("mobile");

        Map<String, String> requestParameters = new HashMap<>(3);
        requestParameters.put("grant_type", "cms_code");
        requestParameters.put("mobile", mobile);
        requestParameters.put("cms_code", code);
        requestParameters.put("client_id", "mobile");
        requestParameters.put("client_secret", "secret");
        String grantTypes = "cms_code";
        TokenRequest tokenRequest = new TokenRequest(requestParameters, clientDetails.getClientId(), clientDetails.getScope(), grantTypes);
        OAuth2AccessToken grant = mgranter.grant("cms_code", tokenRequest);
        return redisOauth2Token(staffId,LoginType,grant);
    }

    @Async(AeoConstant.ASYNC_POOL)
    public Future<OAuth2AccessToken> findByOpenId(Long staffId,String loginType){
        ClientDetails clientDetails=clientDetailsService.loadClientByClientId("wx");

        Map<String, String> requestParameters = new HashMap<>(4);
        requestParameters.put("grant_type", "openId");
        requestParameters.put("openId", String.valueOf(staffId));
        requestParameters.put("client_id", "wx");
        requestParameters.put("client_secret", "wxsecret");
        String grantTypes = "cms_code";
        TokenRequest tokenRequest = new TokenRequest(requestParameters, clientDetails.getClientId(), clientDetails.getScope(), grantTypes);
        return new AsyncResult<OAuth2AccessToken>(redisOauth2Token(String.valueOf(staffId),loginType,openIdGranter.grant("openId", tokenRequest)));
    }

    public Future<OAuth2AccessToken> findAeoTokenByMobileWxLogin(Long staffId,String loginType){
        ClientDetails clientDetails=clientDetailsService.loadClientByClientId("wx");

        Map<String, String> requestParameters = new HashMap<>(4);
        requestParameters.put("grant_type", "openId");
        requestParameters.put("openId", String.valueOf(staffId));
        requestParameters.put("client_id", "wx");
        requestParameters.put("client_secret", "wxsecret");
        String grantTypes = "cms_code";
        TokenRequest tokenRequest = new TokenRequest(requestParameters, clientDetails.getClientId(), clientDetails.getScope(), grantTypes);
        return new AsyncResult<OAuth2AccessToken>(redisOauth2Token(String.valueOf(staffId),loginType,openIdGranter.grant("openId", tokenRequest)));
    }

    public Future<UacUserConnection> inorupdateUacUserConnectionTencent(WxTokenDto wxTokenDto, WxTencentDto wxTencentDto, UacUserConnection userConnection) {
        if (userConnection==null){
            userConnection=new UacUserConnection();
            userConnection.setProviderUserId(wxTokenDto.getOpenid());
            userConnection.setProviderId(BizConstant.ClientEnum.WECHAT.toString().toLowerCase());
            userConnection.setDisplayName(wxTencentDto.getNickname());
            userConnection.setImageUrl(wxTencentDto.getHeadimgurl());
            userConnection.setUnionid(wxTencentDto.getUnionid());
            userConnection.setAccessToken(wxTokenDto.getAccess_token());
            userConnection.setRefreshToken(wxTokenDto.getRefresh_token());
            userConnection.setExpireTime(DateUtil.addSecondToLocalDateTime(wxTokenDto.getExpires_in()));
            uacUserConnectionService.save(userConnection);
        }else {
            UacUserConnection uccn=new UacUserConnection();
            BeanUtils.copyProperties(userConnection,uccn);
            uccn.setId(userConnection.getId());
            uccn.setProviderUserId(wxTokenDto.getOpenid());
            uccn.setProviderId(BizConstant.ClientEnum.WECHAT.toString().toLowerCase());
            uccn.setDisplayName(wxTencentDto.getNickname());
            uccn.setImageUrl(wxTencentDto.getHeadimgurl());
            uccn.setUnionid(wxTencentDto.getUnionid());
            uccn.setAccessToken(wxTokenDto.getAccess_token());
            uccn.setRefreshToken(wxTokenDto.getRefresh_token());
            uccn.setExpireTime(DateUtil.addSecondToLocalDateTime(wxTokenDto.getExpires_in()));
            uccn.setCreatedTime(LocalDateTime.now());
            uacUserConnectionService.updateById(uccn);
            userConnection=uccn;
        }
        return new AsyncResult<UacUserConnection>(userConnection);
    }

    public Future<UacUserConnection> inorupdateUacConnectionTencents(WxTokenDto wxTokenDto, WxTencentDto wxTencentDto, UacUserConnection userConnection,Long id) {
        if (userConnection==null){
            userConnection=new UacUserConnection();
            userConnection.setProviderUserId(wxTokenDto.getOpenid());
            userConnection.setProviderId(BizConstant.ClientEnum.WECHAT.toString().toLowerCase());
            userConnection.setDisplayName(wxTencentDto.getNickname());
            userConnection.setImageUrl(wxTencentDto.getHeadimgurl());
            userConnection.setUnionid(wxTencentDto.getUnionid());
            userConnection.setAccessToken(wxTokenDto.getAccess_token());
            userConnection.setRefreshToken(wxTokenDto.getRefresh_token());
            userConnection.setExpireTime(DateUtil.addSecondToLocalDateTime(wxTokenDto.getExpires_in()));
            userConnection.setStaffId(id);
            uacUserConnectionService.save(userConnection);
        }else {
            UacUserConnection uccn=new UacUserConnection();
            BeanUtils.copyProperties(userConnection,uccn);
            uccn.setId(userConnection.getId());
            userConnection.setStaffId(id);
            uccn.setProviderUserId(wxTokenDto.getOpenid());
            uccn.setProviderId(BizConstant.ClientEnum.WECHAT.toString().toLowerCase());
            uccn.setDisplayName(wxTencentDto.getNickname());
            uccn.setImageUrl(wxTencentDto.getHeadimgurl());
            uccn.setUnionid(wxTencentDto.getUnionid());
            uccn.setAccessToken(wxTokenDto.getAccess_token());
            uccn.setRefreshToken(wxTokenDto.getRefresh_token());
            uccn.setExpireTime(DateUtil.addSecondToLocalDateTime(wxTokenDto.getExpires_in()));
            uacUserConnectionService.updateById(uccn);
            userConnection=uccn;
        }
        return new AsyncResult<UacUserConnection>(userConnection);
    }

    public UacUserConnection getAll(Long id) {
        return findUacUserConnectionBy(id);
    }

    public UacUserConnection getlifei(Long id) {
        return uacUserConnectionMapper.selectById(id);
    }

    public UacUserConnection findUacUserConnectionBy(Long staffId) {
        List<UacUserConnection> list = uacUserConnectionService.lambdaQuery()
                .eq(UacUserConnection::getStaffId, staffId).list();
        if(list.size()!=0){
            return list.get(0);
        }else{
            return new UacUserConnection();
        }

    }

    public OAuth2AccessToken findByRes(String res) {
        ClientDetails clientDetails=clientDetailsService.loadClientByClientId("single");

        Map<String, String> requestParameters = new HashMap<>(3);
        requestParameters.put("grant_type", "single_code");
        requestParameters.put("res", res);
        requestParameters.put("client_id", "single");
        requestParameters.put("client_secret", "secret");
        String grantTypes = "cms_code";
        TokenRequest tokenRequest = new TokenRequest(requestParameters, clientDetails.getClientId(), clientDetails.getScope(), grantTypes);
        OAuth2AccessToken grant = singleTokenGranter.grant("single_code", tokenRequest);
        return grant;
    }

    public OAuth2AccessToken redisOauth2Token(String StaffId,String loginType,OAuth2AccessToken oAuth2AccessToken){
        redisService.setOauth2AccessToken(StaffId,loginType,oAuth2AccessToken.getAdditionalInformation().get("jti").toString(),oAuth2AccessToken, Long.valueOf(oAuth2AccessToken.getExpiresIn()));
        return oAuth2AccessToken;
    }
}
