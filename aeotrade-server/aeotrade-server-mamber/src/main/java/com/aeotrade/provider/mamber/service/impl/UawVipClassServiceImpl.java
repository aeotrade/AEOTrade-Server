package com.aeotrade.provider.mamber.service.impl;


import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.mamber.utils.KeyUilt;
import com.aeotrade.provider.mamber.entity.*;
import com.aeotrade.provider.mamber.mapper.*;
import com.aeotrade.provider.mamber.service.UawVipClassService;
import com.aeotrade.provider.mamber.vo.RightsTypeVo;
import com.aeotrade.provider.mamber.vo.VipClass;
import com.aeotrade.provider.mamber.vo.VipClassVos;
import com.aeotrade.provider.mamber.vo.VipTypeVo;
import com.aeotrade.utlis.CommonUtil;
import com.aeotrade.utlis.ThreadPoolUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 会员等级表 服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-07
 */
@Service
public class UawVipClassServiceImpl extends ServiceImpl<UawVipClassMapper, UawVipClass> implements UawVipClassService {
    @Autowired
    private UawRightsTypeServiceImpl uawRightsTypeService;
    @Autowired
    private UawVipMessageServiceImpl uawVipMessageMapper;
    @Autowired
    private UawVipTypeServiceImpl uawVipTypeMapper;
    @Autowired
    private UawRightsRightsTypeServiceImpl uawVipRigtypeService;
    @Autowired
    private UawVipTypeGroupServiceImpl uawVipTypeGroupMapper;
    @Autowired
    private UawRightsRightsTypeServiceImpl uawRightsRightsTypeMapper;
    @Autowired
    private UawVipRightsTypeServiceImpl uawVipRightsTypeMapper;
    @Autowired
    private UawVipClassMenuServiceImpl uawVipClassMenuMapper;

    public List<VipClassVos> findBytid(Long id, int status) {
        UawVipClass uawVipClass = new UawVipClass();
        uawVipClass.setTypeId(id);
        if (status == 1) {
            uawVipClass.setIsStartUsing(1);
        }
        List<UawVipClass> list = this.lambdaQuery(uawVipClass).list();
        List<VipClassVos> voList = new ArrayList<>();
        for (UawVipClass vipClass : list) {
            VipClassVos tyClassVos = new VipClassVos();
            List<RightsTypeVo> classid = uawRightsTypeService.findByClassid(vipClass.getId());
            tyClassVos.setUawVipClass(vipClass);
            tyClassVos.setRightsTypeVos(classid);
            UawVipClassMenu vipClassMenu=new UawVipClassMenu();
            vipClassMenu.setClassId(vipClass.getId());
            List<UawVipClassMenu> ClassMenulist = uawVipClassMenuMapper.lambdaQuery(vipClassMenu).list();
            List<Long> MenuidList = ClassMenulist.stream().map(UawVipClassMenu::getMenuId).collect(Collectors.toList());
            tyClassVos.setMenuId(StringUtils.join(MenuidList.toArray(),","));
            voList.add(tyClassVos);
        }
        return voList;
    }

    public void deleteClass(Long id) {
        this.removeById(id);
    }

