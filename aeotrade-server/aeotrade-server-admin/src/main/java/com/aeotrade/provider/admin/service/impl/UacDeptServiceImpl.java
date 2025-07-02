package com.aeotrade.provider.admin.service.impl;


import com.aeotrade.provider.admin.entiy.UacDept;
import com.aeotrade.provider.admin.entiy.UacDeptStaff;
import com.aeotrade.provider.admin.mapper.UacDeptMapper;
import com.aeotrade.provider.admin.service.UacDeptService;
import com.aeotrade.provider.admin.service.UacDeptStaffService;
import com.github.yulichang.base.MPJBaseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 部门表 服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-03
 */
@Service
public class UacDeptServiceImpl extends MPJBaseServiceImpl<UacDeptMapper, UacDept> implements UacDeptService {
    @Autowired
    private UacDeptStaffService uacDeptStaffService;

    @Override
    public List<Long> findListById(Long deptId) {
        List<Long> deptids = new ArrayList<>();
        List<UacDept> list = this.lambdaQuery().eq(UacDept::getId, deptId).list();
        if(list.size()==0){
            return deptids;
        }
        deptids=this.toTreeDept(deptId,deptids);
        deptids.add(deptId);
        return uacDeptStaffService.lambdaQuery().select(UacDeptStaff::getStaffId).in(UacDeptStaff::getDeptId,deptids).groupBy(UacDeptStaff::getStaffId)
                .list().stream().map(UacDeptStaff::getStaffId).collect(Collectors.toList());
    }

    private List<Long> toTreeDept(Long deptId,List<Long> deptids){
        List<UacDept> list = this.lambdaQuery().eq(UacDept::getParentId, deptId).list();
        if(list.size()==0){
            return deptids;
        }
        for (UacDept uacDept : list) {
            deptids.add(uacDept.getId());
            toTreeDept(uacDept.getId(),deptids);
        }
        return deptids;
    }
}
