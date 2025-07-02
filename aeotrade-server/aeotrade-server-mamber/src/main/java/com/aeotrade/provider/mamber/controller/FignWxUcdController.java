package com.aeotrade.provider.mamber.controller;
import com.aeotrade.suppot.PageList;
import com.aeotrade.suppot.RespResult;
import com.aeotrade.provider.mamber.service.WxCatCudService;
import com.aeotrade.suppot.BaseController;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: yewei
 * @Date: 2020/6/17 12:16
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/wxcats/")
@CrossOrigin(maxAge = 3600,origins = "*")
//@Api(tags = "fignWxUcdController", description = "动态查询")
public class FignWxUcdController extends BaseController{

    @Autowired
    private WxCatCudService wxCatCudService;

    @GetMapping(value = "ai/usd")
    //@ApiOperation(httpMethod = "POST",value = "动态查询")
    public RespResult findAIMember(@RequestParam(value = "fieldNames")  String fieldNames, @RequestParam(value = "id")String id,
                                   @RequestParam(value = "sort")String sort, @RequestParam(value = "type") Integer type,
                                   @RequestParam(value = "tableName")String tableName, @RequestParam(value = "pageSize")Integer pageSize,
                                   @RequestParam(value = "pageNum")Integer pageNum){

        return  handleResult(wxCatCudService.findAIMember(fieldNames,id,sort,type,tableName,pageNum,pageSize  ));
    }
    @GetMapping(value = "ai/page/usd")
    //@ApiOperation(httpMethod = "POST",value = "动态查询")
    public RespResult findAIPageMember(@RequestParam(value = "fieldNames")  String fieldNames, @RequestParam(value = "id")String id,
                                       @RequestParam(value = "sort")String sort, @RequestParam(value = "type") Integer type,
                                       @RequestParam(value = "tableName")String tableName, @RequestParam(value = "pageSize")Integer pageSize,
                                       @RequestParam(value = "pageNum")Integer pageNum){

        return  handleResult(wxCatCudService.findPageAIMember(fieldNames,id,sort,type,tableName ,pageNum,pageSize ));
    }
}
