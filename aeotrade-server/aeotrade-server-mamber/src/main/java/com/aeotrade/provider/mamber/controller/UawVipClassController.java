package com.aeotrade.provider.mamber.controller;


import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.mamber.entity.UawVipClass;
import com.aeotrade.provider.mamber.entity.UawVipType;
import com.aeotrade.provider.mamber.service.impl.UawRightsRightsTypeServiceImpl;
import com.aeotrade.provider.mamber.service.impl.UawVipClassServiceImpl;
import com.aeotrade.provider.mamber.service.impl.UawVipTypeServiceImpl;
import com.aeotrade.provider.mamber.vo.RightsVo;
import com.aeotrade.provider.mamber.vo.VipClass;
import com.aeotrade.provider.mamber.vo.VipClassVos;
import com.aeotrade.provider.mamber.vo.VipTypeVo;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.RespResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * UawVipClassController 会员等级表 Controller
 *
 * @author admin
 */
@RestController
@RequestMapping("/uaw/VipClass/")
@Slf4j
public class UawVipClassController extends BaseController {

    @Autowired
    private UawVipClassServiceImpl uawVipClassService;
    @Autowired
    private UawVipTypeServiceImpl uawVipTypeService;
    @Autowired
    private UawRightsRightsTypeServiceImpl uawVipRigtypeService;

    /**
     * 运营端添加的会员分类和等级
     *
     * @param
     * @return
     */
    @PostMapping("insert")
    //@ApiOperation(httpMethod = "POST", value = "运营端添加的会员分类和等级")
    @Transactional(propagation = Propagation.REQUIRED)
    public RespResult insertVipClass(@RequestBody VipTypeVo vipTypeVo) {
        try {
            if (vipTypeVo == null) {
                throw new AeotradeException("会员类型会员等级不能为空");
            }
            int i = uawVipClassService.insertVipClass(vipTypeVo);
            if (i == 1) {
                return handleOK();
            }
            return handleFail("添加修改失败");
        } catch (Exception e) {
            log.warn(e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return handleFail(e);
        }
    }

    /**
     * 前端购买会员套餐
     * 根据会员类型id
     * 展示对应的会员等级、权益类型、具体权益项
     *
     * @param
     * @return
     */
    @GetMapping("findBytid")
    //@ApiOperation(httpMethod = "GET", value = "前端购买会员套餐类型id展示对应的会员等级、权益类型、具体权益项")
    //@ApiImplicitParam(name = "id", value = "会员类型id", required = true)
    public RespResult findBytid(Long id, Long userId, int apply) {
        try {
            if (id == null) {
                throw new AeotradeException("会员类型id不能为空");
            }
            if (userId == null) {
                throw new AeotradeException("用户id不能为空");
            }
            if (apply != 0 && apply != 1) {
                throw new AeotradeException("用户类型错误");
            }
            return handleResult(uawVipClassService.findBybuy(id, userId, apply));
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }

    /**
     * 运营端根根据会员类型id展示对应的会员等级、权益类型、具体权益项
     *
     * @param
     * @return
     */
    @GetMapping("findBytypeid")
    //@ApiOperation(httpMethod = "GET", value = "运营端根根据会员类型id展示对应的会员等级、权益类型、具体权益项")
    // @ApiImplicitParam(name = "id", value = "会员类型id", required = true)
    public RespResult findBytid(Long id) {
        try {
            if (id == null) {
                throw new AeotradeException("会员类型id不能为空");
            }
            return handleResult(uawVipClassService.findBytid(id, 0));
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }

    /**
     * 运营端修改的会员分类和等级
     *
     * @param
     * @return
     */
    @PostMapping("update")
    //@ApiOperation(httpMethod = "POST", value = "运营端修改的会员分类和等级")
    @Transactional(propagation = Propagation.REQUIRED)
    public RespResult updateVipClass(@RequestBody VipTypeVo vipTypeVo) {
        UawVipType uawVipType = vipTypeVo.getUawVipType();
        if (uawVipType.getId() == null) {
            throw new AeotradeException("类型id为空");
        }
        //删除会员类型
        uawVipTypeService.deleteType(uawVipType.getId());
        //取出会员等级
        List<VipClassVos> uawVipClassVos = vipTypeVo.getUawVipClassVos();
        //循环遍历会员等级权益数据集合
        for (VipClassVos uawVipClassVo : uawVipClassVos) {
            //取出单个会员等级
            UawVipClass uawVipClass = uawVipClassVo.getUawVipClass();
            if (uawVipClass.getId() != null) {
                uawVipClassService.deleteClass(uawVipClass.getId());
                //取出权益集合数据
                List<RightsVo> rightsVoList = uawVipClassVo.getRightsVoList();
                uawVipRigtypeService.updateVip(uawVipClass.getId(), rightsVoList);
            }
        }
        return insertVipClass(vipTypeVo);
    }

    /**
     * 开通会员获取订单类型（续费or升级）
     * 计算折扣价格
     *
     * @param
     * @return
     */
    @PostMapping("findtype")
    // @ApiOperation(httpMethod = "POST", value = "开通会员获取订单类型（续费or升级,计算折扣价格")
    public RespResult findtype(@RequestBody VipClass vipClass) {

        if (vipClass == null) {
            throw new AeotradeException("传值为空");
        }
        try {
            Map<String, String> findtype = uawVipClassService.findtype(vipClass);
            return handleResult(findtype);
        } catch (ParseException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }

}
