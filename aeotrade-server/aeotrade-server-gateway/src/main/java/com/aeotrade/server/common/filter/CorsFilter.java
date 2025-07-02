package com.aeotrade.server.common.filter;

import com.aeotrade.entity.HmtxOauth2AccessToken;
import com.aeotrade.entity.constant.GlobalConstant;
import com.aeotrade.exception.AeotradeException;
import com.aeotrade.service.RedisService;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class CorsFilter implements GlobalFilter, Ordered {
    private static final String ALLOWED_METHODS = "*";
    private static final String ALLOWED_ORIGIN = "*";
    private static final String ALLOWED_EXPOSE = "*";
    private static final String MAX_AGE = "3600";
    private final RedisService redisService;
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        if (request.getHeaders().containsKey("Access-Control-Request-Method")) {
            response.setStatusCode(HttpStatus.OK);
            return Mono.empty();
        }

        if (request.getMethod() == HttpMethod.OPTIONS) {
            log.info("处理OPTIONS请求");
            response.setStatusCode(HttpStatus.OK);
            return Mono.empty();
        }
        /**
         * 网关过滤器添加 长短TOKEN功能 逻辑： 请求头中Authorization的值为空，或字符长度小于50的，需要从Redis缓存中取出对应的JWT token并设置成当前值；
         * 缓存KEY是请求中的jti值，jti有三种情况， 1是放在请求头的Authorization中，值长度小于50的； 2是请求头jti的值； 3是URL中参数jti的值。
         */
        // 判断并获取jti的值
        String jti = null;
        HttpHeaders httpHeaders = request.getHeaders();
        // 1. Headers 中 Authorization 为短字符的情况 length < 50
        List<String> authorization = httpHeaders.get("Authorization");
        if (authorization != null && authorization.get(0).length() < 50) {
            String temp = authorization.get(0);
            if (temp.toLowerCase(Locale.ROOT).indexOf("earer") > 0) {
                jti = temp.substring(7);
            } else {
                jti = temp;
            }
        }
        // 2. Headers 中 jti 有值
        List<String> jtilist = httpHeaders.get("jti");
        if (jtilist != null) {
            jti = jtilist.get(0);
        }
        // 3. URL中有 jti 参数的情况，例如： ?jti=1e227ec6-69c4-4020-9ff8-55aef56c2b06
        if (request.getQueryParams().get("jti") != null) {
            jti = request.getQueryParams().get("jti").get(0);
        }
        HttpHeaders headers = response.getHeaders();
        headers.set("Access-Control-Allow-Origin", null == request.getHeaders()
                .get("origin") ? ALLOWED_ORIGIN : request.getHeaders().get("origin").get(0));
        headers.add("Access-Control-Allow-Methods", null == request.getHeaders()
                .get("Access-Control-Request-Method") ? ALLOWED_METHODS : StringUtils.join(request.getHeaders().get("Access-Control-Request-Method"), ","));
        headers.add("Access-Control-Max-Age", MAX_AGE);

        headers.add("Access-Control-Allow-Headers", null == request.getHeaders()
                .get("Access-Control-Request-Headers") ? "*" : StringUtils.join(request.getHeaders().get("Access-Control-Request-Headers"), ","));
        headers.add("Access-Control-Expose-Headers", ALLOWED_EXPOSE);
        headers.add("Access-Control-Allow-Credentials", "true");
        ServerHttpRequest newRequest = null;

        if ((authorization == null
                || (StringUtils.isEmpty(authorization.get(0)))
                || authorization.get(0).length() < 50)
                && jti != null) {
            Object o = redisService.getOauth2AccessToken(jti);
            if (o != null) {
                try {
                    HmtxOauth2AccessToken hmtxOauth2AccessToken =
                            JSONObject.parseObject(o.toString(), HmtxOauth2AccessToken.class);
                    if (hmtxOauth2AccessToken != null && hmtxOauth2AccessToken.getValue() != null) {
                        headers.add("Authorization", "bearer " + hmtxOauth2AccessToken.getValue());
                        newRequest =
                                request
                                        .mutate()
                                        .header("Authorization", "bearer " + hmtxOauth2AccessToken.getValue())
                                        .build();
                    }
                } catch (Exception e) {
                    log.warn(e.getMessage());
                }
            } else {
                if (null != redisService.getOauth2LoginInfo(jti)) {
                    throw new AeotradeException("该账户已在其他设备登录");
                }
            }
        }
        if (newRequest != null) {
            chain.filter(exchange.mutate().request(newRequest).response(response).build());
        }

        //添加请求头参数api-version，用于模块服务识别是否openapi
        String url = request.getURI().getPath();
        if (StringUtils.startsWithIgnoreCase(url, "/v")) {
            return chain.filter(exchange.mutate().request(request.mutate().header(GlobalConstant.API_VERSION, url.substring(1, url.indexOf("/", 1))).build()).build());
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
