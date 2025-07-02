package com.aeotrade.provider.admin.controller;


import com.aeotrade.provider.admin.entiy.UacDept;
import com.aeotrade.provider.admin.entiy.UacDeptStaff;
import com.aeotrade.provider.admin.entiy.UacMember;
import com.aeotrade.provider.admin.service.UacDeptService;
import com.aeotrade.provider.admin.service.UacDeptStaffService;
import com.aeotrade.provider.admin.service.UacMemberService;
import com.aeotrade.provider.admin.entiy.UacMemberStaff;
import com.aeotrade.provider.admin.service.UacMemberStaffService;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.RespResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Auther: 吴浩
 * @Date: 2023-11-30 10:51
 */
@RestController
@RequestMapping("/dept")
@Slf4j
public class UacDeptController extends BaseController {

    @Autowired
    private UacDeptService uacDeptService;
    @Autowired
    private UacMemberService uacMemberService;
    @Autowired
    private UacMemberStaffService uacMemberStaffService;
    @Autowired
    private UacDeptStaffService uacDeptStaffService;

    public static List<UacDept> streamMethod(Long parentId, List<UacDept> treeList) {
        List<UacDept> list = new ArrayList<>();
        Optional.ofNullable(treeList).orElse(new ArrayList<>())
                .stream()
                .filter(root -> root.getParentId().equals(parentId))
                .forEach(tree -> {
                    List<UacDept> children = streamMethod(tree.getId(), treeList);
                    tree.setChildren(children);
                    list.add(tree);
                });
        return list;
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public RespResult create(@RequestBody UacDept uacDept) {
        if (uacDept.getParentId() == 0) {
            uacDept.setParentId(uacDept.getMemberId());
        }
        uacDept.setDeptCount(0L);
        uacDept.setCreatedTime(LocalDateTime.now());
        uacDept.setUpdateTime(LocalDateTime.now());
        boolean save = uacDeptService.save(uacDept);
        return handleResult(save);
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public RespResult update(@RequestBody UacDept uacDept) {
        if (uacDept.getParentId() == 0) {
            uacDept.setParentId(uacDept.getMemberId());
        }
        List<UacDept> deptByParentId = findDeptByParentId(uacDept.getId(),new ArrayList<>());
        if(deptByParentId.size()!=0){
            for (UacDept dept : deptByParentId) {
                if(dept.getId().equals(uacDept.getParentId())){
                    return handleFail("不能将子级部门设置为父部门");
                }
            }
        }
        uacDept.setUpdateTime(LocalDateTime.now());
        boolean save = uacDeptService.saveOrUpdate(uacDept);
        return handleResult(save);
    }

    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public RespResult delete(@RequestParam Long id) {
        UacDept uacDept = uacDeptService.getById(id);
        if (uacDept.getDeptCount() != 0) {
            return handleFail("请先将当前部门或下级部门员工移到其它部门再删除");
        }
        boolean remove = uacDeptService.removeById(uacDept);
        List<UacDept> deptByParentId = findDeptByParentId(id,new ArrayList<>());
        uacDeptService.removeBatchByIds(deptByParentId);
        return handleResult(remove);
    }
    public List<UacDept> findDeptByParentId(Long parentId,List<UacDept> dept) {
        List<UacDept> list = uacDeptService.lambdaQuery().eq(UacDept::getParentId, parentId).list();
        if (list.size()==0) {
            return dept;
        } else {
            dept.addAll(list);
            for (UacDept uacDept : list) {
                this.findDeptByParentId(uacDept.getId(),dept);
            }
        }
        return dept;
    }

    @RequestMapping(value = "/findByid", method = RequestMethod.GET)
    public RespResult findById(@RequestParam Long id) {
        UacDept uacDept = uacDeptService.getById(id);
        return handleResult(uacDept);
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public RespResult list(@RequestParam Long memberId) {
        UacMember uacMember = uacMemberService.getById(memberId);
        List<UacMemberStaff> uacMemberStaffs = uacMemberStaffService.lambdaQuery()
                .eq(UacMemberStaff::getMemberId, memberId)
                .eq(UacMemberStaff::getStaffId, uacMember.getStaffId()).list();
        if (uacMemberStaffs.size() == 0) {
            UacMemberStaff uacMemberStaff = new UacMemberStaff();
            uacMemberStaff.setMemberId(memberId);
            uacMemberStaff.setStaffId(uacMember.getStaffId());
            uacMemberStaff.setKindId(1L);
            uacMemberStaff.setCreatedTime(uacMember.getCreatedTime());
            uacMemberStaff.setIsAdmin(1);
            uacMemberStaffService.save(uacMemberStaff);
        }
        Long count = uacMemberStaffService.lambdaQuery()
                .eq(UacMemberStaff::getMemberId, memberId).count();
        UacDept uacDept = new UacDept();
        uacDept.setId(memberId);
        uacDept.setDeptName(uacMember.getMemberName());
        uacDept.setDeptCount(count);
        uacDept.setMemberId(memberId);
        uacDept.setParentId(0L);
        List<UacDept> list = uacDeptService.lambdaQuery()
                .eq(UacDept::getMemberId, memberId).orderByAsc(UacDept::getCreatedTime).list();
        if (list.size() == 0) {
            list.add(uacDept);
            return handleResult(list);
        } else {
            Boolean deptCount = findDeptCount(memberId);
            List<UacDept> lists = uacDeptService.lambdaQuery()
                    .eq(UacDept::getMemberId, memberId).orderByAsc(UacDept::getCreatedTime).list();
            lists.add(uacDept);
            List<UacDept> uacDepts = streamMethod(0L, lists);
            return handleResult(uacDepts);
        }
    }


    public Boolean findDeptCount(Long memberId){
        List<UacDept> list = uacDeptService.lambdaQuery()
                .eq(UacDept::getMemberId, memberId).orderByAsc(UacDept::getCreatedTime).list();
        List<UacDept> uacDepts = streamMethod(memberId, list);
        Set<Long> staffIds=new HashSet<>();
        updateDeptCount(uacDepts,staffIds,memberId);
        return true;
    }
    public Set<Long> updateDeptCount(List<UacDept> treeList, Set<Long> staffids,Long memberId) {
        for (UacDept uacDept : treeList) {
            Set<Long> staffid=new HashSet<>();
            if (uacDept.getChildren().size() != 0) {
                Set<Long> childrenIds=new HashSet<>();
                Set<Long> longs = updateDeptCount(uacDept.getChildren(), childrenIds,memberId);
                Set<Long> collect = uacDeptStaffService.lambdaQuery().eq(UacDeptStaff::getDeptId, uacDept.getId()).list()
                        .stream().map(UacDeptStaff::getStaffId).collect(Collectors.toSet());
                collect.addAll(longs);
                staffid.addAll(collect);

            } else {
                Set<Long> collect = uacDeptStaffService.lambdaQuery().eq(UacDeptStaff::getDeptId, uacDept.getId()).list()
                        .stream().map(UacDeptStaff::getStaffId).collect(Collectors.toSet());
                staffid.addAll(collect);
            }
            uacDept.setDeptCount((long) staffid.size());
            uacDeptService.updateById(uacDept);
            staffids.addAll(staffid);
        }
        return staffids;
    }

}
