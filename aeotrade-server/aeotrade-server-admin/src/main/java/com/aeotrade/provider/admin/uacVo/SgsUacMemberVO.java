package com.aeotrade.provider.admin.uacVo;


import com.aeotrade.provider.admin.entiy.SgsBankInfo;
import com.aeotrade.provider.admin.entiy.SgsCertInfo;
import lombok.Data;

import java.io.Serializable;

@Data
public class SgsUacMemberVO implements Serializable {

    private SgsBankInfo sgsBankInfo;

    private SgsCertInfo sgsCertInfo;

}
