package com.aeotrade.server.common.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * @Author: yewei
 * @Date: 14:23 2021-05-11
 * @Description:判断URL是否需要统计
 */
@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
public class GetPOSTBodyFilter implements GlobalFilter, Ordered {


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        try {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse originalResponse = exchange.getResponse();
//            System.out.println(originalResponse.getStatusCode());
            String method = request.getMethod().toString();
            log.info("请求方式为+body++++++++++++++++++++++++" + method);
            log.info("请求URL为+bodybody++++++++++++++++++++++++" + exchange.getRequest().getPath().toString());
            if (method.equals("GET")) {
                return chain.filter(exchange);
            } else if (exchange.getRequest().getHeaders().getContentType() == null) {
                return chain.filter(exchange);
            } else if (!request.getHeaders().getFirst("Content-Type").contains("application/json")) {
                return chain.filter(exchange);
            } else {
                return DataBufferUtils.join(exchange.getRequest().getBody())
                        .flatMap(dataBuffer -> {
                            DataBufferUtils.retain(dataBuffer);
                            Flux<DataBuffer> cachedFlux = Flux
                                    .defer(() -> Flux.just(dataBuffer.slice(0, dataBuffer.readableByteCount())));
                            ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(
                                    exchange.getRequest()) {
                                @Override
                                public Flux<DataBuffer> getBody() {
                                    return cachedFlux;
                                }
                            };
                            exchange.getAttributes().put(exchange.getRequest().getPath().toString(), cachedFlux);
                            return chain.filter(exchange.mutate().request(mutatedRequest).build());
                        });
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
            return chain.filter(exchange);
        }
    }


    @Override
    public int getOrder() {
        //  return Ordered.HIGHEST_PRECEDENCE;
        return -99;
    }
}
