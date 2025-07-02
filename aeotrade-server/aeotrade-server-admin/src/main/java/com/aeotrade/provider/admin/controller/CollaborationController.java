package com.aeotrade.provider.admin.controller;

import com.aeotrade.provider.admin.entiy.UacMember;
import com.aeotrade.provider.admin.service.impl.UacMemberServiceImpl;
import com.aeotrade.configure.HmtxUserInfoTokenServices;
import com.aeotrade.provider.admin.entiy.UacStaff;
import com.aeotrade.provider.admin.service.impl.UacStaffServiceImpl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: 吴浩
 * @Date: 2022-10-20 17:58
 */
@RestController
public class CollaborationController {
    @Autowired
    private HmtxUserInfoTokenServices hmtxUserInfoTokenServices;
    @Autowired
    private UacMemberServiceImpl uacMemberService;
    @Autowired
    private UacStaffServiceImpl staffService;
    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping("/single/user ")
    public String user( HttpServletRequest request){
        Map<String,Object> map=new HashMap<>();
        String authorization = request.getHeader("Authorization");
        String token = null;
        if (authorization.contains("bearer ")) {
            token = authorization.replaceAll("bearer ", "");
        }
        if (authorization.contains("Bearer ")) {
            token = authorization.replaceAll("Bearer ", "");
        }
        if (token == null) {
            return null;
        }
        OAuth2Authentication oAuth2Authentication = hmtxUserInfoTokenServices.loadAuthentication(token);
        String s = oAuth2Authentication.getPrincipal().toString();
        Map<String, Object> username = JSONObject.parseObject(s, Map.class);
        map.put("userId",username.get("staffId"));
        map.put("userName",username.get("staffName"));
        if(null!=username.get("userPhone")){
            map.put("userPhone",username.get("mobile"));
        }
        UacStaff uacStaff = staffService.getById((Long) username.get("staffId"));
        if(null==uacStaff.getLastMemberId()){
            return null;
        }
        UacMember uacMember = uacMemberService.get(uacStaff.getLastMemberId());
        map.put("memberId",uacMember.getId());
        map.put("memberName",uacMember.getMemberName());
        map.put("uscc",uacMember.getUscCode());
        Query query = new Query();
        query.addCriteria(Criteria.where("tenantId").is(uacStaff.getLastMemberId()))
                .addCriteria(Criteria.where("userId").is(uacStaff.getId()));
        List<Document> documents = mongoTemplate.find(query, Document.class,"chain_cert_user_member");
        if(null!=documents && documents.size()!=0){

            Object certId = documents.get(0).get("signCertId");
            Document chain = mongoTemplate.findById(certId, Document.class, "chain_sdk_cert");
            if(null!=chain){
                map.put("userChainId", chain.getString("certName"));
                map.put("orgChainId", chain.getString("orgId"));
            }
        }
        return JSON.toJSONString(map);
    }
}
