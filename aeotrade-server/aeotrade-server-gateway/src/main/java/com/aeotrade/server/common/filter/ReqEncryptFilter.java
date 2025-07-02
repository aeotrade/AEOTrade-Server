package com.aeotrade.server.common.filter;

/**
 * @Author: yewei
 * @Date: 9:30 2022-02-24
 * @Description:响应加密过滤器
 */
import com.aeotrade.encoded.utils.RSASecurityUtils;
import com.aeotrade.suppot.RespResult;
import com.aeotrade.suppot.RespResultMapper;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReqEncryptFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if(exchange.getAttributes().containsKey("hmm_error")){

            log.info("参数解密失败");
            return chain.filter(exchange);
        }
        MediaType mediaType = exchange.getRequest().getHeaders().getContentType();
        if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(mediaType) || MediaType.IMAGE_JPEG.isCompatibleWith(mediaType)) {
            return chain.filter(exchange);
        }

        ServerHttpResponse originalResponse = exchange.getResponse();
        MediaType responsemMdiaType = originalResponse.getHeaders().getContentType();
        if (! MediaType.APPLICATION_JSON.isCompatibleWith(responsemMdiaType) ) {
            return chain.filter(exchange);
        }

        DataBufferFactory bufferFactory = originalResponse.bufferFactory();

        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux) {
                    Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;

                    return super.writeWith(fluxBody.buffer().map(dataBuffers -> {

                        DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
                        DataBuffer join = dataBufferFactory.join(dataBuffers);

                        byte[] content = new byte[join.readableByteCount()];

                        join.read(content);
                        // 释放掉内存
                        DataBufferUtils.release(join);
                        // 返回值得字符串
                        String str = new String(content, Charset.forName("UTF-8"));
                        byte[] strContent = str.getBytes();
                        originalResponse.getHeaders().setContentLength(strContent.length);
                        try {

                            HttpHeaders headers = exchange.getRequest().getHeaders();
                            if(!headers.containsKey("uscc") || !headers.containsKey("Authorization")){
                                return bufferFactory.wrap(str.getBytes());
                            }

                            String uscc = headers.get("uscc").get(0);
                            String  time = String.valueOf(new Date().getTime());
                            String s = RSASecurityUtils.ReqEncrypt(uscc, str, time);
                            log.info("usccusccuscc->"+uscc);
                            log.info("timetimetime->"+time);
                            //log.info(" RSASecurity RSASecurity->"+s);
                            String result = JSONObject.toJSONString(RespResultMapper.wrap(RespResult.SUCCESS_CODE, "操作成功", s));
                            originalResponse.getHeaders().setContentLength(result.getBytes().length);
                            originalResponse.getHeaders().set("time",time);
                            if(exchange.getAttributes().containsKey("isDecoder") && originalResponse.getStatusCode().equals(HttpStatus.valueOf(400))) {
                                originalResponse.setStatusCode(HttpStatus.valueOf(200));
                            }
                            byte[] uppedContent = result.getBytes();
                            originalResponse.getHeaders().setContentLength(uppedContent.length);
                            originalResponse.getHeaders().set("time",time);
                            return bufferFactory.wrap(result.getBytes());
                        }catch (Exception e){
                            log.warn(e.getMessage());
                            String result = JSONObject.toJSONString(RespResultMapper.wrap(RespResult.SUCCESS_CODE, "加密失败", ""));
                            log.info("加密失败................");
                            return bufferFactory.wrap(result.getBytes());
                        }
                    }));

                }

                return super.writeWith(body);
            }
        };
        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
