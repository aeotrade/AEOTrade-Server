package com.aeotrade.server.message.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @Auther: 吴浩
 * @Date: 2024-02-07 10:54
 */
@Getter
@Setter
@TableName("msg_message_user")
public class MsgMessageUser {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /***
     * 用户id
     */
    private Long staffId;
    /***
     * 企业id
     */
    private Long mamberId;
    /***
     * 消息id
     */
    private Long messageId;
    /***
     * 是否已读0否1是
     */
    private Integer readMark;
    /***
     * 消息标题
     */
    private String messageTitle;
    /***
     * 消息摘要
     */
    private String messageContent;
    /***
     * 查看详情按钮类型
     * 1 富文本 2 链接 3 无
     */
    private Integer detailsType;
    /***
     * 详情按钮内容
     */
    private String detailButton;
    /***
     * 是否弹窗
     */
    private Integer popFlag;
    /***
     * 弹窗封面
     */
    private String popCover;
    /***
     * 消息发送时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;
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
     * 消息类型
     */
    private Integer messageType;
    /***
     * 用户名称
     */
    private String staffName;
    /***
     * 企业名称
     */
    private String mamberName;

    /***
     * 弹窗截止时间
     */
    private String popStopTime;
}
