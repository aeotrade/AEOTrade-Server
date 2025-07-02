package com.aeotrade.server.common.controller;

import com.aeotrade.entity.FebsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author MrBird
 */
@Slf4j
@RestController
public class FallbackController {


    @RequestMapping("fallback/{name}")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<FebsResponse> systemFallback(@PathVariable String name) {
        String response = "服务访问超时，请稍后再试";
        log.error("{}，目标微服务：{}", response, name);
        return Mono.just(new FebsResponse().message(response+".微服务->"+name+"异常"));
    }

}
