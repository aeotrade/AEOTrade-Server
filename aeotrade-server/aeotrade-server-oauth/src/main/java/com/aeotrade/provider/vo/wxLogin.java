package com.aeotrade.provider.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Auther: 吴浩
 * @Date: 2023-07-12 9:58
 */
@Data
public class wxLogin implements Serializable {

    private String sceneValue;

    private String loginType;

    private wxUser wxUser;
}
