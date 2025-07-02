package com.aeotrade.server.message.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @Auther: 吴浩
 * @Date: 2024-02-07 10:52
 */
@Getter
@Setter
@TableName("msg_message_model")
public class MsgMessageModel {


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
     * 消息类型（1 公告 2 通知）
     */
    private Integer messageType;
    /***
     * 消息标题
     */
    private String messageTitle;
    /***
     * 消息摘要
     */
    private String messageContent;
    /***
     * 是否弹窗
     */
    private Integer popFlag;
    /***
     * 消息内容类型（1 富文本 2链接 3无）
     */
    private Integer detailsType;
    /***
     * 详情按钮是否自定义0否1是
     */
    private Integer detailButtonType;
    /***
     * 是否启用
     */
    private Integer isStart;
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
     * 软删除
     */
    private Integer status;
    /***
     * 消息渠道
     */
    private String messageChannel;
}
