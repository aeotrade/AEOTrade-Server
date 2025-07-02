package com.aeotrade.server.message.service.impl;

import com.aeotrade.exception.AeotradeException;
import com.aeotrade.server.message.mapper.MsgMessageMapper;
import com.aeotrade.server.message.model.MsgMessage;
import com.aeotrade.server.message.model.MsgMessageModel;
import com.aeotrade.server.message.service.MsgMessageModelService;
import com.aeotrade.server.message.service.MsgMessageService;
import com.aeotrade.server.message.service.MsgMessageUserService;
import com.aeotrade.utlis.RecordNumberUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @Auther: 吴浩
 * @Date: 2024-02-07 11:14
 */
@Service
public class MsgMessageServiceImpl extends ServiceImpl<MsgMessageMapper, MsgMessage> implements MsgMessageService {
    @Autowired
    private MsgMessageUserService msgMessageUserService;
    @Autowired
    private MsgMessageModelService msgMessageModelService;
    public void receivceMessage(String payload) {
        Map document = JSON.parseObject(payload, Map.class);
        MsgMessageModel msgMessageModel = msgMessageModelService.lambdaQuery()
                .eq(MsgMessageModel::getTemplateNumber, document.get("template_number"))
                .eq(MsgMessageModel::getIsStart,1).one();
        if(msgMessageModel!=null){
            MsgMessage msgMessage=new MsgMessage();
            msgMessage.setDetailsType(msgMessageModel.getDetailsType());
            if(msgMessageModel.getDetailsType()!=3){
                msgMessage.setDetailsButton(String.valueOf(document.get("details_button")));
            }
            msgMessage.setMessageChannel(msgMessageModel.getMessageChannel());
            if(msgMessageModel.getMessageContent().contains("{")){
                String replace = msgMessageModel.getMessageContent().replace("{", "${");
                String string = RecordNumberUtil.resolvePlaceholders(replace, document);
                msgMessage.setMessageContent(string);
            }else{
                msgMessage.setMessageContent(msgMessageModel.getMessageContent());
            }
            msgMessage.setMessageDetails(String.valueOf(document.get("message_details")));
            msgMessage.setMessageSource(String.valueOf(document.get("message_source")));
            msgMessage.setMessageStatus(1);
            msgMessage.setMessageTitle(msgMessageModel.getMessageTitle());
            msgMessage.setMessageType(msgMessageModel.getMessageType());
            msgMessage.setPopCover("http://aeotrade-launch-advisor.oss-cn-zhangjiakou.aliyuncs.com/dcbfa6be-b98c-4631-adef-c9f8097412c4.png");
            msgMessage.setPopFlag(msgMessageModel.getPopFlag());
            msgMessage.setReceiveId(String.valueOf(document.get("receive_id")));
            msgMessage.setReceiveType(Integer.parseInt(document.get("receive_type").toString()));
            msgMessage.setReceiveName(String.valueOf(document.get("receive_name")));
            msgMessage.setTemplateName(msgMessageModel.getTemplateName());
            msgMessage.setTemplateNumber(String.valueOf(document.get("template_number")));
            insert(msgMessage);
        }
    }

    @Override
    public void insert(MsgMessage msgMessage) {
        msgMessage.setCreatTime(LocalDateTime.now());
        msgMessage.setUpdateTime(LocalDateTime.now());
        msgMessage.setStatus(0);
        this.save(msgMessage);
        if(msgMessage.getMessageStatus()==1){
            if(null==msgMessage.getReceiveType() || StringUtils.isEmpty(msgMessage.getReceiveId())){
                throw new AeotradeException("没有接收人信息无法发送消息");
            }
            msgMessageUserService.startMessage(msgMessage);
        }
    }
}
