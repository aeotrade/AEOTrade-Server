package com.aeotrade.server.message.controller;

import com.aeotrade.exception.AeotradeException;
import com.aeotrade.server.message.model.MsgMessageModel;
import com.aeotrade.server.message.service.MsgMessageModelService;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.RespResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Auther: 吴浩
 * @Date: 2024/5/9 9:31
 */
@Slf4j
@Controller
@RestController
@RequestMapping("/model")
@CrossOrigin
public class MsgMessageModelController extends BaseController {
    @Autowired
    private MsgMessageModelService msgMessageModelService;

    //@ApiOperation(httpMethod = "GET", value = "查询全部消息模版列表")
    @GetMapping("/list")
    public RespResult modelList(Integer isStart) {
        if(null==isStart){
            List<MsgMessageModel> list = msgMessageModelService.lambdaQuery()
                    .eq(MsgMessageModel::getStatus, 0).orderByDesc(MsgMessageModel::getCreatTime).list();
            return handleResult(list);
        }else{
            List<MsgMessageModel> list = msgMessageModelService.lambdaQuery().eq(MsgMessageModel::getIsStart,isStart)
                    .eq(MsgMessageModel::getStatus, 0).orderByDesc(MsgMessageModel::getCreatTime).list();
            return handleResult(list);
        }

    }

    //@ApiOperation(httpMethod = "POST", value = "添加消息模版")
    @PostMapping("/save")
    public RespResult modelSave(@RequestBody MsgMessageModel msgMessageModel) {
        try {
            List<MsgMessageModel> listed = msgMessageModelService.lambdaQuery().eq(MsgMessageModel::getTemplateNumber, msgMessageModel.getTemplateNumber()).list();
            if(!listed.isEmpty()){
                throw new AeotradeException("消息模版编号不能重复");
            }
            msgMessageModel.setIsStart(1);
            msgMessageModel.setCreatTime(LocalDateTime.now());
            msgMessageModel.setUpdateTime(LocalDateTime.now());
            msgMessageModel.setStatus(0);
            msgMessageModelService.save(msgMessageModel);
            return handleOK();
        }catch (Exception e){
            log.error("消息模版添加报错信息：{}",e.getMessage());
            return handleFail(e);
        }

    }

    //@ApiOperation(httpMethod = "POST", value = "修改消息模版")
    @PostMapping("/update")
    public RespResult modelUpdate(@RequestBody MsgMessageModel msgMessageModel) {
        try {
            msgMessageModel.setUpdateTime(LocalDateTime.now());
            msgMessageModel.setStatus(0);
            msgMessageModelService.saveOrUpdate(msgMessageModel);
            return handleOK();
        }catch (Exception e){
            log.error("消息模版修改报错信息：{}",e.getMessage());
            return handleFail("修改失败！");
        }
    }

    //@ApiOperation(httpMethod = "GET", value = "删除消息模版")
    @GetMapping("/delete")
    public RespResult modelDelete(@RequestParam Long id) {
        try{
            MsgMessageModel byId = msgMessageModelService.getById(id);
            if(byId.getIsStart()==1){
                throw new AeotradeException("模版启用不能被删除");
            }
            MsgMessageModel msgMessageModel = new MsgMessageModel();
            msgMessageModel.setId(id);
            msgMessageModel.setUpdateTime(LocalDateTime.now());
            msgMessageModel.setStatus(1);
            msgMessageModelService.updateById(msgMessageModel);
            return handleOK();
        }catch (Exception e){
            log.error("消息模版删除报错信息：{}",e.getMessage());
            return handleFail(e);
        }
    }

    //@ApiOperation(httpMethod = "GET", value = "启/禁消息模版")
    @GetMapping("/stop")
    public RespResult modelstop(@RequestParam Long id,@RequestParam Integer isStart) {
        try{
            MsgMessageModel msgMessageModel = new MsgMessageModel();
            msgMessageModel.setId(id);
            msgMessageModel.setUpdateTime(LocalDateTime.now());
            msgMessageModel.setIsStart(isStart);
            msgMessageModelService.updateById(msgMessageModel);
            return handleOK();
        }catch (Exception e){
            log.error("消息模版启/禁报错信息：{}",e.getMessage());
            return handleFail("禁用失败！");
        }
    }

}
