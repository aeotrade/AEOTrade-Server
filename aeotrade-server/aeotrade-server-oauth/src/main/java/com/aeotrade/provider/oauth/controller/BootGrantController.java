package com.aeotrade.provider.oauth.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

@Controller
//必须配置
@SessionAttributes("authorizationRequest")
public class BootGrantController {
//    @Autowired
//    private AuthorizationEndpoint authorizationEndpoint;

    @RequestMapping("/oauth/confirm_access")
    public ModelAndView getAccessConfirmation(Map<String, Object> model, HttpServletRequest request) throws Exception {
        AuthorizationRequest authorizationRequest = (AuthorizationRequest) model.get("authorizationRequest");
        ModelAndView view = new ModelAndView();
        view.setViewName("base-grant"); //自定义页面名字，resources\templates\base-grant.html
        view.addObject("clientId", authorizationRequest.getClientId());
        view.addObject("scopes",authorizationRequest.getScope());
        return view;
    }

//    @RequestMapping(value = {"/oauth/authorize","/sso/oauth/authorize"})
//    public ModelAndView authorize(Map<String, Object> model, @RequestParam Map<String, String> parameters,
//                                  SessionStatus sessionStatus, Principal principal) {
//        return authorizationEndpoint.authorize(model, parameters, sessionStatus, principal);
//    }
}

