package com.aeotrade.provider.mamber.controller;


import com.aeotrade.base.constant.SgsConstant;
import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.mamber.entity.UawVipClass;
import com.aeotrade.provider.mamber.entity.UawVipMessage;
import com.aeotrade.provider.mamber.entity.UawVipType;
import com.aeotrade.provider.mamber.entity.UawVipTypeGroup;
import com.aeotrade.provider.mamber.service.impl.*;

import com.aeotrade.provider.mamber.vo.UawVipTypeDto;
import com.aeotrade.provider.mamber.vo.UawVipTypeVO;
import com.aeotrade.provider.mamber.vo.VipClassVos;
import com.aeotrade.provider.mamber.vo.VipTypeVo;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.RespResult;
import com.alibaba.druid.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * UawVipTypeController 会员类型表 controller
 *
 * @author admin
 */
@RestController
@RequestMapping("/uaw/VipType/")
@Slf4j
public class UawVipTypeController extends BaseController {
    @Autowired
    private UawVipTypeServiceImpl uawVipTypeService;
    @Autowired
    private UawVipMessageServiceImpl uawVipMessageService;
    @Autowired
    private UawVipClassServiceImpl uawVipClassService;
    @Autowired
    private UawAptitudesServiceImpl uawAptitudesService;
    @Autowired
    private UawVipTypeGroupServiceImpl uawVipTypeGroupService;
    @Autowired
    private UawWorkbenchServiceImpl uawWorkbenchService;

