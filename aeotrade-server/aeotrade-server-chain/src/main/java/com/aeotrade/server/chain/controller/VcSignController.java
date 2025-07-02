package com.aeotrade.server.chain.controller;

import com.aeotrade.chainmaker.model.ChainApplyCredentialsLogs;
import com.aeotrade.server.chain.service.VcSignService;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;

@RestController
public class VcSignController {
    private final VcSignService vcSignService;

    public VcSignController(VcSignService vcSignService) {
        this.vcSignService = vcSignService;
    }

    /**
     * 查看签名记录
     * @param memberId 企业唯一标识
     * @param page 当前页码
     * @param size 每页大小
     */
    @GetMapping("/credential/logs")
    public Page<ChainApplyCredentialsLogs> credentialsLogs(Long memberId,@RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "10") int size){
        return vcSignService.findCredentialsLogs(memberId,PageRequest.of(page, size));
    }
}
