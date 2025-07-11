package com.aeotrade.provider.mamber.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author yewei
 * @Date 2022/5/30 10:05
 * @Description:
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppCloudDto implements Serializable {




    private Long appCategoryCid;


    private String appCategoryName;


    private String appLogo;


    private String appName;


    private Integer appPayCount;

    private Integer appPv;

//    @Column(name="created_time", length=19)
//    @AutoGenerated
//    @ApiModelProperty(value="创建时间", allowEmptyValue=true)
    private LocalDateTime createdTime;



//    @Column(name="id", length=19, nullable=false)
//    @ApiModelProperty(value="id", allowEmptyValue=true)
//    @Id
    private Long id;

//    @Column(name="is_auth", length=10, nullable=false)
//    @ApiModelProperty(value="是否需要认证,1需要,0不需要", allowEmptyValue=true)
    private Integer isAuth;

//    @Column(name="is_ent_special_use", length=10)
//    @ApiModelProperty(value="是否企业专用 1-是 0-否", allowEmptyValue=true)
    private Integer isEntSpecialUse;

//    @Column(name="member_id", length=19)
//    @ApiModelProperty(value="企业ID", allowEmptyValue=true)
    private Long memberId;

//    @Column(name="product_detail", length=65535)
//    @ApiModelProperty(value="产品详情", allowEmptyValue=true)
    private String productDetail;

//    @Column(name="product_instruction", length=65535)
//    @ApiModelProperty(value="使用说明", allowEmptyValue=true)
    private String productInstruction;

//    @Column(name="publish_status", length=10)
//    @ApiModelProperty(value="上架状态0->下架1->上架", allowEmptyValue=true)
    private Integer publishStatus;



//    @Column(name="subhead", length=65535)
//    @ApiModelProperty(value="副标题", allowEmptyValue=true)
    private String subhead;

//    @Column(name="tag_list")
//    @ApiModelProperty(value="标签列表", allowEmptyValue=true)
    private String tagList;

//    @Column(name="updated_time", length=19)
//    @AutoGenerated(event=titan.lightbatis.annotations.GeneratedEvent.Update)
//    @ApiModelProperty(value="更新时间", allowEmptyValue=true)
    private LocalDateTime updatedTime;

//    @Column(name="url", length=65535)
//    @ApiModelProperty(value="应用链接", allowEmptyValue=true)
    private String url;

//    @Column(name="manage_url", length=65535)
//    @ApiModelProperty(value="应用链接", allowEmptyValue=true)
    private String manageUrl;

//    @Column(name="sources", length=65535)
//    @ApiModelProperty(value="采集数据来源", allowEmptyValue=true)
    private String sources;

    private Integer appSort;

    private String memberName;

    private List<VipType> typeNames;

    private Integer isShow;

    public AppCloudDto(String appName, String appCategoryName, String appLogo, Long id,Long appCategoryCid, String subhead,Integer isShow) {
        this.id=id;
        this.appName=appName;
        this.appCategoryName=appCategoryName;
        this.appLogo=appLogo;
        this.subhead=subhead;
        this.appCategoryCid=appCategoryCid;
        this.isShow=isShow;
    }

}
