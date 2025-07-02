package com.aeotrade.provider.mamber.vo;

import com.aeotrade.provider.mamber.entity.UawVipType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VipTypeVo {
   // @ApiModelProperty(value="会员类型对象", allowEmptyValue=true,required = true)
    private UawVipType uawVipType;
   // @ApiModelProperty(value="（会员等级+权益集合）集合", allowEmptyValue=true,required = true)
    private List<VipClassVos> uawVipClassVos;
}
