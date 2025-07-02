package com.aeotrade.provider.admin.uacVo;


import com.aeotrade.provider.admin.entiy.SgsCertInfo;
import com.aeotrade.provider.admin.entiy.UacMember;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;

@Data
public class SgsMemberVO  implements Serializable {
    @Autowired
    private UacMember uacMember;
    @Autowired
    private SgsCertInfo sgsMember;
}
