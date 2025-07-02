package com.aeotrade.provider.mamber.service.impl;

import com.aeotrade.provider.mamber.entity.PmsApp;
import com.aeotrade.provider.mamber.entity.PmsAppMember;
import com.aeotrade.provider.mamber.mapper.PmsAppMapper;
import com.aeotrade.provider.mamber.service.PmsAppMemberService;
import com.aeotrade.provider.mamber.service.PmsAppService;
import com.aeotrade.suppot.PageList;
import com.aeotrade.utlis.CommonUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: 吴浩
 * @Date: 2023-11-14 11:23
 */
@Service
public class PmsAppServiceImpl extends ServiceImpl<PmsAppMapper, PmsApp> implements PmsAppService {
    @Autowired
    private PmsAppMemberService pmsAppMemberService;
    @Override
    public PageList<PmsApp> findList(Page page, String appName) {
        LambdaQueryWrapper<PmsApp> pmsAppLambdaQueryChainWrapper = new LambdaQueryWrapper<>();
        if(StringUtils.isNotEmpty(appName)){
            pmsAppLambdaQueryChainWrapper.like(PmsApp::getAppName,appName);
        }else{
            pmsAppLambdaQueryChainWrapper.isNotNull(PmsApp::getId);
        }
        pmsAppLambdaQueryChainWrapper.orderByDesc(PmsApp::getCreatedTime);
        Page<PmsApp> page1 = this.page(page, pmsAppLambdaQueryChainWrapper);
        PageList<PmsApp> apps=new PageList<>();
        apps.setRecords(page1.getRecords());
        apps.setTotalSize(page1.getTotal());
        return apps;

    }

    @Override
    public PageList<PmsApp> findAppListBymemberId(Page page, Long memberId) {
        PageList<PmsApp> vos = new PageList<>();
        List<PmsAppMember> list =pmsAppMemberService.findByMemberId(memberId);
        if(!CommonUtil.isEmpty(list)&& !list.isEmpty()){
            List<PmsApp> vo = new ArrayList<>();
            list.forEach(i->{
                PmsApp pmsApp = this.getById(i.getAppId());
                if(pmsApp!=null){
                    vo.add(pmsApp);
                }
            });
            vos.setRecords(vo);
            vos.setTotalSize(list.size());
            return vos;
        }
        return null;
    }

    @Override
    public void updateAppMemberList(Long appId, Long memberId) {
        List<PmsAppMember> list = pmsAppMemberService.lambdaQuery()
                .eq(PmsAppMember::getAppId, appId).eq(PmsAppMember::getMemberId, memberId).list();
        PmsAppMember app = !list.isEmpty() ?list.get(0):null;
        if(app==null) {
            PmsAppMember pmsAppMember = new PmsAppMember();
            pmsAppMember.setAppId(appId);
            pmsAppMember.setMemberId(memberId);
            pmsAppMemberService.save(pmsAppMember);
        }else {
            pmsAppMemberService.removeById(app.getId());
        }
    }

    @Override
    public List<Document> findDynamicConditionist(String s, String pms_app, String sort, String s1) {
        List<Document> documentList=new ArrayList<>();
        List<PmsApp> list = this.lambdaQuery().in(PmsApp::getId, s1).orderByDesc(PmsApp::getCreatedTime).list();
        return getDocuments(documentList, list);
    }

    @Override
    public List<Document> findDynamicConditionistPage(String s, String pms_app, String sort, String substring, long current, long size) {
        List<Document> documentList=new ArrayList<>();
        LambdaQueryWrapper<PmsApp> appCloudLambdaQueryWrapper = new LambdaQueryWrapper<>();
        appCloudLambdaQueryWrapper.in(PmsApp::getId,substring);
        appCloudLambdaQueryWrapper.orderByDesc(PmsApp::getId);
        List<PmsApp> records = this.page(new Page<>(size, current), appCloudLambdaQueryWrapper).getRecords();
        return getDocuments(documentList, records);
    }

    private List<Document> getDocuments(List<Document> documentList, List<PmsApp> records) {
        for (PmsApp pmsApp : records) {
            Document document=new Document();
            document.put("id",pmsApp.getId());
            document.put("appName",pmsApp.getAppName());
            document.put("appLog",pmsApp.getAppLog());
            document.put("url",pmsApp.getUrl());
            document.put("subhead",pmsApp.getSubhead());
            document.put("authStatus",pmsApp.getAuthStatus());
            document.put("createdTime",pmsApp.getCreatedTime());
            documentList.add(document);
        }
        return documentList;
    }
}