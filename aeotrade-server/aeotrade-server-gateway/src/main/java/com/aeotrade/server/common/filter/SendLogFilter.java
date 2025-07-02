package com.aeotrade.server.common.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @Author: yewei
 * @Date: 14:23 2021-05-11
 * @Description:
 */
@Slf4j
@Component
@Order(10)
@RequiredArgsConstructor
public class SendLogFilter implements GlobalFilter, Ordered {


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        try {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse originalResponse = exchange.getResponse();
            String method = request.getMethod().toString();
            log.info("请求方式为logloglog+++++++++++++++++++++++++" + method);
            log.info("请求URL为loglog+++++++++++++++++++++++++" + exchange.getRequest().getPath().toString());
            if (method.equals("GET")) {
                ServerHttpResponse serverHttpResponse = executeGET( originalResponse);
                ServerHttpRequest.Builder builder = request.mutate();
                return chain.filter(exchange.mutate().request(builder.build()).response(serverHttpResponse).build());
            } else if(!method.equals("POST")){
                return chain.filter(exchange);
            }else if (request.getHeaders().getFirst("Content-Type") == null){
                return chain.filter(exchange);
            }else if (request.getHeaders().getFirst("Content-Type").contains("application/json")) {
                ServerHttpResponse serverHttpResponse = executePOST(originalResponse);
                ServerHttpRequest.Builder builder = request.mutate();
                return chain.filter(exchange.mutate().request(builder.build()).response(serverHttpResponse).build());
            }
            return chain.filter(exchange);
        } catch (Exception e) {
            log.warn(e.getMessage());
            return chain.filter(exchange);
        }

    }

    private ServerHttpResponse executePOST(ServerHttpResponse originalResponse) {
        ServerHttpResponseDecorator response = new ServerHttpResponseDecorator(originalResponse) {
            @Override
            public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
                return writeWith(Flux.from(body)
                        .flatMapSequential(p -> p));
            }

        };
        return response;
    }

    private ServerHttpResponse executeGET(ServerHttpResponse originalResponse) {
        ServerHttpResponseDecorator response = new ServerHttpResponseDecorator(originalResponse) {
            @Override
            public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
                return writeWith(Flux.from(body)
                        .flatMapSequential(p -> p));
            }

        };
        return response;
    }


    @Override
    public int getOrder() {
        return -10;
    }


}
