package com.aeotrade.provider.mamber.vo;


import com.aeotrade.provider.mamber.entity.UawRightsType;
import com.aeotrade.provider.mamber.entity.UawVipClass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 *@description:
用于返回首页用户会员类型、会员等级、权益类型
 *@return:
 *@author: wuhao
 *@date:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class homePageVo {
    //@ApiModelProperty(value="会员分组id", allowEmptyValue=true)
    private Long groupId;
    //@ApiModelProperty(value="会员分组名称", allowEmptyValue=true)
    private String groupName;
   // @ApiModelProperty(value="会员类型id", allowEmptyValue=true)
    private Long viptypeId;
   // @ApiModelProperty(value="会员状态", allowEmptyValue=true)
    private int vipstatus;//0到期1没到期
   // @ApiModelProperty(value="会员类型名称", allowEmptyValue=true)
    private String typeName;
   // @ApiModelProperty(value="会员类型图标", allowEmptyValue=true)
    private String typeIoc;
   // @ApiModelProperty(value="结束时间", allowEmptyValue=true)
    private LocalDateTime entTime;
    //@ApiModelProperty(value="登录链接地址", allowEmptyValue=true)
    private String loginUrl;
   // @ApiModelProperty(value="标识", allowEmptyValue=true)
    private String code;
    //@ApiModelProperty(value="工作台名称", allowEmptyValue=true)
    private String workbenchName;
   // @ApiModelProperty(value="工作台id", allowEmptyValue=true)
    private Long workbenchId;
   // @ApiModelProperty(value="会员类型描述", allowEmptyValue=true)
    private String description;
   // @ApiModelProperty(value="频道栏目id", allowEmptyValue=true)
    private Long channelColumnsId;
    //@ApiModelProperty(value="会员等级对象", allowEmptyValue=true)
    private UawVipClass uawVipClass;
   // @ApiModelProperty(value="工作台图标", allowEmptyValue=true)
    private String workbenchIco;
   // @ApiModelProperty(value="工作台描述", allowEmptyValue=true)
    private String workbenchDescription;
   // @ApiModelProperty(value="是否为默认工作台0否1是", allowEmptyValue=true)
    private Integer workbenchStatus;
    //@ApiModelProperty(value="广告图", allowEmptyValue=true)
    private String banner;
   // @ApiModelProperty(value="权益类型集合", allowEmptyValue=true)
    private List<UawRightsType> uawRightsTypes;
}
