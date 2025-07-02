package com.aeotrade.server.log.controller;

import com.aeotrade.server.log.model.UserDocumentLog;
import com.aeotrade.server.log.service.UserDocumentLogService;
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
@RequestMapping("/user/document/log")
public class UserDocumentLogController extends BaseController{

    
    @Autowired
    private UserDocumentLogService userDocumentLogService;

    
    /**
    * 通过id查询
    */
    @GetMapping("/get/byid/{id}")
    public RespResult getById(@PathVariable(value = "id") Long id){
        return handleResult(userDocumentLogService.getById(id));
    }

    /**
    * 新增
    */
    @PostMapping("/save")
    public RespResult save(@RequestBody UserDocumentLog userDocumentLog){
        userDocumentLogService.save(userDocumentLog);
        return handleOK();
    }

    /**
    * 通过id删除
    */
    @DeleteMapping("/delete/{id}")
    public RespResult delete(@PathVariable(value = "id") String ids){
        String[] idsStrs = ids.split(",");
        for (String id:idsStrs){
            userDocumentLogService.removeById(Long.valueOf(id));
        }
        return handleOK();
    }

    /**
    * 修改
    */
    @PutMapping("/update")
    public RespResult updateById(@RequestBody UserDocumentLog userDocumentLog){
        userDocumentLogService.updateById(userDocumentLog);
        return handleOK();
    }


    /**
    * 查询列表
    */
    @PostMapping("/list")
    public RespResult<List> list(@RequestBody UserDocumentLogReqVo userDocumentLog ){
    final LambdaQueryWrapper<UserDocumentLog> lambda = new QueryWrapper<UserDocumentLog>().lambda();
        this.buildCondition(lambda,userDocumentLog);
        return handleResult(userDocumentLogService.list(lambda));
    }

    /**
    * 分页查询
    */
    @PostMapping("/page")
    public RespResult<PageResult<UserDocumentLog>> page(@RequestBody PageParam<UserDocumentLogReqVo> pageParam){
        final UserDocumentLogReqVo param = pageParam.getParam();
        final LambdaQueryWrapper<UserDocumentLog> lambda = new QueryWrapper<UserDocumentLog>().lambda();
        this.buildCondition(lambda,param);
        final IPage<UserDocumentLog> page = userDocumentLogService.page(new Page<>(pageParam.getPageNo(), pageParam.getPageSize()), lambda);
        PageResult<UserDocumentLog> pr = new PageResult<>();
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
        private void buildCondition(LambdaQueryWrapper<UserDocumentLog> lambda, UserDocumentLogReqVo param){
            if(!StringUtils.isEmpty(param.getMemberName())){
                lambda.eq(UserDocumentLog::getMemberName, param.getMemberName());
            }
            if(!StringUtils.isEmpty(param.getFileTypeCode())){
                lambda.eq(UserDocumentLog::getFileTypeCode, param.getFileTypeCode());
            }
            if(!StringUtils.isEmpty(param.getFileType())){
                lambda.eq(UserDocumentLog::getFileType, param.getFileType());
            }
            if(!StringUtils.isEmpty(param.getDatasourceTypeCode())){
                lambda.eq(UserDocumentLog::getDatasourceTypeCode, param.getDatasourceTypeCode());
            }
            if(!CollectionUtils.isEmpty(param.getCreateTimeList())){
                lambda.ge(UserDocumentLog::getCreateTime, param.getCreateTimeList().get(0));
                lambda.le(UserDocumentLog::getCreateTime, param.getCreateTimeList().get(1));
            }
            lambda.orderBy(true,false, UserDocumentLog::getId);
        }


        /**
         * 请求model
         */
        @EqualsAndHashCode(callSuper = true)
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        private static class UserDocumentLogReqVo extends UserDocumentLog {
            private List<String> createTimeList; // 采集时间起止
        }


}
