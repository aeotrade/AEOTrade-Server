package com.aeotrade.server.message.service.impl;

import com.aeotrade.exception.AeotradeException;
import com.aeotrade.server.message.mapper.MsgMessageUserMapper;
import com.aeotrade.server.message.model.*;
import com.aeotrade.server.message.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Auther: 吴浩
 * @Date: 2024-02-07 11:14
 */
@Service
public class MsgMessageUserServiceImpl extends ServiceImpl<MsgMessageUserMapper, MsgMessageUser> implements MsgMessageUserService {
    @Autowired
    private UawVipMessageService uawVipMessageService;
    @Autowired
    private UacStaffService uacStaffService;
    @Autowired
    private UacMemberService uacMemberService;
    @Autowired
    private UacMemberStaffService uacMemberStaffService;
    @Autowired
    private MsgMessageModelService msgMessageModelService;

    @Override
    public void startMessage(MsgMessage msgMessage){
        List<MsgMessageModel> listed = msgMessageModelService.lambdaQuery()
                .eq(MsgMessageModel::getTemplateNumber, msgMessage.getTemplateNumber())
                .eq(MsgMessageModel::getIsStart,0).list();
        if(!listed.isEmpty()){
            throw new AeotradeException("模板已被禁用或已被删除不支持发送消息");
        }
        MsgMessageUser msgMessageUser=new MsgMessageUser();
        msgMessageUser.setMessageId(msgMessage.getId());
        msgMessageUser.setReadMark(0);
        msgMessageUser.setMessageTitle(msgMessage.getMessageTitle().replaceAll("}","").replaceAll("\\{",""));
        msgMessageUser.setMessageContent(msgMessage.getMessageContent().replaceAll("}","").replaceAll("\\{",""));
        msgMessageUser.setDetailsType(msgMessage.getDetailsType());
        msgMessageUser.setDetailButton(msgMessage.getDetailsButton());
        msgMessageUser.setPopFlag(msgMessage.getPopFlag());
        msgMessageUser.setMessageType(msgMessage.getMessageType());
        if(null!=msgMessage.getPopStopTime()){
            msgMessageUser.setPopStopTime(msgMessage.getPopStopTime());
        }
        if(msgMessage.getPopFlag()==1){
            msgMessageUser.setPopCover(msgMessage.getPopCover());
        }
        msgMessageUser.setSendTime(LocalDateTime.now());
        msgMessageUser.setStatus(0);
        if(msgMessage.getReceiveType()==1){
            String[] split = msgMessage.getReceiveId().split(",");
            for (String staffId : split) {
                UacStaff uacStaff = uacStaffService.getById(staffId);
                if(null!=uacStaff){
                    msgMessageUser.setId(null);
                    msgMessageUser.setStaffId(uacStaff.getId());
                    msgMessageUser.setStaffName(uacStaff.getStaffName());
                    this.save(msgMessageUser);
                }
            }
        }

        if(msgMessage.getReceiveType()==2){
            String[] split = msgMessage.getReceiveId().split(",");
            for (String uacmember : split) {
                UacMember uacMember = uacMemberService.getById(uacmember);
                if(null!=uacMember){
                    List<UacMemberStaff> uacMemberStaffs = uacMemberStaffService.lambdaQuery().eq(UacMemberStaff::getMemberId, uacMember.getId()).list();
                    for (UacMemberStaff uacMemberStaff : uacMemberStaffs) {
                        UacStaff uacStaff = uacStaffService.getById(uacMemberStaff.getStaffId());
                        if(null!=uacStaff){
                            msgMessageUser.setId(null);
                            msgMessageUser.setStaffId(uacStaff.getId());
                            msgMessageUser.setStaffName(uacStaff.getStaffName());
                            msgMessageUser.setMamberId(uacMember.getId());
                            msgMessageUser.setMamberName(uacMember.getMemberName());
                            this.save(msgMessageUser);
                        }
                    }
                }
            }
        }

        if(msgMessage.getReceiveType()==3){
            String[] split = msgMessage.getReceiveId().split(",");
            for (String vip : split) {
                List<UawVipMessage> vipMessages = uawVipMessageService.lambdaQuery()
                        .eq(UawVipMessage::getStatus,0)
                        .eq(UawVipMessage::getTypeId, vip).list();
                for (UawVipMessage vipMessage : vipMessages) {
                    if(vipMessage.getUserType()==0){
                        UacStaff uacStaff = uacStaffService.getById(vipMessage.getStaffId());
                        if(null!=uacStaff){
                            msgMessageUser.setId(null);
                            msgMessageUser.setStaffId(uacStaff.getId());
                            msgMessageUser.setStaffName(uacStaff.getStaffName());
                            this.save(msgMessageUser);
                        }
                    }
                    if(vipMessage.getUserType()==1){
                        UacMember uacMember = uacMemberService.getById(vipMessage.getMemberId());
                        if(null!=uacMember){
                            List<UacMemberStaff> uacMemberStaffs = uacMemberStaffService.lambdaQuery().eq(UacMemberStaff::getMemberId, uacMember.getId()).list();
                            for (UacMemberStaff uacMemberStaff : uacMemberStaffs) {
                                UacStaff uacStaff = uacStaffService.getById(uacMemberStaff.getStaffId());
                                if(null!=uacStaff){
                                    msgMessageUser.setId(null);
                                    msgMessageUser.setStaffId(uacStaff.getId());
                                    msgMessageUser.setStaffName(uacStaff.getStaffName());
                                    msgMessageUser.setMamberId(uacMember.getId());
                                    msgMessageUser.setMamberName(uacMember.getMemberName());
                                    this.save(msgMessageUser);
                                }
                            }
                        }
                    }
                }
            }
            
            
        }
    }
}
