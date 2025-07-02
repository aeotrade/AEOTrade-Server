package com.aeotrade.provider.mamber.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppListVo implements Serializable {

    private Long id ;

    private Long appCategoryCid;


    private String appCategoryName;


    private String appLogo;

    private String appName;

    private String subhead;


    private String url;

    private Integer isEntSpecialUse;


    private Integer isAuth;

    private String userKey;

    private String excelSourcePayAfter;

    private java.sql.Timestamp endTime;

    private String attributeName;

    private java.sql.Timestamp startTime;

    private String attributeId;

    private String serviceTypeId;


    private Integer useCount;


    private String appUnit;
}
