package com.aeotrade.provider.mamber.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.data.annotation.Id;

/**
 * PmsAppMember
 */
@TableName("pms_app_member")
@Data
public class PmsAppMember {

    //@Column(name="app_id", length=19, nullable=false)
    //@ApiModelProperty(value="app_id", allowEmptyValue=true)
    private Long appId;

    //@Column(name="id", length=19, nullable=false)
    //@ApiModelProperty(value="id", allowEmptyValue=true)
    //@Id
    private Long id;

    //@Column(name="member_id", length=19, nullable=false)
    //@ApiModelProperty(value="member_id", allowEmptyValue=true)
    private Long memberId;

}

