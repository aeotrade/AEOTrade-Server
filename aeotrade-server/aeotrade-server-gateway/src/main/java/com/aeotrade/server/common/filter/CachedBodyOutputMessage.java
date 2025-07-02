package com.aeotrade.server.common.filter;

/**
 * @Author: yewei
 * @Date: 9:49 2022-02-24
 * @Description:
 */

import java.util.function.Supplier;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

class CachedBodyOutputMessage implements ReactiveHttpOutputMessage {
    private final DataBufferFactory bufferFactory;
    private final HttpHeaders httpHeaders;
    private Flux<DataBuffer> body = Flux.error(new IllegalStateException("The body is not set. Did handling complete with success?"));

    CachedBodyOutputMessage(ServerWebExchange exchange, HttpHeaders httpHeaders) {
        this.bufferFactory = exchange.getResponse().bufferFactory();
        this.httpHeaders = httpHeaders;
    }

    public void beforeCommit(@Nonnull Supplier<? extends Mono<Void>> action) {
    }

    public boolean isCommitted() {
        return false;
    }
    @Nonnull
    public HttpHeaders getHeaders() {
        return this.httpHeaders;
    }
    @Nonnull
    public DataBufferFactory bufferFactory() {
        return this.bufferFactory;
    }
    @Nonnull
    public Flux<DataBuffer> getBody() {
        return this.body;
    }
    @Nonnull
    public Mono<Void> writeWith(@Nonnull Publisher<? extends DataBuffer> body) {
        this.body = Flux.from(body);
        return Mono.empty();
    }
    @Nonnull
    public Mono<Void> writeAndFlushWith(@Nonnull Publisher<? extends Publisher<? extends DataBuffer>> body) {
        return this.writeWith(Flux.from(body).flatMap((p) -> {
            return p;
        }));
    }
    @Nonnull
    public Mono<Void> setComplete() {
        return this.writeWith(Flux.empty());
    }
}
