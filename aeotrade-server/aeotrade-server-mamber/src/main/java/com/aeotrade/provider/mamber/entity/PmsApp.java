package com.aeotrade.provider.mamber.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import javax.annotation.Generated;
import java.time.LocalDateTime;

/**
 * PmsApp
 */

@Generated("titan.lightbatis.web.generate.LombokBeanSerializer")
@Data
@TableName("pms_app")
public class PmsApp {

    //@Column(name="app_log")
    //@ApiModelProperty(value="应用图片", allowEmptyValue=true)
    private String appLog;

    //@Column(name="app_name", length=50)
    //@ApiModelProperty(value="应用名称", allowEmptyValue=true)
    private String appName;

    //@Column(name="app_status", length=10)
    //@ApiModelProperty(value="应用状态: 0:未启用 1:启用", allowEmptyValue=true)
    private Integer appStatus;

    //@Column(name="auth_status", length=10, nullable=false)
    //@ApiModelProperty(value="认证状态,0未认证,1已认证", allowEmptyValue=true)
    private Integer authStatus;

    //@Column(name="created_time", length=19)
    //@ApiModelProperty(value="创建时间", allowEmptyValue=true)
    //@AutoGenerated
    private LocalDateTime createdTime;

    //@Column(name="id", length=19, nullable=false)
    //@ApiModelProperty(value="id", allowEmptyValue=true)
    //@Id
    private Long id;

    //@Column(name="member_id", length=19)
    //@ApiModelProperty(value="企业ID", allowEmptyValue=true)
    private Long memberId;

    //@Column(name="new_function", length=65535)
    //@ApiModelProperty(value="我想要的新功能", allowEmptyValue=true)
    private String newFunction;

    //@Column(name="product_detils", length=65535)
    //@ApiModelProperty(value="产品介绍", allowEmptyValue=true)
    private String productDetils;

    //@Column(name="revision", length=10)
    //@ApiModelProperty(value="乐观锁", allowEmptyValue=true)
    //@Revision
    private Integer revision;

    //@Column(name="service_mamual", length=65535)
    //@ApiModelProperty(value="使用手册", allowEmptyValue=true)
    private String serviceMamual;

    //@Column(name="service_package", length=65535)
    //@ApiModelProperty(value="服务套餐", allowEmptyValue=true)
    private String servicePackage;

    //@Column(name="subhead", length=100)
    //@ApiModelProperty(value="副标题", allowEmptyValue=true)
    private String subhead;

    //@Column(name="updated_time", length=19)
    //@ApiModelProperty(value="更新时间", allowEmptyValue=true)
    //@AutoGenerated(event=titan.lightbatis.annotations.GeneratedEvent.Update)
    private LocalDateTime updatedTime;

    //@Column(name="url")
    //@ApiModelProperty(value="应用链接", allowEmptyValue=true)
    private String url;

}

