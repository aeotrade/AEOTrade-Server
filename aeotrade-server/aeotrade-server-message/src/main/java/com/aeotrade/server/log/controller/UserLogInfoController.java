package com.aeotrade.server.log.controller;

import com.aeotrade.server.log.model.UserLogInfo;
import com.aeotrade.server.log.service.UserLogInfoService;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.PageParam;
import com.aeotrade.suppot.PageResult;
import com.aeotrade.suppot.RespResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author yewei
 * @since 2022-10-27
 */
@RestController
@RequestMapping("/user/log/info")
public class UserLogInfoController extends BaseController{

    
    @Autowired
    private UserLogInfoService userLogInfoService;

    
    /**
    * 通过id查询
    */
    @GetMapping("/get/byid/{id}")
    public RespResult getById(@PathVariable(value = "id") Long id){
        return handleResult(userLogInfoService.getById(id));
    }

    /**
    * 新增
    */
    @PostMapping("/save")
    public RespResult save(@RequestBody UserLogInfo userLogInfo){
        userLogInfoService.save(userLogInfo);
        return handleOK();
    }

    /**
    * 通过id删除
    */
    @DeleteMapping("/delete/{id}")
    public RespResult delete(@PathVariable(value = "id") String ids){
        String[] idsStrs = ids.split(",");
        for (String id:idsStrs){
            userLogInfoService.removeById(Long.valueOf(id));
        }
        return handleOK();
    }

    /**
    * 修改
    */
    @PutMapping("/update")
    public RespResult updateById(@RequestBody UserLogInfo userLogInfo){
        userLogInfoService.updateById(userLogInfo);
        return handleOK();
    }


    /**
    * 查询列表
    */
    @PostMapping("/list")
    public RespResult<List> list(@RequestBody UserLogInfoReqVo userLogInfo ){
    final LambdaQueryWrapper<UserLogInfo> lambda = new QueryWrapper<UserLogInfo>().lambda();
        this.buildCondition(lambda,userLogInfo);
        return handleResult(userLogInfoService.list(lambda));
    }

    /**
    * 分页查询
    */
    @PostMapping("/page")
    public RespResult<PageResult<UserLogInfo>> page(@RequestBody PageParam<UserLogInfoReqVo> pageParam){
        final UserLogInfoReqVo param = pageParam.getParam();
        final LambdaQueryWrapper<UserLogInfo> lambda = new QueryWrapper<UserLogInfo>().lambda();
        this.buildCondition(lambda,param);
        final IPage<UserLogInfo> page = userLogInfoService.page(new Page<>(pageParam.getPageNo(), pageParam.getPageSize()), lambda);
        PageResult<UserLogInfo> pr = new PageResult();
        pr.setPageCount(page.getPages());
        pr.setTotalCount(page.getTotal());
        pr.setPageNo((int) page.getCurrent());
        pr.setPageSize((int) page.getSize());
        pr.setResults(page.getRecords());
        return handleResult(pr);
    }


        /**
        * 构造查询条件
        * @param lambda
        * @param param
        */
        private void buildCondition(LambdaQueryWrapper<UserLogInfo> lambda, UserLogInfoReqVo param){
            if(!StringUtils.isEmpty(param.getUserName())){
                lambda.eq(UserLogInfo::getUserName, param.getUserName());
            }
            if(!StringUtils.isEmpty(param.getMenberName())){
                lambda.eq(UserLogInfo::getMenberName, param.getMenberName());
            }
            if(!StringUtils.isEmpty(param.getWebUrl())){
                lambda.eq(UserLogInfo::getWebUrl, param.getWebUrl());
            }
            if(!StringUtils.isEmpty(param.getIp())){
                lambda.eq(UserLogInfo::getIp, param.getIp());
            }
            if(!CollectionUtils.isEmpty(param.getRequestTimeList())){
                lambda.ge(UserLogInfo::getRequestTime, param.getRequestTimeList().get(0));
                lambda.le(UserLogInfo::getRequestTime, param.getRequestTimeList().get(1));
            }
            if(!StringUtils.isEmpty(param.getRequestUrl())){
                lambda.eq(UserLogInfo::getRequestUrl, param.getRequestUrl());
            }
            if(!StringUtils.isEmpty(param.getRequestType())){
                lambda.eq(UserLogInfo::getRequestType, param.getRequestType());
            }
            if(!StringUtils.isEmpty(param.getRequestParameter())){
                lambda.eq(UserLogInfo::getRequestParameter, param.getRequestParameter());
            }
            if(!StringUtils.isEmpty(param.getRequestNature())){
                lambda.eq(UserLogInfo::getRequestNature, param.getRequestNature());
            }
            if(!StringUtils.isEmpty(param.getUrlName())){
                lambda.eq(UserLogInfo::getUrlName, param.getUrlName());
            }
            if(!StringUtils.isEmpty(param.getWebName())){
                lambda.eq(UserLogInfo::getWebName, param.getWebName());
            }
            if(!StringUtils.isEmpty(param.getWebSys())){
                lambda.eq(UserLogInfo::getWebSys, param.getWebSys());
            }
            if(!StringUtils.isEmpty(param.getIpAddress())){
                lambda.eq(UserLogInfo::getIpAddress, param.getIpAddress());
            }
            lambda.orderBy(true,false, UserLogInfo::getId);
        }


        /**
         * 请求model
         */
        @EqualsAndHashCode(callSuper = true)
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        private static class UserLogInfoReqVo extends UserLogInfo {
            private List<String> requestTimeList; // 请求时间起止
        }


}
