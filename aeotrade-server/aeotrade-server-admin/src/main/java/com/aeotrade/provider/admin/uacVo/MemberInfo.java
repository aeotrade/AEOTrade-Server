package com.aeotrade.provider.admin.uacVo;


import com.aeotrade.provider.admin.entiy.UacStaff;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberInfo implements Serializable {
    private UacStaff mainInfo;

    private Integer staffSize;

    private Integer adminSize;

    private Integer sgsStatus;

}
