package com.aeotrade.provider.oauth.config;

import com.aeotrade.provider.oauth.service.AeotradeUserDetailsService;
import com.aeotrade.provider.oauth.service.MoblieUserDetailsService;
import com.aeotrade.provider.oauth.service.SingleUserDetailsService;
import com.aeotrade.provider.oauth.service.WxUserDetailsService;
import com.aeotrade.provider.oauth.single.SingleAuthenticationProvider;
import com.aeotrade.provider.oauth.sms.MobileAuthenticationProvider;
import com.aeotrade.provider.oauth.wx.OpenIdAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    /**
     * UserDetailsService
     */
    @Autowired
    private MoblieUserDetailsService userDetailsService;
    @Autowired
    private WxUserDetailsService wxUserDetailsService;
    @Autowired
    private SingleUserDetailsService singleUserDetailsService;
    @Value("${hmtx.login.url:login}")
    private String loginUrl;
    @Value("${hmtx.login.processing-url:login}")
    private String loginProcessingUrl;
    /**
     * redis服务
     */
    @Autowired
    private StringRedisTemplate redisTemplate;


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if (loginUrl.lastIndexOf("login")>0){
            http.formLogin().loginPage(loginUrl).loginProcessingUrl(loginProcessingUrl).permitAll()
                    .and()
                    .exceptionHandling((exceptions) -> exceptions
                            .defaultAuthenticationEntryPointFor(
                                    // 这里使用自定义的未登录处理，并设置登录地址为前端的登录地址
                                    new LoginUrlAuthenticationEntryPoint(loginUrl),
                                    new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                            )
                    );
        }else {
            http.formLogin().loginPage(loginUrl).permitAll();
        }

//        http.csrf().disable().anonymous().and().authorizeRequests().antMatchers("/oauth/**","/social/**").permitAll();
        http
                .authorizeRequests()
                .antMatchers(
                        "/ccba/login","/sso/**","/oauth/**","/social/**","/token/wx/code",
                        "/oms/**","/bi/**","/ccba/**","/user","/erp/**","/open/**",
                        "/query/type/**","/atcl/log/**","/single/**","/base-login.html").permitAll()//大多第三方对接的登录相关的接口
                .anyRequest().authenticated()
                .and().headers().frameOptions().disable()
                .and().csrf().disable();

    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        /**此处添加Provider!!!!!!!!!!!!!*/
        auth.authenticationProvider(new MobileAuthenticationProvider(userDetailsService,redisTemplate));
        auth.authenticationProvider(new SingleAuthenticationProvider(singleUserDetailsService));
        auth.authenticationProvider(new OpenIdAuthenticationProvider(wxUserDetailsService));
        auth.authenticationProvider(authenticationProvider());
        auth.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder());
    }

    @Bean
    @Override
    protected AuthenticationManager authenticationManager() throws Exception {

        return super.authenticationManager();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new AeotradeUserDetailsService();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    /**
     * 配置修改hideUserNotFoundExceptions = false,不隐藏usernameNotFundExceptions
     * @return
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setHideUserNotFoundExceptions(false);
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}
