package com.aeotrade.provider.admin.controller;

import com.aeotrade.base.constant.AeoConstant;
import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.admin.entiy.UacStaff;
import com.aeotrade.provider.admin.service.impl.UacMemberStaffSelectServiceImpl;
import com.aeotrade.provider.admin.service.impl.UacStaffServiceImpl;
import com.aeotrade.provider.admin.service.impl.UacValidateCodeServiceImpl;
import com.aeotrade.provider.admin.uacVo.SmsSendCodeParam;
import com.aeotrade.provider.admin.uacVo.UacStaffDto;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.PageList;
import com.aeotrade.suppot.RespResult;
import com.aeotrade.utlis.CheckRepeat;
import com.aeotrade.utlis.JacksonUtil;
import com.aeotrade.utlis.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RequestMapping(value = "/uac", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//@Api(value = "Web - UacStaffController", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@RestController
public class UacStaffController extends BaseController {

    @Autowired
    private UacStaffServiceImpl uacStaffService;
    @Autowired
    private UacValidateCodeServiceImpl uacValidateCodeService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private UacMemberStaffSelectServiceImpl uacMemberStaffSelectService;

    @GetMapping("/staff")
    //@ApiOperation(httpMethod = "GET", value = "根据员工id查询员工")
    public RespResult findStaffById(Long staffId) {
        if (null == staffId) {
            throw new RuntimeException("员工id不能为空");
        }
        UacStaff uacStaff = uacStaffService.getById(staffId);
        return handleResult(uacStaff);

    }
    @GetMapping("/staff/user")
    //@ApiOperation(httpMethod = "GET", value = "根据员工id查询员工")
    public RespResult findStaffUserById(Long staffId) {
        if (null == staffId) {
            throw new RuntimeException("员工id不能为空");
        }
        UacStaff uacStaff = uacStaffService.getById(staffId);
        return handleResult(uacStaff);

    }

    @GetMapping("/staffStutas")
    //@ApiOperation(httpMethod = "GET", value = "根据员工id查询员工状态")
    public Integer findStaff(Long staffId) {
        if (null == staffId) {
            throw new RuntimeException("员工id不能为空");
        }
        UacStaff uacStaff = uacStaffService.getById(staffId);
        return uacStaff.getAuthStatus();

    }

    @PostMapping("/update")
    //@ApiOperation(httpMethod = "POST", value = "员工修改")
    public RespResult updateById(@RequestBody UacStaff uacStaff) {
        try {
            if (null == uacStaff.getId()) {
                throw new AeotradeException("员工Id不能为空");
            }
            Map<String, Object> stringObjectMap = uacStaffService.updateStaffById(uacStaff);
            return handleResult(stringObjectMap);
        } catch (Exception e) {
            return handleFail(e);
        }

    }

    //@ApiOperation(httpMethod = "POST", value = "发送验证码")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "phone", value = "手机号", required = true)})
    @PostMapping("/vcode/sms/send")
    public RespResult sendVerificationCode(@RequestBody SmsSendCodeParam phone) throws Exception {
        if (StringUtils.isBlank(phone.getPhone())) {
            return handleFail(new RuntimeException("手机号为必填项"));
        }
        String code = NumberUtils.validateCode();
        try {
            CheckRepeat.checkRepeat("com.aeotrade.provider.controller.UacStaffController.sendVerificationCode",
                    JacksonUtil.toJson(phone), 2);
            int i = uacValidateCodeService.sendSmsValidateCode(phone.getPhone(), code);
            if (i == 20) {
                return handleFail(new RuntimeException("您今日获取验证码短信次数超限，请24小时后再重试"));
            }
            if (i == 5) {
                return handleFail(new RuntimeException("您获取验证码的操作太频繁，请一小时后再重试"));
            }
            if (i == 2) {
                return handleFail(new RuntimeException("您获取验证码的操作太频繁，请一分钟后再重试"));
            }
        } catch (Exception e) {
            return handleFail(new RuntimeException("系统繁忙请稍后重试，或联系系统管理员处理"));
        }

        return handleOK();
    }


    private void checkVerificationCode(String phone, String code) throws Exception {
        //判断验证码格式
        if (!NumberUtils.testPhone(phone)) {
            throw new RuntimeException("请输入正确的手机号");
        }
        //判断数据库存的验证码是否正确
        String code1 = redisTemplate.opsForValue().get(AeoConstant.SMSREDIS_KEY + phone);
        if (code1 == null) {
            throw new RuntimeException("验证码不存在");
        }
        //验证的过期时间
        Long expire = redisTemplate.opsForValue().getOperations().getExpire(AeoConstant.SMSREDIS_KEY + phone);
        if (expire <= 0) {
            throw new RuntimeException(
                    "验证码已过期");
        }
        if (!code1.equals(code)) {
            throw new RuntimeException("验证码错误");
        }
        redisTemplate.delete(AeoConstant.SMSREDIS_KEY + phone);
    }

    /**
     * 新增一个用户
     *
     * @param uacStaff
     * @return
     */
    //@ApiOperation(httpMethod = "POST", value = "新增一个用户")
    @RequestMapping(value = "/staff", method = RequestMethod.POST)
    public RespResult saveStaff(@RequestBody UacStaffDto uacStaff) {
        if (StringUtils.isBlank(uacStaff.getValidateCode())) {
            return handleFail(new RuntimeException("请输入验证码"));
        }
        if (StringUtils.isBlank(uacStaff.getUnionid())) {
            return handleFail(new RuntimeException("unionid不能为空，请重新扫码"));
        }
        try {
            checkVerificationCode(uacStaff.getStasfTel(), uacStaff.getValidateCode());
        } catch (Exception e) {
            return handleFail(e);
        }
        uacStaffService.saveMemberAndStaff(uacStaff);

        Map map = new HashMap(1);
        Map<String, Object> bindmap = new HashMap<>(4);
        bindmap.put("memberid", uacStaff.getMemberId());
        bindmap.put("membername", uacStaff.getMemberName());
        bindmap.put("staffid", uacStaff.getStaffId());
        bindmap.put("staffname", uacStaff.getMemberName());

        map.put("bind", bindmap);

        return handleResult(map);
    }

    @GetMapping("/staff/list")
    //@ApiOperation(httpMethod = "GET", value = "根据企业Id列出企业下的所有员工")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "pageSize", value = "每页多少条", defaultValue = "10"),
            //@ApiImplicitParam(name = "pageNo", value = "当前要查询的页码", defaultValue = "1")})
    public RespResult findStaffList(Long memberId, Integer pageSize, Integer pageNo, Long workbenchId,
                                    Long organ,Long deptId,Long roleId,String staffName) {

        try {
            if (null == memberId) {
                throw new RuntimeException("企业id不能为空");
            }
            if (null == workbenchId) {
                throw new RuntimeException("工作台id不能为空");
            }
            if(pageSize==5){
                pageSize=100;
            }
            PageList<UacStaff> staffList = uacStaffService.findStaffList(memberId, pageSize, pageNo, workbenchId, organ,deptId,roleId,staffName);
            return handleResultList(staffList);
        }catch (Exception e){
            return handleFail(e.getMessage());
        }
    }

    //@ApiOperation(httpMethod = "GET", value = "修改是否第一次登录状态")
    @GetMapping("/staff/islogin")
    public RespResult isLogin(Long staffId) {
        if (null == staffId) {
            throw new RuntimeException("个人id不能为空");
        }
        UacStaff uacStaff = uacStaffService.getById(staffId);
        uacStaff.setIsLogin(1);
        uacStaff.setStatus(0);
        uacStaff.setRevision(0);
        uacStaffService.updateById(uacStaff);
        return handleOK();
    }

    //@ApiOperation(httpMethod = "GET", value = "查询是否看过登录提示")
    @GetMapping("/staff/status")
    public RespResult isstatus(Long staffId, Long memberId, String type) {
        return handleResult(uacMemberStaffSelectService.findStatus(staffId, memberId, type));
    }

    //@ApiOperation(httpMethod = "GET", value = "修改是否看过登录提示")
    @GetMapping("/staff/update/status")
    public RespResult isUpdateStatus(Long staffId, Long memberId, String type, Integer value) {
        uacMemberStaffSelectService.updateStatus(staffId, memberId, type, value);
        return handleOK();
    }


    @GetMapping("/find/staff/info")
    public RespResult findStaffInfo(Long staffId,Long memberId,Long workbenchId){
        return handleResult(uacStaffService.findStaffInfo(staffId,memberId,workbenchId));
    }
}
