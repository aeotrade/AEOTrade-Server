package com.aeotrade.provider.oauth.config;

import com.aeotrade.provider.oauth.authorizationCode.AeotradeAccessTokenConverter;
import com.aeotrade.provider.oauth.authorizationCode.AeotradeAuthorizationCodeTokenGranter;
import com.aeotrade.provider.oauth.authorizationCode.AeotradeJwtAccessTokenConverter;
import com.aeotrade.provider.oauth.img.ImgTokenGranter;
import com.aeotrade.provider.oauth.refreshStaffToken.RefreshStaffTokenGranter;
import com.aeotrade.provider.oauth.service.*;
import com.aeotrade.provider.oauth.single.SingleTokenGranter;
import com.aeotrade.provider.oauth.sms.AeotradeCompositeTokenGranter;
import com.aeotrade.provider.oauth.sms.MobileTokenGranter;
import com.aeotrade.provider.oauth.wx.OpenIdGranter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.CompositeTokenGranter;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.client.ClientCredentialsTokenGranter;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeTokenGranter;
import org.springframework.security.oauth2.provider.implicit.ImplicitTokenGranter;
import org.springframework.security.oauth2.provider.password.ResourceOwnerPasswordTokenGranter;
import org.springframework.security.oauth2.provider.refresh.RefreshTokenGranter;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.*;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.util.FileCopyUtils;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableAuthorizationServer
public class OauthConfig extends AuthorizationServerConfigurerAdapter {
    @Value("${hmtx.oauth.token.jks-file:aeotrade.jks}")
    private String jksFile;
    @Value("${hmtx.oauth.token.jks-pwd:hmtx20191001}")
    private String jksPwd;
    @Value("${hmtx.oauth.token.crt-file:aeotrade.crt}")
    private String crtFile;
    @Value("${hmtx.oauth.token.alias:aeotrade}")
    private String alias;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private IValidateCodeService iValidateCodeService;
    @Autowired
    private RedisClientDetailsService clientDetailsService;
    @Autowired
    private UserDetailsService userDetailsService;
    /** 控制授权码回调地址是否校验。默认 false:不校验 */
    @Value("${hmtx.oauth2.check_redirect_url:false}")
    private Boolean checkRedirectUrlRule;
    @Value("${hmtx.jwt.aes-crypto:hahahahahahahaha}")
    private String jwtAESCrypto;
    @Value("${hmtx.oauth2.unused-url-encode-clients:hengshih}")
    private String[] unusedUrlEncodeClients;

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
        security.allowFormAuthenticationForClients();
        security.checkTokenAccess("permitAll()");
        security.tokenKeyAccess("permitAll()");
        security.passwordEncoder(passwordEncoder);
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        /**
         * 从数据库读取客户端配置
         clients.withClientDetails(clientDetailsService()); */
        /**从redis读取*/
        clients.withClientDetails(clientDetailsService);
        clientDetailsService.loadAllClientToCache();

    }

 /* 数据库读取客户端的配置
    private ClientDetailsService clientDetailsService() {
        AeotradeJdbcClientDetailsService aeotradeJdbcClientDetailsService = new AeotradeJdbcClientDetailsService(dataSource);
        aeotradeJdbcClientDetailsService.setPasswordEncoder(passwordEncoder);
        return aeotradeJdbcClientDetailsService;
    }*/

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {

        // 初始化所有的TokenGranter，并且类型为CompositeTokenGranter
        List<TokenGranter> defaultTokenGranters = this.getDefaultTokenGranters(endpoints);
        //添加手机号模式
        defaultTokenGranters.add(new MobileTokenGranter(authenticationManager, endpoints.getTokenServices(),
                endpoints.getClientDetailsService(), endpoints.getOAuth2RequestFactory()));
        //添加北京单一窗口用户唯一标识模式
        defaultTokenGranters.add(new SingleTokenGranter(authenticationManager, endpoints.getTokenServices(),
                endpoints.getClientDetailsService(), endpoints.getOAuth2RequestFactory()));
        //添加微信模式
        defaultTokenGranters.add(new OpenIdGranter(authenticationManager, endpoints.getTokenServices(),
                endpoints.getClientDetailsService(), endpoints.getOAuth2RequestFactory()));
        //添加用户名密码加图片验证码模式
        defaultTokenGranters.add(new ImgTokenGranter(authenticationManager, endpoints.getTokenServices(),
                endpoints.getClientDetailsService(), endpoints.getOAuth2RequestFactory(), iValidateCodeService));
        //添加切换企业获取令牌
        defaultTokenGranters.add(new RefreshStaffTokenGranter(authenticationManager, endpoints.getTokenServices(),
                endpoints.getClientDetailsService(), endpoints.getOAuth2RequestFactory()));
        endpoints.tokenGranter(new CompositeTokenGranter(defaultTokenGranters));
        endpoints
                .tokenStore(tokenStore())    /**采用JWT非对称加密*/
                .authenticationManager(authenticationManager)
                .userDetailsService(userDetailsService)
                .allowedTokenEndpointRequestMethods(HttpMethod.POST, HttpMethod.GET);
        //.exceptionTranslator(exceptionTranslator);
        TokenEnhancerChain enhancerChain = new TokenEnhancerChain();
        List<TokenEnhancer> enhancerList = new ArrayList<>();
        enhancerList.add(tokenEnhancer());
        enhancerList.add(jwtAccessTokenConverter());
        enhancerChain.setTokenEnhancers(enhancerList);
        endpoints
                .tokenEnhancer(enhancerChain)
                .accessTokenConverter(jwtAccessTokenConverter());
        RedirectResolver redirectResolver = new RedirectResolver();
        redirectResolver.setCheckRedirectUrlRule(checkRedirectUrlRule);
        redirectResolver.setUnusedUrlEncodeClients(unusedUrlEncodeClients);
        endpoints.redirectResolver(redirectResolver);
        new AeotradeCompositeTokenGranter(defaultTokenGranters);
    }


    private TokenEnhancer tokenEnhancer() {
        return new TokenJwtEnhancer();
    }

    @Bean
    @Primary
    public DefaultTokenServices defaultTokenServices() {
        DefaultTokenServices tokenServices = new DefaultTokenServices();
        tokenServices.setReuseRefreshToken(false);
        tokenServices.setTokenStore(tokenStore());
        tokenServices.setSupportRefreshToken(true);
        tokenServices.setClientDetailsService(clientDetailsService);
        tokenServices.setTokenEnhancer(jwtAccessTokenConverter());
        return tokenServices;
    }

    /**
     * 采用JWT非对称加密
     */
    private TokenStore tokenStore() {
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    @Bean
    public KeyPair keyPair() {
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(
                new ClassPathResource(jksFile),jksPwd.toCharArray());
        return keyStoreKeyFactory.getKeyPair(alias);
    }

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        AeotradeJwtAccessTokenConverter converter = new AeotradeJwtAccessTokenConverter();
        converter.setKeyPair(keyPair());
        converter.setAccessTokenConverter(new AeotradeAccessTokenConverter(jwtAESCrypto));

        Resource resouce = new ClassPathResource(crtFile);
        String publicKey = null;
        try {
            publicKey = new String(FileCopyUtils.copyToByteArray(resouce.getInputStream()), "utf-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        converter.setVerifierKey(publicKey);
        converter.setVerifier(new RsaVerifier(publicKey));

        return converter;
    }

    @Bean
    public ResourceOwnerPasswordTokenGranter resourceOwnerPasswordTokenGranter(
            @Autowired AuthenticationManager authenticationManager,
            @Autowired OAuth2RequestFactory oAuth2RequestFactory) {
        DefaultTokenServices defaultTokenServices = defaultTokenServices();
        defaultTokenServices.setTokenEnhancer(jwtAccessTokenConverter());
        return new ResourceOwnerPasswordTokenGranter(authenticationManager, defaultTokenServices, clientDetailsService, oAuth2RequestFactory);
    }

    @Bean
    public MobileTokenGranter mobileTokenGranter(@Autowired AuthenticationManager authenticationManager, @Autowired OAuth2RequestFactory oAuth2RequestFactory) {
        DefaultTokenServices defaultTokenServices = defaultTokenServices();
        defaultTokenServices.setTokenEnhancer(jwtAccessTokenConverter());
        return new MobileTokenGranter(authenticationManager, defaultTokenServices, clientDetailsService, oAuth2RequestFactory);
    }

    @Bean
    public SingleTokenGranter singleTokenGranter(@Autowired AuthenticationManager authenticationManager, @Autowired OAuth2RequestFactory oAuth2RequestFactory) {
        DefaultTokenServices defaultTokenServices = defaultTokenServices();
        defaultTokenServices.setTokenEnhancer(jwtAccessTokenConverter());
        return new SingleTokenGranter(authenticationManager, defaultTokenServices, clientDetailsService, oAuth2RequestFactory);
    }

    @Bean
    public OpenIdGranter openIdGranter(@Autowired AuthenticationManager authenticationManager, @Autowired OAuth2RequestFactory oAuth2RequestFactory) {
        DefaultTokenServices defaultTokenServices = defaultTokenServices();
        defaultTokenServices.setTokenEnhancer(jwtAccessTokenConverter());
        return new OpenIdGranter(authenticationManager, defaultTokenServices, clientDetailsService, oAuth2RequestFactory);
    }

    @Bean
    public RefreshStaffTokenGranter refreshStaffTokenGranter(@Autowired AuthenticationManager authenticationManager, @Autowired OAuth2RequestFactory oAuth2RequestFactory) {
        DefaultTokenServices defaultTokenServices = defaultTokenServices();
        defaultTokenServices.setTokenEnhancer(jwtAccessTokenConverter());
        return new RefreshStaffTokenGranter(authenticationManager, defaultTokenServices, clientDetailsService, oAuth2RequestFactory);
    }

    @Bean
    public DefaultOAuth2RequestFactory oAuth2RequestFactory() {
        return new DefaultOAuth2RequestFactory(clientDetailsService);
    }

    /**
     * 初始化所有的TokenGranter
     */
    private List<TokenGranter> getDefaultTokenGranters(AuthorizationServerEndpointsConfigurer endpoints) {

        ClientDetailsService clientDetails = endpoints.getClientDetailsService();
        AuthorizationServerTokenServices tokenServices = endpoints.getTokenServices();
        AuthorizationCodeServices authorizationCodeServices = endpoints.getAuthorizationCodeServices();
        OAuth2RequestFactory requestFactory = endpoints.getOAuth2RequestFactory();

        List<TokenGranter> tokenGranters = new ArrayList<>();
        // 添加授权码模式
        tokenGranters.add(new AeotradeAuthorizationCodeTokenGranter(tokenServices, authorizationCodeServices, clientDetails, requestFactory, checkRedirectUrlRule));
        // 添加刷新令牌的模式
        tokenGranters.add(new RefreshTokenGranter(tokenServices, clientDetails, requestFactory));
        ImplicitTokenGranter implicit = new ImplicitTokenGranter(tokenServices, clientDetails, requestFactory);
        // 添加隐式授权模式
        tokenGranters.add(implicit);
        // 添加客户端模式
        tokenGranters.add(new ClientCredentialsTokenGranter(tokenServices, clientDetails, requestFactory));
        if (authenticationManager != null) {
            // 添加密码模式
            tokenGranters.add(new ResourceOwnerPasswordTokenGranter(authenticationManager, tokenServices,
                    clientDetails, requestFactory));
        }
        return tokenGranters;
    }

}
