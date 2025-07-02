package com.aeotrade.server.message.controller;

import com.aeotrade.exception.AeotradeException;
import com.aeotrade.server.message.model.MsgMessage;
import com.aeotrade.server.message.model.MsgMessageUser;
import com.aeotrade.server.message.service.MsgMessageService;
import com.aeotrade.server.message.service.MsgMessageUserService;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.PageList;
import com.aeotrade.suppot.RespResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: 吴浩
 * @Date: 2024/5/9 11:22
 */
@Slf4j
@Controller
@RestController
@RequestMapping("/message")
@CrossOrigin
public class MsgMessageController  extends BaseController {
      @Autowired
      private MsgMessageService msgMessageService;
      @Autowired
      private MsgMessageUserService msgMessageUserService;


//      //@ApiOperation(httpMethod = "GET", value = "查询全部消息列表")
      @GetMapping("/page/list")
      public RespResult modelPageList(String messageSource, Integer messageStatus, Integer messageType,
                                      String oneTime,String twoTime,Integer popFlag, @RequestParam Integer pageSize, @RequestParam Integer pageNo) {
            LambdaQueryWrapper<MsgMessage> msgMessageLambdaQueryWrapper = new LambdaQueryWrapper<>();
            if(StringUtils.isNotEmpty(messageSource)){
                  msgMessageLambdaQueryWrapper.eq(MsgMessage::getMessageSource,messageSource);
            }
            if(null!=messageStatus){
                  msgMessageLambdaQueryWrapper.eq(MsgMessage::getMessageStatus,messageStatus);
            }
            if(null!=messageType){
                  msgMessageLambdaQueryWrapper.eq(MsgMessage::getMessageType,messageType);
            }
            if(StringUtils.isNotEmpty(oneTime) && StringUtils.isNotEmpty(twoTime) ){
                  msgMessageLambdaQueryWrapper.between(MsgMessage::getCreatTime,oneTime,twoTime);
            }
            if(null!=popFlag){
                  msgMessageLambdaQueryWrapper.eq(MsgMessage::getPopFlag,popFlag);
            }
            msgMessageLambdaQueryWrapper.orderByDesc(MsgMessage::getCreatTime);
            Page<MsgMessage> page = msgMessageService.page(new Page<>(pageNo, pageSize), msgMessageLambdaQueryWrapper);
            PageList<MsgMessage> list = new PageList<>();
            list.setTotalSize(page.getTotal());
            list.setRecords(page.getRecords());
            list.setSize(page.getSize());
            list.setCurrent(page.getCurrent());
            return handleResultList(list);

      }

//      //@ApiOperation(httpMethod = "POST", value = "添加消息")
      @PostMapping("/save")
      public RespResult modelSave(@RequestBody MsgMessage msgMessage) {
            try {
                  msgMessageService.insert(msgMessage);
                  return handleOK();
            }catch (Exception e){
                  log.error("消息模版添加报错信息：{}",e.getMessage());
                  return handleFail(e.getMessage());
            }

      }

      //@ApiOperation(httpMethod = "POST", value = "编辑消息")
      @PostMapping("/update")
      public RespResult modelUpdate(@RequestBody MsgMessage msgMessage) {
            try {
                  msgMessage.setUpdateTime(LocalDateTime.now());
                  msgMessageService.saveOrUpdate(msgMessage);
                  if(msgMessage.getMessageStatus()==1){
                        if(null==msgMessage.getReceiveType() || StringUtils.isEmpty(msgMessage.getReceiveId())){
                              throw new AeotradeException("没有接收人信息无法发送消息");
                        }
                        msgMessageUserService.startMessage(msgMessage);
                  }
                  return handleOK();
            }catch (Exception e){
                  log.error("消息模版添加报错信息：{}",e.getMessage());
                  return handleFail(e.getMessage());
            }

      }

      //@ApiOperation(httpMethod = "GET", value = "修改消息状态")
      @GetMapping("/update/statue")
      public RespResult updateStatus(@RequestParam Long id,@RequestParam Integer status) {
            try{
                  MsgMessage msgMessage = msgMessageService.getById(id);
                  if(status==1){
                        if(null==msgMessage.getReceiveType() || StringUtils.isEmpty(msgMessage.getReceiveId())){
                              throw new AeotradeException("没有接收人信息无法发送消息");
                        }
                        msgMessageUserService.startMessage(msgMessage);
                  }
                  if(status==2){
                        List<MsgMessageUser> messageUsers = msgMessageUserService.lambdaQuery().eq(MsgMessageUser::getMessageId, id).list();
                        msgMessageUserService.removeByIds(messageUsers);
                  }

                  msgMessage.setUpdateTime(LocalDateTime.now());
                  msgMessage.setMessageStatus(status);
                  msgMessageService.updateById(msgMessage);
                  return handleOK();
            }catch (Exception e){
                  log.error("消息模版启/禁报错信息：{}",e.getMessage());
                  return handleFail("禁用失败！");
            }
      }
      
      //@ApiOperation(httpMethod = "GET", value = "根据id查询消息详情")
      @GetMapping("/find/message/byid")
      public RespResult findmessageByid(@RequestParam Long id){
            MsgMessage msgMessage = msgMessageService.getById(id);
            return handleResult(msgMessage);
      }

