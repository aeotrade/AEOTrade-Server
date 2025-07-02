package com.aeotrade.server.common.filter;
import com.aeotrade.encoded.utils.RSASecurityUtils;
import com.aeotrade.utlis.CommonUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * @Author: yewei
 * @Date: 9:31 2022-02-24
 * @Description:请求解密过滤器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResDecodeFilter implements GlobalFilter, Ordered {

    private final List<HttpMessageReader<?>> messageReaders = HandlerStrategies.withDefaults().messageReaders();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        try {
            //对post的请求体处理
            return operationExchange(exchange, chain);

        }catch (Exception e){
            log.info("处理请求失败!!!!!");
            log.warn(e.getMessage());
            return chain.filter(exchange);
        }
    }

    private Mono<Void> operationExchange(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        MediaType mediaType = exchange.getRequest().getHeaders().getContentType();
        if (request.getMethod() != HttpMethod.POST || (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(mediaType))) {
            return chain.filter(exchange);
        }
        HttpHeaders headers1 = exchange.getRequest().getHeaders();
        if(exchange.getRequest().getMethod()==HttpMethod.POST && headers1.containsKey("Authorization") && headers1.containsKey("time")
                &&  headers1.containsKey("uscc")){
            ServerRequest serverRequest = ServerRequest.create(exchange, messageReaders);
            Mono<String> modifiedBody = serverRequest.bodyToMono(String.class).flatMap(body -> {
                if (MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)) {
                    try {
                        // 对原先的body进行操作，处理解密，添加参数等等
                        JSONObject jsonObject = JSON.parseObject(body);
                        String s = RSASecurityUtils.ResDecoder(headers1.get("uscc").get(0), jsonObject.getString("param"), headers1.get("time").get(0));
                        if(CommonUtil.isEmpty(s)){
                            exchange.getAttributes().put("hmm_error","参数解密失败");
                            return Mono.empty();
                        }
                        JSONObject jsonObject1 = JSON.parseObject(s);
                        log.info("参数参数+++++++"+s);
                        if(CommonUtil.isNotEmpty(jsonObject)){
                            exchange.getAttributes().put("isDecoder", "true");
                        }
                        exchange.getAttributes().put("newBody", jsonObject1);
                        return Mono.just(s);
                    }catch (Exception e){
                        log.info("解密异常!!!!!!!!!1");
                        log.warn(e.getMessage());
                        return Mono.empty();
                    }
                }
                return Mono.empty();
            });
            BodyInserter bodyInserter = BodyInserters.fromPublisher(modifiedBody, String.class);
            HttpHeaders headers = new HttpHeaders();
            headers.putAll(exchange.getRequest().getHeaders());
            headers.remove(HttpHeaders.CONTENT_LENGTH);
            CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange, headers);
            return bodyInserter.insert(outputMessage, new BodyInserterContext()).then(Mono.defer(() -> {
                ServerHttpRequestDecorator decorator = new ServerHttpRequestDecorator(exchange.getRequest()) {
                    @Override
                    public HttpHeaders getHeaders() {
                        //对请求头修改，可以添加请求头参数等等
                        long contentLength = headers.getContentLength();
                        HttpHeaders httpHeaders = new HttpHeaders();
                        httpHeaders.putAll(super.getHeaders());
                        if (contentLength > 0) {
                            httpHeaders.setContentLength(contentLength);
                        } else {
                            httpHeaders.set(HttpHeaders.TRANSFER_ENCODING, "chunked");
                        }
                        return httpHeaders;
                    }
                    @Override
                    public Flux<DataBuffer> getBody() {
                        return outputMessage.getBody();
                    }
                };
                return chain.filter(exchange.mutate().request(decorator).build());
            }));
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
