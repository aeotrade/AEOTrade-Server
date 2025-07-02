package com.aeotrade.provider.mamber.vo;


import com.aeotrade.provider.mamber.entity.UawVipClass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VipClassVos {
   // @ApiModelProperty(value="会员等级对象", allowEmptyValue=true)
    private UawVipClass uawVipClass;

   // @ApiModelProperty(value="做会员等级添加时不添加", allowEmptyValue=true)
    private List<RightsTypeVo> rightsTypeVos;

   // @ApiModelProperty(value="权益类型id+权益项数组", allowEmptyValue=true)
    private List<RightsVo> rightsVoList;

    private String menuId;

   // @ApiModelProperty(value="是否为默认会员", allowEmptyValue=true,required = true)
    private Long not;
}