      @GetMapping("/update/all/read")
      public RespResult updateAllRead(@RequestParam Long staffId,Long msgMessageId,Long memberId,Integer messageType){
            LambdaQueryWrapper<MsgMessageUser> msgMessageLambdaQueryWrapper = new LambdaQueryWrapper<>();
            msgMessageLambdaQueryWrapper.eq(MsgMessageUser::getStaffId, staffId);
            msgMessageLambdaQueryWrapper.eq(MsgMessageUser::getReadMark, 0);
            if(null!=msgMessageId){
                  msgMessageLambdaQueryWrapper.eq(MsgMessageUser::getMessageId, msgMessageId);
            }
            if(null!=memberId){
                  msgMessageLambdaQueryWrapper.eq(MsgMessageUser::getMamberId, memberId);
            }
            if(null!=messageType){
                  msgMessageLambdaQueryWrapper.eq(MsgMessageUser::getMessageType, messageType);
            }
            List<MsgMessageUser> messageUsers = msgMessageUserService.list(msgMessageLambdaQueryWrapper);
            for (MsgMessageUser messageUser : messageUsers) {
                  messageUser.setReadMark(1);
                  msgMessageUserService.saveOrUpdate(messageUser);
            }
            return handleOK();
      }


      //@ApiOperation(httpMethod = "GET", value = "查询推送结果")
      @GetMapping("/find/message/user")
      public RespResult findMessageUser(String staffName,Long memberId,Integer readMark,Long messageId, @RequestParam Integer pageSize, @RequestParam Integer pageNo){
            LambdaQueryWrapper<MsgMessageUser> msgMessageLambdaQueryWrapper = new LambdaQueryWrapper<>();
            msgMessageLambdaQueryWrapper.eq(MsgMessageUser::getMessageId, messageId);
            if(StringUtils.isNotEmpty(staffName)){
                  msgMessageLambdaQueryWrapper.like(MsgMessageUser::getStaffName, staffName);
            }
            if(null!=memberId){
                  msgMessageLambdaQueryWrapper.eq(MsgMessageUser::getMamberId, memberId);
            }
            if(null!=readMark){
                  msgMessageLambdaQueryWrapper.eq(MsgMessageUser::getReadMark, readMark);
            }
            msgMessageLambdaQueryWrapper.orderByDesc(MsgMessageUser::getSendTime);
            Page<MsgMessageUser> page = msgMessageUserService.page(new Page<>(pageNo, pageSize), msgMessageLambdaQueryWrapper);
            PageList<MsgMessageUser> list = new PageList<>();
            list.setTotalSize(page.getTotal());
            list.setRecords(page.getRecords());
            list.setSize(page.getSize());
            list.setCurrent(page.getCurrent());
            return handleResultList(list);
      }

      //@ApiOperation(httpMethod = "GET", value = "根据用户id查询用户所有信息")
      @GetMapping("/find/message/bystaffid")
      public RespResult<Map<String, Object>> findMessageByStaffId(@RequestParam Long staffId, Long memberId, Integer readMark, Integer messageType, @RequestParam Integer pageSize, @RequestParam Integer pageNo){
            Map<String,Object> map=new HashMap<>();
            LambdaQueryWrapper<MsgMessageUser> msgMessageLambdaQueryWrapper = new LambdaQueryWrapper<>();
            msgMessageLambdaQueryWrapper.eq(MsgMessageUser::getStaffId, staffId);
            if(null!=readMark){
                  msgMessageLambdaQueryWrapper.eq(MsgMessageUser::getReadMark, readMark);
            }
            if(null!=messageType){
                  msgMessageLambdaQueryWrapper.eq(MsgMessageUser::getMessageType, messageType);
            }
            msgMessageLambdaQueryWrapper.orderByDesc(MsgMessageUser::getSendTime);
            msgMessageLambdaQueryWrapper.orderByAsc(MsgMessageUser::getReadMark);
            Page<MsgMessageUser> page = msgMessageUserService.page(new Page<>(pageNo, pageSize), msgMessageLambdaQueryWrapper);
            map.put("pagelist",page);
            if(null!=memberId){
                  List<MsgMessageUser> list= msgMessageUserService.lambdaQuery()
                          .eq(MsgMessageUser::getStaffId, staffId)
                          .eq(MsgMessageUser::getMamberId, memberId)
                          .eq(MsgMessageUser::getReadMark, 0)
                          .eq(MsgMessageUser::getPopFlag,1)
                          .and(i->i.ge(MsgMessageUser::getPopStopTime,LocalDateTime.now())
                                  .or().isNull(MsgMessageUser::getPopStopTime)
                                  .or().eq(MsgMessageUser::getPopStopTime,""))
                          .orderByDesc(MsgMessageUser::getSendTime).list();
                  map.put("popList",list);
            }else{
                  List<MsgMessageUser> list = msgMessageUserService.lambdaQuery()
                          .eq(MsgMessageUser::getStaffId, staffId)
                          .eq(MsgMessageUser::getReadMark, 0)
                          .eq(MsgMessageUser::getPopFlag,1)
                          .isNull(MsgMessageUser::getMamberId)
                          .and(i->i.ge(MsgMessageUser::getPopStopTime,LocalDateTime.now())
                                  .or().isNull(MsgMessageUser::getPopStopTime)
                                  .or().eq(MsgMessageUser::getPopStopTime,""))
                          .orderByDesc(MsgMessageUser::getSendTime).list();
                  map.put("popList",list);
            }
            for (int i = 1; i < 5; i++) {
                  map.put(i+"count",findReadMarkCount(staffId,i));
            }
            return handleResult(map);
      }

      private Long findReadMarkCount(Long staffId,Integer messageType){
          return msgMessageUserService.lambdaQuery()
                    .eq(MsgMessageUser::getStaffId, staffId)
                    .eq(MsgMessageUser::getReadMark, 0)
                    .eq(MsgMessageUser::getMessageType, messageType)
                    .count();
      }


}
