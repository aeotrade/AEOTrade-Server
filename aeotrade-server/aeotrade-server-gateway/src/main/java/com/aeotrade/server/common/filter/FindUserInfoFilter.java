package com.aeotrade.server.common.filter;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTHeader;
import cn.hutool.jwt.JWTUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @Auther: 吴浩
 * @Date: 2023-04-18 9:26
 */
@Slf4j
@Component
@Order(50)
@RequiredArgsConstructor
public class FindUserInfoFilter implements GlobalFilter, Ordered {
    private final AntPathMatcher matcher=new AntPathMatcher();
    @Autowired
    private RedisTemplate<String, Object> stringObjectRedisTemplate;
    @Value("${hmtx.gateway.uaa.permit-url:}")
    private List<String> uaaPermitUrl;

    /**
     * 判断授权码模式token是否请求其他接口
     *
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取请求信息
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();

        if (headers.containsKey("Authorization")) {
            //获取token
            String authorization = headers.getFirst("Authorization");
            if (authorization.contains("Bearer ")) {
                authorization = authorization.replaceAll("Bearer ", "");
            } else {
                authorization = authorization.replaceAll("bearer ", "");
            }
            if (authorization.length() > 50 && !authorization.startsWith("Basic ")) {
                //解析token
                final JWT jwt = JWTUtil.parseToken(authorization);
                jwt.getHeader(JWTHeader.TYPE);
                //获取token中client_id
                Object client_id = jwt.getPayload("client_id");
                if (null == client_id) {
                    return chain.filter(exchange);
                }
                if (client_id.equals("hengshih")) {
                    return chain.filter(exchange);
                }
                //通过client_id获取redis中client信息
                Object o = stringObjectRedisTemplate.opsForValue().get("oauth_client_details:" + client_id);
                if (null == o) {
                    return chain.filter(exchange);
                } else {
                    //判断cilent是否为授权码模式
                    JSONObject jsonObject = JSON.parseObject(String.valueOf(o));
                    JSONArray authorizedGrantTypes = null;
                    if (jsonObject.containsKey("authorizedGrantTypes")){
                        authorizedGrantTypes = jsonObject.getJSONArray("authorizedGrantTypes");
                    }
                    if (jsonObject.containsKey("authorized_grant_types")){
                        authorizedGrantTypes = jsonObject.getJSONArray("authorized_grant_types");
                    }
                    if (authorizedGrantTypes!=null){
                        for (Object authorizedGrantType : authorizedGrantTypes) {
                            if (String.valueOf(authorizedGrantType).equals("authorization_code")) {
                                //判断该接口是否为授权码模式指定调用接口
                                boolean isPermitUrl=false;
                                if (uaaPermitUrl!=null) {
                                    for (String uaaUrl:uaaPermitUrl) {
                                        isPermitUrl=matcher.match(uaaUrl, exchange.getRequest().getPath().toString());
                                        if (isPermitUrl){
                                            break;
                                        }
                                    }
                                }
                                //添加判断是为了限制只能调用条件中的URL资源
                                if (isPermitUrl && exchange.getRequest().getPath().toString().startsWith("/uaa")
                                        &&!exchange.getRequest().getPath().toString().equals("/uaa/social/user")
                                        && !exchange.getRequest().getPath().toString().equals("/uaa/social/get/user")
                                        && !exchange.getRequest().getPath().toString().equals("/uaa/social/co/user")
                                        && !exchange.getRequest().getPath().toString().equals("/uaa/social/login/user")
                                        && !exchange.getRequest().getPath().toString().equals("/uaa/social/mei/user")) {
                                    ServerHttpResponse response = exchange.getResponse();
                                    JSONObject message = new JSONObject();
                                    message.put("status", -1);
                                    message.put("data", "token无权限");
                                    byte[] bits = message.toJSONString().getBytes(StandardCharsets.UTF_8);
                                    DataBuffer buffer = response.bufferFactory().wrap(bits);
                                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                                    //指定编码，否则在浏览器中会中文乱码
                                    response.getHeaders().add("Content-Type", "text/plain;charset=UTF-8");
                                    return response.writeWith(Mono.just(buffer));
                                }
                            }
                        }
                    }
                }
            }
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -50;
    }
}
