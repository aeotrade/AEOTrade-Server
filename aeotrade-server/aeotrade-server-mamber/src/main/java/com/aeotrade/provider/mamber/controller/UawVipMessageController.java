package com.aeotrade.provider.mamber.controller;


import cn.hutool.json.JSONUtil;
import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.mamber.entity.UacMember;
import com.aeotrade.provider.mamber.entity.UawVipMessage;
import com.aeotrade.provider.mamber.entity.UawVipPostpone;
import com.aeotrade.provider.mamber.entity.UawVipType;
import com.aeotrade.provider.mamber.service.impl.UawVipMessageServiceImpl;
import com.aeotrade.provider.mamber.service.impl.UawVipPostponeServiceImpl;
import com.aeotrade.provider.mamber.service.impl.UawVipTypeServiceImpl;

import com.aeotrade.provider.mamber.vo.MessageUpdate;
import com.aeotrade.provider.mamber.vo.PostponeTimeVo;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.CommonResult;
import com.aeotrade.suppot.PageList;
import com.aeotrade.suppot.RespResult;
import com.aeotrade.utlis.HttpRequestUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.*;

/**
 * UawVipMessageController 会员信息表 controller
 *
 * @author admin
 */
@RestController
@RequestMapping("/uaw/VipMessage/")
@Slf4j
public class UawVipMessageController extends BaseController {
    @Autowired
    private UawVipMessageServiceImpl uawVipMessageService;
    @Autowired
    private UawVipTypeServiceImpl uawVipTypeService;
    @Autowired
    private UawVipPostponeServiceImpl uawVipPostponeService;
    @Value("${hmtx.bi.url:}")
    private String biUrl;


    /**
     * @description: 运营端查询会员信息（会员列表）
     * @return:
     * @author: wuhao
     * @date:
     */
    @GetMapping("list")
    public RespResult findPageAll(@RequestParam Integer pageSize, @RequestParam Integer pageNo, @RequestParam Integer apply, @RequestParam String name, @RequestParam String typeId) {
        try {
            if (apply != 0 && apply != 1) {
                throw new AeotradeException("订单类型出错");
            }
            Long group = 0L;
            Long type = 0L;
            if (!typeId.equals("0,0")) {
                String[] split = typeId.split(",");
                group = Long.valueOf(split[0]);
                type = Long.valueOf(split[1]);
            }
            return handleResult(uawVipMessageService.findPageAll(pageSize, pageNo, apply, name, group, type));
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }

    }

