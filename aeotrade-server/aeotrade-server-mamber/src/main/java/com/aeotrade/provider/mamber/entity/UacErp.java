package com.aeotrade.provider.mamber.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import org.springframework.data.annotation.Id;


/**
 * UacErp
 */

@Data
@TableName("uac_erp")
public class UacErp {


    private String code;


    private java.sql.Timestamp createdTime;


    private Long id;


    private Long memberId;


    private String memberName;


    private Integer memberStatus;


    private Integer revision;


    private Integer role;


    private Long staffId;


    private String staffName;


    private Integer status;

    private java.sql.Timestamp updatedTime;

}