    /**
     * 前端购买会员套餐根据会员分组id进行会员类型展示
     *
     * @param
     * @return
     */
    @GetMapping("findBygid")
    //@ApiOperation(httpMethod = "GET", value = "前端购买会员套餐根据会员分组id进行会员类型展示")
    //@ApiImplicitParam(name = "id", value = "会员分组id", required = true)
    public RespResult findBygid(Long id) {
        try {
            if (id == null) {
                throw new AeotradeException("会员分组id不能为空");
            }
            UawVipType uawVipType = new UawVipType();
            uawVipType.setGroupId(id);
            List<UawVipType> list = uawVipTypeService.lambdaQuery(uawVipType).list();
            return handleResult(list);
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }

    /**
     * 运营端根据会员分组类型进行会员类型展示
     *
     * @param
     * @return
     */
    @GetMapping("list")
    //@ApiOperation(httpMethod = "GET", value = "运营端根据会员分组类型进行会员类型展示")
    //@ApiImplicitParams({
           // @ApiImplicitParam(name = "apply", value = "会员类型所属适用于0个人/1企业", required = true),
           // @ApiImplicitParam(name = "pageSize", value = "每页多少条", defaultValue = "10", required = true),
           // @ApiImplicitParam(name = "pageNo", value = "当前要查询的页码", defaultValue = "1", required = true)})
    public RespResult findAll(@RequestParam Integer pageSize, @RequestParam Integer pageNo, @RequestParam Integer apply, Long groupId) {
        try {
            if (apply != 0 && apply != 1) {
                throw new AeotradeException("会员分组类型不能为空");
            }
            if(null==groupId){
                groupId=0L;
            }
            return handleResult(uawVipTypeService.findAll(pageSize, pageNo, apply,groupId));
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }

    /**
     * @description: 运营端根据会员类型id删除会员类型
     * @return:
     * @author: wuhao
     * @date:
     */
    @GetMapping("deleteBygid")
   // @ApiOperation(httpMethod = "GET", value = "运营端根据会员类型id删除会员类型")
   // @ApiImplicitParam(name = "id", value = "会员类型id", required = true)
    public RespResult deleteById(Long id) {
        try {
            if (id == null) {
                throw new AeotradeException("会员类型id不能为空");
            }
            UawVipMessage uawVipMessage = new UawVipMessage();
            uawVipMessage.setTypeId(id);
            UawVipClass uawVipClass = new UawVipClass();
            uawVipClass.setTypeId(id);
            UawVipType uawVipType = new UawVipType();
            uawVipType.setId(id);
            List<UawVipMessage> list = uawVipMessageService.lambdaQuery(uawVipMessage).list();
            if (list.size() != 0) {
                return handleFail("该会员类型正在使用中不能删除");
            } else {
                uawVipType.setStatus(0);
                uawVipTypeService.updateById(uawVipType);
                List<UawVipClass> vipClasses = uawVipClassService.lambdaQuery(uawVipClass).list();
                for (UawVipClass vipClass : vipClasses) {
                    uawVipClassService.delete(vipClass);

                }
                return handleOK();
            }
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }

    /**
     * @description: 运营端修改会员类型回显会员类型，会员等级，权益类型，权益项
     * @return:
     * @author: wuhao
     * @date:
     */
    @GetMapping("findByid")
   // @ApiOperation(httpMethod = "GET", value = "运营端修改会员类型回显会员类型，会员等级，权益类型，权益项")
   // @ApiImplicitParam(name = "id", value = "会员类型id", required = true)
    public RespResult findByid(Long id) {
        try {
            if (id == null) {
                throw new AeotradeException("会员类型id不能为空");
            }
            //得到会员类型对象
            UawVipType uawVipType = uawVipTypeService.getById(id);
            //得到会员等级、权益类型、权益封装集合
            List<VipClassVos> bytid = uawVipClassService.findBytid(id,0);
            VipTypeVo vipTypeVo = new VipTypeVo();
            vipTypeVo.setUawVipType(uawVipType);
            vipTypeVo.setUawVipClassVos(bytid);
            return handleResult(vipTypeVo);
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }

    /**
     * @description: 运营端根据会员类型id修改会员类型状态
     * @return:
     * @author: wuhao
     * @date:
     */
    @GetMapping("updatestatus")
   // @ApiOperation(httpMethod = "GET", value = "运营端根据会员类型id修改会员类型状态")
    //@ApiImplicitParam(name = "id", value = "会员类型id", required = true)
    public RespResult updateStatus(Long id) {
        try {
            if (id == null) {
                throw new AeotradeException("会员类型id不能为空");
            }
            UawVipMessage uawVipMessage = new UawVipMessage();
            uawVipMessage.setTypeId(id);
            List<UawVipMessage> list = uawVipMessageService.lambdaQuery(uawVipMessage).list();
            if (list.size() != 0) {
                return handleFail("该会员类型正在使用中不能修改状态");
            } else {
                UawVipType uawVipType = uawVipTypeService.getById(id);
                uawVipType.setRevision(0);
                if (uawVipType.getVipTypeStatus().equals(SgsConstant.TypeStatus.FORBIDDEN.getValue())) {
                    uawVipType.setVipTypeStatus(SgsConstant.TypeStatus.STARTUSING.getValue());
                }else {
                    uawVipType.setVipTypeStatus(SgsConstant.TypeStatus.FORBIDDEN.getValue());
                }
                uawVipType.setUpdatedTime(LocalDateTime.now());
                uawVipTypeService.updateById(uawVipType);
                return handleOK();
            }
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }
    @GetMapping("findAll")
   // @ApiOperation(httpMethod = "GET", value = "查询全部会员类型")
    public RespResult findAll(){
        List<UawVipType> list = uawVipTypeService.findVipAll();
        return handleResult(list);
    }
    @GetMapping("findbycode")
    //@ApiOperation(httpMethod = "GET", value = "根据工作台标识获取对应会员类型")
    public RespResult findBycode(@RequestParam String code,@RequestParam Long memberId){
        if(StringUtils.isEmpty(code)){
            throw new AeotradeException("工作台标识不能为空");
        }
        UawVipType uawVipType=new UawVipType();
        uawVipType.setCode(code);
        List<UawVipType> list = uawVipTypeService.lambdaQuery(uawVipType).list();
        UawVipTypeGroup uawVipTypeGroup = uawVipTypeGroupService.getById(list.get(0).getGroupId());
        List<UawVipTypeDto> uawVipTypeDtos=new ArrayList<>();
        for (UawVipType vipType : list) {
            UawVipTypeDto uawVipTypeDto=new UawVipTypeDto();
            BeanUtils.copyProperties(vipType,uawVipTypeDto);
            uawVipTypeDtos.add(uawVipTypeDto);
        }
        if(uawVipTypeGroup!=null){
            uawVipTypeDtos.get(0).setGroupName(uawVipTypeGroup.getGroupName());
        }
        if(memberId!=null){
            Integer status = uawAptitudesService.findStatus(memberId, list.get(0).getId());
            if(status!=0){
                uawVipTypeDtos.get(0).setStatus(status);
            }
        }
        List<UawVipTypeVO> uawVipTypeVOS=new ArrayList<>();
        UawVipTypeVO uawVipTypeVO=new UawVipTypeVO();
        BeanUtils.copyProperties(uawVipTypeDtos.get(0),uawVipTypeVO);
        uawVipTypeVO.setUawWorkbench(uawWorkbenchService.getById(list.get(0).getWorkbench()));
        uawVipTypeVOS.add(uawVipTypeVO);
        return handleResult(Optional.ofNullable(uawVipTypeVOS).orElseGet(() -> new ArrayList<>()));
    }

    @GetMapping("find/staff")
    //@ApiOperation(httpMethod = "GET", value = "根据用户id获取用户当前工作台标识")
    public RespResult findBystaff(@RequestParam Long staffId){
        if(StringUtils.isEmpty(String.valueOf(staffId))){
            throw new AeotradeException("用户id不能为空");
        }
        return handleResult(uawVipTypeService.findBystaff(staffId));
    }

    @GetMapping("find/mam")
    //@ApiOperation(httpMethod = "GET", value = "查询慧贸OS会员")
    public RespResult findMam(){
        List<UawVipType> list = uawVipTypeService.findVipMam();
        return handleResult(list);
    }

}
