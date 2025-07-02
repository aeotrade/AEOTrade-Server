package com.aeotrade.server.message.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/sse")
@CrossOrigin
public class IndexController {

    /**
     * 首页
     *
     * @return
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }

}
