package com.aeotrade.provider.mamber.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UawTypeErp implements Serializable {
    private Long memberId;
    private Long staffId;
    private String code;
}
