package com.aeotrade.provider.mamber.controller;

import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.mamber.entity.PmsColumn;
import com.aeotrade.provider.mamber.service.PmsColumnService;
import com.aeotrade.provider.mamber.vo.ColumnVO;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.RespResult;
import com.aeotrade.utlis.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 运营栏目配置
 * @Author: yewei
 * @Date: 2020/5/13 11:14
 */
@RestController
//@Api(tags = "运营栏目配置,运营栏目配置", description = "运营栏目配置")
@RequestMapping("/column")
public class PmsColumnController extends BaseController {

    @Autowired
    private PmsColumnService pmsColumnService;

    @GetMapping("/lists")
    //@ApiOperation(httpMethod = "GET", value = "查询所有栏目以及子栏目")
    public RespResult findAlls(){
        try {
            List<ColumnVO> vo =  pmsColumnService.findAll();

            return handleResult(vo);
        }catch (Exception e){
            return handleFail(e);
        }
    }

    @GetMapping("/list")
    //@ApiOperation(httpMethod = "GET", value = "查询指定栏目下的分类")
    public RespResult findAll( Long id ){
        try {
            List<PmsColumn> vo =  pmsColumnService.lambdaQuery().eq(PmsColumn::getParentId,id).list();

            return handleResult(vo);
        }catch (Exception e){
            return handleFail(e);
        }
    }

   // @ApiOperation("栏目添加")
    @PostMapping("/save")
    public RespResult savePmsColumn(@RequestBody PmsColumn pmsColumn){
        try {
            if(CommonUtil.isEmpty(pmsColumn)) {
                throw new AeotradeException("不能为空");
            }
            pmsColumn.setColumnStatus(1);
            pmsColumnService.save(pmsColumn);
            return handleOK();
        }catch (Exception e){
            return handleFail(e);
        }
    }

  //  @ApiOperation("栏目批量添加")
    @PostMapping("/saves")
    public RespResult savePmsColumnList(@RequestBody List<PmsColumn> pmsColumn){
        try {
            if(CommonUtil.isEmpty(pmsColumn)) {
                throw new AeotradeException("不能为空");
            }
            for (PmsColumn pms  : pmsColumn ) {
                pms.setColumnStatus(1);
                pmsColumnService.save(pms);
            }
            return handleOK();
        }catch (Exception e){
            return handleFail(e);
        }
    }

   // @ApiOperation("栏目修改")
    @PostMapping("/update")
    public RespResult updatePmsColumn(@RequestBody PmsColumn pmsColumn){
        try {
            if(CommonUtil.isEmpty(pmsColumn)) {
                throw new AeotradeException("不能为空");
            }
            pmsColumn.setRevision(1);
            pmsColumnService.updateById(pmsColumn);
            return handleOK();
        }catch (Exception e){
            return handleFail(e);
        }
    }
    //@ApiOperation("栏目删除")
    @PostMapping("/delete")
    public RespResult updatePmsColumn(Long id){
        try {
            if(CommonUtil.isEmpty(id)) {
                throw new AeotradeException("Id不能为空");
            }
            pmsColumnService.removeById(id);
            return handleOK();
        }catch (Exception e){
            return handleFail(e);
        }
    }

}
