package com.aeotrade.server.message.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @Auther: 吴浩
 * @Date: 2024-02-07 10:49
 */
@Getter
@Setter
@TableName("msg_message")
public class MsgMessage {

        private Long id;
        /***
         * 模板编号
         */
        private String templateNumber;

        /***
         * 模板名称
         */
        private String templateName;
        /***
         * 消息类型 1公告  2通知  3提醒  4待办
         */
        private Integer messageType;
        /***
         * 消息标题
         */
        private String messageTitle;
        /***
         * 消息内容
         */
        private String messageContent;
        /***
         * 消息详情
         */
        private String messageDetails;
        /***
         * 是否弹窗
         */
        private Integer popFlag;
        /***
         * 弹窗封面
         */
        private String popCover;
        /***
         * 弹窗截止时间
         */
        private String popStopTime;
        /***
         * 查看详情按钮类型
         * 1 富文本 2 链接 3 无
         */
        private Integer detailsType;
        /***
         * 详情按钮内容
         */
        private String detailsButton;
        /***
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime creatTime;
        /***
         * 修改时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updateTime;
        /***
         * 创建人名称
         */
        private String creatName;
        /***
         * 接收人类型1 个人2 组织 3 会员类型
         */
        private Integer receiveType;
        /***
         * 接收人id（逗号分隔）
         */
        private String receiveId;
        /***
         * 接收人名称
         */
        private String receiveName;
        /***
         * 是否发送 0草稿 1发送 2撤回
         */
        private Integer messageStatus;
        /***
         * 消息来源
         */
        private String messageSource;
        /***
         * 是否定时发送
         */
        private Integer scheduleSend;
        /***
         * 定时发送时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime timing;
        /***
         * 软删除
         */
        private Integer status;
        /***
         * 消息等级（1最大级别越大消息展示越靠前）
         */
        private Integer messagePriority;
        /***
         * 消息位置
         */
        private Integer messageLocation;
        /***
         * 消息渠道
         */
        private String messageChannel;
}
