package com.aeotrade.provider.dto;

import lombok.Data;

@Data
public class PmsTopicDto {

    //创建时间
    private java.sql.Timestamp createdTime;

    private Long id;

    //咨询人登陆状态
    private Integer loginStatus;

    //企业商品主题
    private Long memberTopicId;

    //咨询人邮箱
    private String sedMail;

    //咨询企业ID
    private Long sedMemberId;

    //咨询企业名称
    private String sedMemberName;

    //咨询人姓名
    private String sedName;

    //咨询人手机号
    private String sedTel;

    //咨询人关联系统的UserId
    private Long sedUserId;

    //目标商品类目
    private Long targetCategoryId;

    //类目名称
    private String targetCategoryName;

    //目标企业ID
    private Long targetMemberId;

    //目标企业名称
    private String targetMemberName;

    //目标商品id
    private Long targetProductId;

    //目标商品名称
    private String targetProductName;

    //咨询内容
    private String topicContent;

    //咨询状态
    private String topicStatus;

    //咨询时间
    private java.sql.Timestamp topicTime;

    //类型,1为商品,2 为服务
    private Integer type;

    //修改时间
    private java.sql.Timestamp updateTime;

    //回访状态, 0为未回访,1为已经回访
    private Integer visitStatus;

}