    /**
     * @description: 前端会员中心首页展示该用户的会员类型、会员等级、权益类型
     * @return:
     * @author: wuhao
     * @date:
     */
    @GetMapping("findMessage")
    public RespResult findMessage(Long id, int apply) throws Exception {
        try {
            if (id == null) {
                throw new AeotradeException("企业或个人id不能为空");
            }
            if (apply != 0 && apply != 1) {
                throw new AeotradeException("用户类型错误");
            }
            return handleResult(Optional.ofNullable(uawVipMessageService.findMessage(id, apply)).orElseGet(() -> new ArrayList<>()));
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }

    /**
     * @description: 个人/企业用户完成注册开通默认会员
     * @return:
     * @author: wuhao
     * @date:
     */
    @GetMapping("loginMessage")
    public RespResult loginMessage(Long id, int apply) throws Exception {

        if (id == null || (apply != 0 && apply != 1)) {
            throw new AeotradeException("用户id和用户类型不能为空");
        }
        System.out.println("调用开始执行---------------------------------------------");
        Boolean aBoolean = uawVipMessageService.loginMessage(id, apply);
        return handleResult(aBoolean);
    }

    @GetMapping("vip")
    // @ApiOperation(httpMethod = "GET", value = "个人/企业用户 开通默认指定会员")
    public RespResult openVip(@RequestParam(value = "id") Long id,
                              @RequestParam(value = "memberName") String memberName,
                              @RequestParam(value = "uscc") String uscc,
                              @RequestParam(value = "vipClassId") Long vipClassId,
                              @RequestParam(value = "vipTypeId") Long vipTypeId) throws Exception {

        if (id == null) {
            throw new AeotradeException("用户id不能为空");
        }
        System.out.println("调用开始执行---------------------------------------------");
        Boolean aBoolean = uawVipMessageService.openVip(id, memberName, uscc, vipClassId, vipTypeId);
        return handleResult(aBoolean);
    }

    /**
     * @description: 运营端会员延期
     * @return:
     * @author: wuhao
     * @date:
     */
    @Transactional
    @PostMapping("postpone")
    //@ApiOperation(httpMethod = "POST", value = "运营端会员延期")
    public RespResult postpone(@RequestBody PostponeTimeVo postponeTimeVo) {
        if (null == postponeTimeVo) {
            throw new AeotradeException("数据不能为空");
        }
        try {
            uawVipMessageService.postpone(postponeTimeVo);
            return handleOK();
        } catch (ParseException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }

    @GetMapping("list/postpone")
    //@ApiOperation(httpMethod = "GET", value = "运营端会员延期记录展示")
    public RespResult findPostpone(@RequestParam Integer pageSize, @RequestParam Integer pageNo,
                                   String memberName, String vipTypeId) {
        if (null == vipTypeId) {
            throw new AeotradeException("会员类型id不能为空");
        }
        String[] split = vipTypeId.split(",");
        PageList<UawVipPostpone> postpone = uawVipPostponeService.findPostpone(pageSize, pageNo, memberName, Long.valueOf(split[1]));
        return handleResultList(postpone);
    }


    @GetMapping("judgeworkbench")
    //@ApiOperation(httpMethod = "GET", value = "判断用户是否有使用该工作台的权限")
    //@ApiImplicitParams({@ApiImplicitParam(name = "id", value = "个人或企业的id", required = true),
    //  @ApiImplicitParam(name = "workbenchid", value = "工作台id", required = true),
    // @ApiImplicitParam(name = "apply", value = "用户类型所属适用于0个人/1企业", required = true)})
    public RespResult workbench(@RequestParam Long id, @RequestParam Long workbenchid, @RequestParam int apply) {
        try {
            if (id == null) {
                throw new AeotradeException("用户id不能为空");
            }
            if (workbenchid == null) {
                throw new AeotradeException("工作台id不能为空");
            }
            UawVipMessage uawVipMessage = new UawVipMessage();
            uawVipMessage.setUserType(apply);
            if (apply == 0) {
                uawVipMessage.setStaffId(id);
            } else if (apply == 1) {
                uawVipMessage.setMemberId(id);
            } else {
                throw new AeotradeException("用户类型出错");
            }
            List<UawVipMessage> list = uawVipMessageService.lambdaQuery(uawVipMessage).list();
            if (list.size() != 0) {
                for (UawVipMessage vipMessage : list) {
                    UawVipType uawVipType = uawVipTypeService.getById(vipMessage.getTypeId());
                    if (uawVipType.getWorkbench().equals(workbenchid)) {
                        return handleResult(true);
                    }
                }
            }
            return handleResult(false);
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }


    //@ApiOperation(httpMethod = "GET", value = "查询所有企业有无自定义角色")
    @GetMapping("list/member/page")
    public RespResult memberPage(@RequestParam Long platformId, @RequestParam String name, Long organ) throws Exception {
        if(null!=organ && organ==102 && !StringUtils.isEmpty(biUrl)){
            Map<String, Object> map = new HashMap<>();
            map.put("name",name);
            String httpGet = HttpRequestUtils.httpGet(biUrl + "/bi_report/bi-roles-api/", map);
            return JSONObject.parseObject(httpGet, RespResult.class);
        }
        List<UacMember> uacMembers = uawVipMessageService.listMemberPage(name);
        List<UacMember> list = new ArrayList<>();
        for (UacMember record : uacMembers) {
            int i = uawVipMessageService.findRole(platformId, record.getId(), organ);
            if (i == 1) {
                record.setAtpwStatus(1);
                list.add(record);
            }
        }
        return handleResult(list);
    }

    //@ApiOperation(httpMethod = "POST", value = "以开通会员类型修改")
    @PostMapping("list/member/update")
    public RespResult memberMenuUpdate(@RequestBody MessageUpdate messageUpdate) {
        try {
            if (null == messageUpdate.getNewVipTypeId()) {
                return handleFail("请选择要修改的会员类型");
            }
            if (null == messageUpdate.getNewVipClass()) {
                return handleFail("请选择要修改的会员等级");
            }
            String update = uawVipMessageService.vipUpdate(messageUpdate);
            if (update.equals("ok")) {
                return handleOK();
            } else {
                return handleFail(update);
            }

        } catch (Exception e) {
            log.warn(e.getMessage());
            return handleFail(e.getMessage());
        }
    }

    // @ApiOperation(httpMethod = "GET", value = "企业名片调用查询企业是否有接口使用权限")
    @GetMapping("find/member/power")
    public RespResult findMemberPower(@RequestParam Long memberId, @RequestParam String url) {
        Long memberPower = uawVipMessageService.findMemberPower(memberId, url);
        if (memberPower != 0) {
            return handleResult(true);
        }
        return handleResult(false);
    }
}