    public Map<String, Object> findBybuy(Long id, Long userId, int apply) {
        List<VipClassVos> all = findBytid(id, 1);
        UawVipMessage uawVipMessage = new UawVipMessage();
        uawVipMessage.setTypeId(id);
        uawVipMessage.setUserType(apply);
        if (apply == 0) {
            uawVipMessage.setStaffId(userId);
        } else if (apply == 1) {
            uawVipMessage.setMemberId(userId);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("all", all);
        UawVipType uawVipType = uawVipTypeMapper.getById(id);
        map.put("rightsIco", uawVipType.getRightsIco());
        map.put("current", StringUtils.EMPTY);
        map.put("endTime", StringUtils.EMPTY);
        List<UawVipMessage> list = uawVipMessageMapper.lambdaQuery(uawVipMessage).list();
        UawVipMessage byEntity = !list.isEmpty() ?list.get(0):null;
        if (byEntity != null && byEntity.getVipStatus() != 0) {
            map.put("endTime", byEntity.getEndTime());
            String ClassSerialNumber = byEntity.getClassSerialNumber();
            UawVipClass uawVipClass = new UawVipClass();
            uawVipClass.setClassSerialNumber(ClassSerialNumber);
            List<UawVipClass> vipClasses = this.lambdaQuery(uawVipClass).list();
            UawVipClass vipClass = !vipClasses.isEmpty() ?vipClasses.get(0):null;
            if (vipClass != null) {
                map.put("current", Optional.ofNullable(vipClass.getClassName()).orElse(StringUtils.EMPTY));
            }
        }
        return map;
    }

    public int insertVipClass(VipTypeVo vipTypeVo) {
        //取出会员类型对象
        UawVipType uawVipType = vipTypeVo.getUawVipType();
        uawVipType.setStatus(0);
        if (uawVipType.getIsDefaultVip() == 1) {
            UawVipTypeGroup group = uawVipTypeGroupMapper.getById(uawVipType.getGroupId());
            if (group.getIsDefaultVip() == 0) {
                throw new AeotradeException("该会员类型分组不是默认类型分组请重新添加或修改");
            } else {
                UawVipType type = new UawVipType();
                type.setIsDefaultVip(1);
                type.setGroupId(uawVipType.getGroupId());
                List<UawVipType> list = uawVipTypeMapper.lambdaQuery(type).list();
                if (list.size() == 1 && !list.get(0).getId().equals(vipTypeVo.getUawVipType().getId())) {
                    throw new AeotradeException("该分组已有默认会员类型，请删除修改上一个会员类型或修改当前会员类型");
                }
            }
            //是否需要入驻审核0否1是
            uawVipType.setIsAuditRequired(0);
            //执行会员类型添加操作
            uawVipType.setVipTypeStatus(0);
            uawVipType.setShopTypeName(uawVipType.getTypeName() + "店铺");
            uawVipTypeMapper.saveOrUpdate(uawVipType);
        } else {
            uawVipType.setIsAuditRequired(1);
            //执行会员类型添加操作
            uawVipType.setVipTypeStatus(0);
            uawVipType.setShopTypeName(uawVipType.getTypeName() + "店铺");
            uawVipTypeMapper.saveOrUpdate(uawVipType);
        }
        //取出会员等级权益集合
        List<VipClassVos> uawVipClassVos = vipTypeVo.getUawVipClassVos();
        //对会员等级权益集合进行遍历
        for (int i = 0; i < uawVipClassVos.size(); i++) {
            //取出单个会员等级权益集合
            VipClassVos vipClassVos = uawVipClassVos.get(i);
            //取出会员等级对象
            UawVipClass vipClass = vipClassVos.getUawVipClass();
            //将得到的会员类型id添加到会员等级中
            vipClass.setTypeId(uawVipType.getId());
            vipClass.setSort(i + 1);
            vipClass.setPriceUnit("￥");
            vipClass.setCreatedBy(686L);
            if (vipClass.getClassSerialNumber() == null) {
                vipClass.setClassSerialNumber(KeyUilt.generateUniqueKey());
            }
            //执行会员等级添加操作
            this.saveOrUpdate(vipClass);
            //判断该集合是否为默认会员
            if (!CommonUtil.isEmpty(vipClassVos.getNot()) && vipClassVos.getNot() == 1) {
                //将添加后的会员等级id添加到会员类型
                uawVipType.setDefaultVipClassId(vipClass.getId());
            }
            ThreadPoolUtils.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        insertMenu(vipClass.getId(),vipClassVos.getMenuId());
                    } catch (Exception e) {
                        log.warn(e.getMessage());
                    }
                }
            });
            //将会员等级id，权益集合传到权益类型service
            Boolean aBoolean = uawVipRigtypeService.insertVip(vipClass.getId(), vipClassVos.getRightsVoList());
            if (!aBoolean) {
                //回滚
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                throw new AeotradeException("权益添加修改失败");
            }
        }
        uawVipType.setRevision(0);
        uawVipType.setVipTypeStatus(0);
        uawVipType.setShopTypeName(uawVipType.getTypeName() + "店铺");
        boolean i = uawVipTypeMapper.updateById(uawVipType);
        return i?1:0;
    }
    public void insertMenu(Long classId,String menuIds){
        UawVipClassMenu vipClassMenu=new UawVipClassMenu();
        vipClassMenu.setClassId(classId);
        List<UawVipClassMenu> list = uawVipClassMenuMapper.lambdaQuery(vipClassMenu).list();
        for (UawVipClassMenu uawVipClassMenu : list) {
            uawVipClassMenuMapper.removeById(uawVipClassMenu);
        }
        for (String s : menuIds.split(",")) {
            UawVipClassMenu uawVipClassMenu=new UawVipClassMenu();
            uawVipClassMenu.setClassId(classId);
            uawVipClassMenu.setMenuId(Long.valueOf(s));
            uawVipClassMenuMapper.saveOrUpdate(uawVipClassMenu);
        }
    }
    public Map<String, String> findtype(VipClass vipClass) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        Map<String, String> map = new HashMap<>();
        map.put("discount", StringUtils.EMPTY);
        map.put("type", StringUtils.EMPTY);
        map.put("totalAmount", StringUtils.EMPTY);
        map.put("actualPaymet", StringUtils.EMPTY);
        map.put("periodOfValidity", StringUtils.EMPTY);


        UawVipMessage uawVipMessage = new UawVipMessage();
        uawVipMessage.setMemberId(vipClass.getMemberId());
        uawVipMessage.setTypeId(vipClass.getTypeId());
        List<UawVipMessage> list = uawVipMessageMapper.lambdaQuery(uawVipMessage).list();
        if (!list.isEmpty()) {
            UawVipClass VipClass = new UawVipClass();
            VipClass.setClassSerialNumber(list.get(0).getClassSerialNumber());
            //用户当前使用的会员等级
            List<UawVipClass> uawVipClasses = this.lambdaQuery(VipClass).list();
            UawVipClass uawVipClass = !uawVipClasses.isEmpty() ?uawVipClasses.get(0):null;
            if (uawVipClass == null) {
                throw new AeotradeException("没有此会员等级");
            }

            if (uawVipClass.getId().equals(vipClass.getClassId())) {
                map.put("totalAmount", decimalFormat.format(uawVipClass.getPrice()));
                map.put("actualPaymet", decimalFormat.format(uawVipClass.getPrice()));
                map.put("periodOfValidity", uawVipClass.getTerm() + uawVipClass.getTermUnit());
                map.put("discount", "￥0.00");
                map.put("type", "续费");
            } else {
                map.put("type", "升级");
                UawVipClass aClass = this.getById(vipClass.getClassId());
                if (null == list.get(0).getEndTime()) {
//                    throw new AeotradeException("有效期永久会员不能升级");
                    map.put("totalAmount", decimalFormat.format(aClass.getPrice()));
                    map.put("actualPaymet",  decimalFormat.format(aClass.getPrice()));
                    map.put("periodOfValidity", aClass.getTerm() + aClass.getTermUnit());
                    map.put("discount", "￥0.00");
                }else{
                    LocalDateTime endTime = list.get(0).getEndTime();
                    String one = df.format(endTime);
                    String two = df.format(new Date());
                    Long aLong = daysBetween(one, two);

                    //当前会等级天数
                    int nowDay = this.numberDay(uawVipClass.getTerm(), uawVipClass.getTermUnit());
                    //要购买会员等级天数
                    int upgradeDay = this.numberDay(aClass.getTerm(), aClass.getTermUnit());
                    //折扣价格
                    Double aDouble = (double) (uawVipClass.getPrice() / nowDay * aLong);
                    if (aLong > 365) {
                        //购买后等级
                        Double bDouble1 = (double) (aClass.getPrice() / upgradeDay * aLong);
                        map.put("totalAmount", decimalFormat.format(bDouble1));
                        map.put("actualPaymet", decimalFormat.format(bDouble1 - aDouble));
                        map.put("periodOfValidity", aLong + "天");
                        map.put("discount", "￥" + decimalFormat.format(aDouble));
                    } else {
                        map.put("totalAmount", decimalFormat.format(aClass.getPrice()));
                        map.put("actualPaymet", decimalFormat.format(aClass.getPrice() - aDouble));
                        map.put("periodOfValidity", aClass.getTerm() + aClass.getTermUnit());
                        map.put("discount", "￥" + decimalFormat.format(aDouble));
                    }
                }
            }
        }
        return map;
    }

    private Long daysBetween(String one, String two) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date parse = df.parse(one);
        Date parsetwo = df.parse(two);
        long difference = (parse.getTime() - parsetwo.getTime()) / 86400000;
        return Math.abs(difference);
    }

    private int numberDay(int term, String termunit) {
        int day = 0;
        if ("年".equals(termunit)) {
            day = term * 365;
        } else if ("月".equals(termunit)) {
            day = term * 30;
        } else if ("日".equals(termunit)) {
            day = term;
        } else {
            day = term;
        }
        return day;
    }

    public void delete(UawVipClass vipClass) {
        this.removeById(vipClass.getId());
        UawRightsRightsType uawRightsRightsType = new UawRightsRightsType();
        uawRightsRightsType.setVipClassId(vipClass.getId());
        List<UawRightsRightsType> list = uawVipRigtypeService.lambdaQuery(uawRightsRightsType).list();
        for (UawRightsRightsType rightsRightsType : list) {
            uawRightsRightsTypeMapper.removeById(rightsRightsType.getId());
        }
        UawVipRightsType uawVipRightsType = new UawVipRightsType();
        uawVipRightsType.setVipClassId(vipClass.getId());
        List<UawVipRightsType> vipRightsTypeMapperList = uawVipRightsTypeMapper.lambdaQuery(uawVipRightsType).list();
        for (UawVipRightsType vipRightsType : vipRightsTypeMapperList) {
            uawVipRightsTypeMapper.removeById(vipRightsType.getId());
        }
        UawVipClassMenu vipClassMenu=new UawVipClassMenu();
        vipClassMenu.setClassId(vipClass.getId());
        List<UawVipClassMenu> ClassMenulist = uawVipClassMenuMapper.lambdaQuery(vipClassMenu).list();
        for (UawVipClassMenu menulist : ClassMenulist) {
            uawVipClassMenuMapper.removeById(menulist);
        }
    }
}
