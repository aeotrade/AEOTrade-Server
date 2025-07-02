package com.aeotrade.server.common.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.gateway.support.ServiceUnavailableException;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 服务不可用过滤器
 * 当过滤器触发降级逻辑时，将降级key存储到Redis中，并设置过期时间。
 */
@Slf4j
@Component
public class ServiceUnavailableFilter implements GlobalFilter, Ordered {
    @Value("${hmtx.gateway.service-unavailable.key-timeout:10}")
    private Integer keyTimeout = 10; // 设置降级key的过期时间（单位：秒）
    private static final String SERVICE_UNAVAILABLE_KEY = "service_unavailable:";
    private final RedisTemplate<String, Object> redisTemplate;

    public ServiceUnavailableFilter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate=redisTemplate;
    }
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 检查是否已经触发降级逻辑
        if (keyExists(SERVICE_UNAVAILABLE_KEY + exchange.getRequest().getURI().getPath())){
            // 返回降级响应
            ServerHttpResponse response = exchange.getResponse();
            response.getHeaders().add("Content-Type", "application/json");
            response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
            return response.setComplete();
        }
        // 从Nacos获取服务实例
        // 这里需要根据实际情况获取服务实例，可能需要自定义服务发现客户端
        // 如果服务实例为空，则触发降级逻辑
        return chain.filter(exchange).onErrorResume(e -> {
            // 检查是否是服务未找到异常
            if (e instanceof ServiceUnavailableException || e instanceof NotFoundException){
                // 触发降级逻辑
                keyDemote(exchange.getRequest().getURI().getPath());

                // 返回降级响应
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                return response.setComplete();
            }
            // 对于其他异常，可以返回特定的错误响应
            return Mono.error(e);
        });
    }

    @Override
    public int getOrder() {
        return -10;
    }

    private boolean keyExists(String key) {
        // 确保 RedisTemplate 不为空
        if (redisTemplate == null) {
            return false;
        }
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    // 降级
    private void keyDemote(String path) {
        // 确保 RedisTemplate 不为空
        if (redisTemplate == null) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(SERVICE_UNAVAILABLE_KEY + path, true);
            redisTemplate.expire(SERVICE_UNAVAILABLE_KEY + path, keyTimeout, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
