package com.aeotrade.server.common.filter;

import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.jwt.crypto.sign.Signer;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * 检查token是否过期，并对范围内的client颁发的token续期
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenRenewalFilter implements GlobalFilter, Ordered {
    @Value("${hmtx.session.renewal.clients:h4a,zgch4a,mobile,wechat,wx,ai-assistant}")
    private String[] renewalClients;
    @Value("${hmtx.session.timeout:7200}")
    private Integer timeout;
    @Value("${hmtx.session.anon-uris:/uaa/social/login,/uaa/social/signout,/sys/admin/code/random/image," +
            "/pms/catalog/list,/pms/catalog/list/details,/pms/catalog/page/list/details,/wx/wxcat/cover,/wxcat/cover,/oauth/token,/oauth/authorize," +
            "/uaa/oauth/authorize,/uaa/oauth/token,/social/co/user,/uaa/social/co/user,/social/user,/uaa/social/user," +
            "/refresh/token,/uaa/refresh/token,/uaa/single/get/user,/uaa/oauth/check_token}")
    private String[] anonUris;

    private final RedisTemplate<String,String> redisTemplate;
    private final Signer signer;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        // 排除免检查URL
        boolean matchd = Arrays.stream(anonUris).anyMatch(url -> antPathMatcher.match(url, request.getPath().pathWithinApplication().value()));
        if (matchd){
            return chain.filter(exchange);
        }

        ServerHttpResponse response = exchange.getResponse();
        List<String> authorizationList = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (authorizationList != null && !authorizationList.isEmpty()) {
            String authorization = authorizationList.get(0);
            if ((authorization.startsWith("Bearer ")||(authorization.startsWith("bearer "))) && authorization.length()<50) {
                authorization = authorization.substring(7);
                String oldToken = redisTemplate.opsForValue().get("session_timeout_strategy:" + authorization);
                if (oldToken == null) {
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return response.setComplete();
                }
                String aeotradeLogoff = redisTemplate.opsForValue().get("aeotrade_logoff:" + authorization);
                if (aeotradeLogoff != null) {
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return response.setComplete();
                }

                ZonedDateTime zonedDateTime = LocalDateTime.now().atZone(ZoneId.of("Asia/Shanghai"));

                Jwt jwt = JwtHelper.decode(oldToken);
                Map jwtMap = JSON.parseObject(jwt.getClaims().toString(), Map.class);
                Integer expTime = (Integer) jwtMap.get("exp");
                if (expTime != null){
                    long nowMilli =zonedDateTime.toEpochSecond();
                    if (nowMilli >= expTime){
                        response.setStatusCode(HttpStatus.UNAUTHORIZED);
                        return response.setComplete();
                    }
                }

                String client = (String) jwtMap.get("client_id");
                if (Arrays.asList(renewalClients).contains(client)) {

                    // 当前的日期和时间
                    long seconds = zonedDateTime.plusSeconds(timeout).toEpochSecond();
                    jwtMap.put("exp", seconds);

                    Jwt newJwt = JwtHelper.encode(JSON.toJSONString(jwtMap), signer);
                    String newToken = newJwt.getEncoded();

                    ServerHttpRequest build = exchange.getRequest().mutate().header(HttpHeaders.AUTHORIZATION, "Bearer "+newToken).build();
                    ServerWebExchange newExchange = exchange.mutate().request(build).build();

                    // 续签
                    redisTemplate.opsForValue().set("session_timeout_strategy:" + authorization,newToken);
                    redisTemplate.expire("session_timeout_strategy:" + authorization, timeout, TimeUnit.SECONDS);

                    return chain.filter(newExchange);
                }
            }
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -101;
    }
}
