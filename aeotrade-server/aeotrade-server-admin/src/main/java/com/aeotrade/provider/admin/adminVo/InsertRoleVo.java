package com.aeotrade.provider.admin.adminVo;

import com.aeotrade.provider.admin.entiy.UacRoleDocment;
import lombok.Data;

import java.util.List;

/**
 * @Auther: 吴浩
 * @Date: 2024-01-11 14:22
 */
@Data
public class InsertRoleVo {
    private Long id;
    private String name;
    private String description;
    private Integer status;
    private Integer isModel;
    private String organ;
    private String orgid;
    private Integer platform;
    private Long platformId;
    private List<String> menuIds;
    private List<UacRoleDocment> uacRoleDocments;
}
