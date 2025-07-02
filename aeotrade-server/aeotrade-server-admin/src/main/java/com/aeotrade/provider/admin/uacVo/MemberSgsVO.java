package com.aeotrade.provider.admin.uacVo;


import com.aeotrade.provider.admin.entiy.SgsBankInfo;
import com.aeotrade.provider.admin.entiy.UacMember;
import lombok.Data;

import java.io.Serializable;


@Data
public class MemberSgsVO  implements Serializable {
    private UacMember uacMember;
    private SgsBankInfo sgsBank;

}
