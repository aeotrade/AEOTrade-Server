package com.aeotrade.provider.mamber.vo;


import com.aeotrade.provider.mamber.entity.AppCloud;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * AppCloud
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class AppCloudVO extends AppCloud implements Serializable {

    private Integer openStatus;


}

