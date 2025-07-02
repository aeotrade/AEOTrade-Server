package com.aeotrade.provider.admin.controller;

import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.admin.entiy.UacMemberStaff;
import com.aeotrade.provider.admin.entiy.UacStaff;
import com.aeotrade.provider.admin.service.impl.UacMemberStaffServiceImpl;
import com.aeotrade.provider.admin.uacVo.MemberInfo;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.PageList;
import com.aeotrade.suppot.RespResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/sys/")
//@Api(tags = "管理员,角色接口")
@Slf4j
public class UUacRoleController extends BaseController {

    @Autowired
    private UacMemberStaffServiceImpl uacMemberStaffService;

    //@ApiOperation(httpMethod = "GET", value = "根据企业ID查询子管理员")
    @GetMapping("sub/admin/list")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "pageSize", value = "每页多少条", defaultValue = "10"),
            //@ApiImplicitParam(name = "pageNo", value = "当前要查询的页码", defaultValue = "1")})
    public RespResult findSubAdminList(Integer pageSize , Integer pageNo ,Long memberId) {
        try {
            PageList<UacStaff> uacStaffs =  uacMemberStaffService.findSubAdminList(pageSize,pageNo,memberId);
            return handleResultList(Optional.ofNullable(uacStaffs).orElseGet(()->new PageList<>()));
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    //@ApiOperation(httpMethod = "GET", value = "查询主管理员")
    @GetMapping("/master/admin")
    public RespResult findMasterAdmin(Long memberId ) {
        try {
            if(memberId == null) {
                throw new AeotradeException("企业ID不能为空");
            }
            UacStaff uacStaff=  uacMemberStaffService.findMasterAdmin(memberId);
            return handleResult(Optional.ofNullable(uacStaff).orElseGet(()->new UacStaff()));
        } catch (Exception e) {
            return handleFail(e);
        }
    }
    //@ApiOperation(httpMethod = "POST", value = "修改/添加管理员")
    @PostMapping("admin/update")
    public RespResult upadteAdmin(@RequestBody List<UacMemberStaff> uacMemberStaff) {
        try {
            if(uacMemberStaff == null) {
                throw new AeotradeException("参数不能为空");
            }
            uacMemberStaffService.upadteAdmin(uacMemberStaff);
            return handleOK();
        } catch (Exception e) {
            return handleFail(e);
        }
    }


    //@ApiOperation(httpMethod = "GET", value = "设置主管理员")
    @GetMapping("admin")
    public RespResult setAdmin(Long memberId ,Long staffId) {
        try {
            if(memberId == null) {
                throw new AeotradeException("企业ID不能为空");
            }
            if(staffId == null) {
                throw new AeotradeException("员工ID不能为空");
            }
            uacMemberStaffService.usetAdmin(memberId,staffId);
            return handleOK();
        } catch (Exception e) {
            return handleFail(e);
        }
    }
    //@ApiOperation(httpMethod = "GET", value = "根据企业ID查询企业信息")
    @GetMapping("user/info")
    public RespResult findUserInfo(Long memberId ) {
        try {
            if(memberId == null) {
                throw new AeotradeException("企业ID不能为空");
            }

          MemberInfo info = uacMemberStaffService.findUserInfo(memberId);

            return handleResult(info);
        } catch (Exception e) {
            return handleFail(e);
        }
    }

//    //@ApiOperation(httpMethod = "GET", value = "根据用户ID查询所有权限的列表")
//    @RequestMapping(value = "/user",method = RequestMethod.GET)
//    public Optional<List<String>> queryAuthority(@RequestParam("userid") String userid) {
//        return uacRoleService.findAuthority(Long.valueOf(userid));
//    }

}
