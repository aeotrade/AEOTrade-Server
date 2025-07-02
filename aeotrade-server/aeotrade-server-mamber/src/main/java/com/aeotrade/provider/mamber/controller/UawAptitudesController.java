package com.aeotrade.provider.mamber.controller;
import com.aeotrade.base.constant.SgsConstant;
import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.mamber.entity.UawAptitudes;
import com.aeotrade.provider.mamber.entity.UawVipType;
import com.aeotrade.provider.mamber.feign.AdminFeign;
import com.aeotrade.provider.mamber.service.impl.UawAptitudesServiceImpl;
import com.aeotrade.provider.mamber.service.impl.UawVipMessageServiceImpl;
import com.aeotrade.provider.mamber.service.impl.UawVipTypeServiceImpl;
import com.aeotrade.provider.mamber.vo.UawVipTypeVO;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.PageList;
import com.aeotrade.suppot.RespResult;
import com.aeotrade.utlis.CommonUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * UawAptitudesController 入驻申请接口
 *
 * @author admin
 */
@RestController
@RequestMapping("/aptitude/")
@Slf4j
public class UawAptitudesController extends BaseController {
    @Autowired
    private UawAptitudesServiceImpl uawAptitudesService;
    @Autowired
    private UawVipTypeServiceImpl uawVipTypeService;

    @PostMapping("save")
    //@ApiOperation(httpMethod = "POST", value = "入驻资料提交")
    public RespResult sgsListSave(@RequestBody UawAptitudes uawAptitudes) throws Exception {
        if (uawAptitudes == null) {
            throw new AeotradeException("值不能为空");
        }
        log.info("传进来的" + uawAptitudes);
        if (StringUtils.isNotEmpty(uawAptitudes.getVipTypeName()) && uawAptitudes.getVipTypeId() == 0L) {
            UawVipType uawVipType = new UawVipType();
            uawVipType.setCode(uawAptitudes.getVipTypeName());
            List<UawVipType> list = uawVipTypeService.lambdaQuery(uawVipType).list();
            if (list.size() != 0) {
                uawAptitudes.setVipTypeId(list.get(0).getId());
            }
        }
        if (uawAptitudes.getVipTypeId() == 0L) {
            return handleResult(uawAptitudes);
        }
        log.info("要传走的" + uawAptitudes);
        UawAptitudes uawAptitudes1 = uawAptitudesService.sgsListSave(uawAptitudes);
        log.info("uawAptitudes1" + uawAptitudes1);
        return handleResult(uawAptitudes1);
    }


    @PostMapping("update")
    //@ApiOperation(httpMethod = "POST", value = "通过/不通过(通过sgsStatus传2)")
    public RespResult sgsListupdate(@RequestBody UawAptitudes uawAptitudes) {
        try {
            if (uawAptitudes == null) {
                throw new AeotradeException("type不能为空");
            }
            if (uawAptitudes.getId() == null) {
                throw new AeotradeException("id不能为空");
            }
            uawAptitudesService.sgsListupdate(uawAptitudes);
            return handleResult(Optional.ofNullable(uawAptitudes).orElseGet(UawAptitudes::new));
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    @PostMapping("del")
    //@ApiOperation(httpMethod = "POST", value = "根据Id 删除")
    public RespResult sgsListDelete(@RequestBody UawAptitudes uawAptitudes) {
        try {
            if (uawAptitudes == null) {
                throw new AeotradeException("值不能为空");
            }
            uawAptitudes.setStatus(0);
            uawAptitudesService.updateById(uawAptitudes);
            return handleResult(Optional.ofNullable(uawAptitudes).orElseGet(UawAptitudes::new));
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    @GetMapping("query")
    //@ApiOperation(httpMethod = "GET", value = "根据ID查询详情")
    public RespResult queryId(Long id) {
        try {
            if (id == null) {
                throw new AeotradeException("值不能为空");
            }
            UawAptitudes uawAptitudes = uawAptitudesService.getById(id);
            return handleResult(Optional.ofNullable(uawAptitudes).orElseGet(UawAptitudes::new));
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    @GetMapping("save/member/shop")
    //@ApiOperation(httpMethod = "GET", value = "同步店铺,code传店铺标识")
    public RespResult saveShopMember(Long memberId, String code) throws Exception {
        try {
            if (memberId == null) {
                throw new AeotradeException("企业ID不能为空");
            }
            if (StringUtils.isEmpty(code)) {
                throw new AeotradeException("code不能为空");
            }
            Integer integer = uawAptitudesService.memberShop(memberId, code);
            return handleResult(integer);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 前端购买会员套餐根据会员分组id进行会员类型展示
     *
     * @param
     * @return
     */
    @GetMapping("findBygid")
    //@ApiOperation(httpMethod = "GET", value = "前端购买会员套餐根据会员分组id进行会员类型展示")
    public RespResult findById(Long id, Long memberId) {
        try {
            if (id == null) {
                throw new AeotradeException("会员分组id不能为空");
            }
            List<UawVipType> list = uawVipTypeService.lambdaQuery()
                    .eq(UawVipType::getGroupId,id)
                    .eq(UawVipType::getStatus,0)
                    .eq(UawVipType::getVipTypeStatus,SgsConstant.TypeStatus.STARTUSING.getValue())
                    .orderByAsc(UawVipType::getSort).list();
            if (!CommonUtil.isEmpty(list)) {
                List<UawVipTypeVO> vo = new ArrayList<>();
                list.forEach(vip -> {
                    UawVipTypeVO typeVO = new UawVipTypeVO();
                    BeanUtils.copyProperties(vip, typeVO);
                    if (!CommonUtil.isEmpty(memberId)) {
                        Integer isAptitudes = uawAptitudesService.findStatus(memberId, typeVO.getId());
                        typeVO.setIsAptiudes(isAptitudes);
                    } else {
                        typeVO.setIsAptiudes(0);
                    }
                    typeVO.setUawWorkbench(uawVipTypeService.findListworkbench(typeVO.getWorkbench()));
                    vo.add(typeVO);
                });
                return handleResult(Optional.ofNullable(vo).orElseGet(ArrayList::new));
            }
            return handleResult(Optional.ofNullable(list).orElseGet(ArrayList::new));
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }

    @GetMapping("list")
    //@ApiOperation(httpMethod = "GET", value = "入驻认证列表")
    public RespResult test(@RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                           @RequestParam(name = "pageNo", required = false, defaultValue = "0") Integer pageNo, String memberName,Long vipTypeId) {
        PageList<UawAptitudes> list = uawAptitudesService.findAll(pageSize, pageNo, memberName,vipTypeId);
        return handleResultList(list);
    }


}
