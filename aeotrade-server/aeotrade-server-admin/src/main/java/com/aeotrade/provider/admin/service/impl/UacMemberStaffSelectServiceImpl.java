package com.aeotrade.provider.admin.service.impl;



import com.aeotrade.provider.admin.entiy.UacMemberStaffSelect;
import com.aeotrade.provider.admin.mapper.UacMemberStaffSelectMapper;
import com.aeotrade.provider.admin.service.UacMemberStaffSelectService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-02
 */
@Service
public class UacMemberStaffSelectServiceImpl extends ServiceImpl<UacMemberStaffSelectMapper, UacMemberStaffSelect> implements UacMemberStaffSelectService {


    public int findStatus(Long staffId, Long memberId, String type) {
        List<UacMemberStaffSelect> list =this.lambdaQuery()
                .eq(UacMemberStaffSelect::getStaffId,staffId)
                .eq(UacMemberStaffSelect::getMemberId,memberId).list();
        if(null==list || list.size()==0){
            UacMemberStaffSelect uacMemberStaffSelect=new UacMemberStaffSelect();
            uacMemberStaffSelect.setStaffId(staffId);
            uacMemberStaffSelect.setMemberId(memberId);
            uacMemberStaffSelect.setCreatedTime(LocalDateTime.now());
            Map<String,Integer> map=new HashMap<>();
            map.put("web",0);
            map.put("client",0);
            map.put("webVideo",0);
            map.put("clientVideo",0);
            map.put("webNew",0);
            map.put("clientNew",0);
            uacMemberStaffSelect.setIsSelect(JSON.toJSONString(map));
            this.save(uacMemberStaffSelect);
            return 0;
        }else{
            int i=0;
            for (UacMemberStaffSelect uacMemberStaffSelect : list) {
                Map map = JSON.parseObject(uacMemberStaffSelect.getIsSelect(), Map.class);
                if(null!=map.get(type)){
                    i=i+(int)map.get(type);
                    break;
                }
            }
            return i;
        }
    }

    public void updateStatus(Long staffId, Long memberId, String type,Integer memberStatus) {
        List<UacMemberStaffSelect> list =this.lambdaQuery()
                .eq(UacMemberStaffSelect::getStaffId,staffId)
                .eq(UacMemberStaffSelect::getMemberId,memberId).list();
        if(null!=list && list.size()!=0){
            for (UacMemberStaffSelect uacMemberStaffSelect : list) {
                Map map = JSON.parseObject(uacMemberStaffSelect.getIsSelect(), Map.class);
                map.put(type,memberStatus);
                uacMemberStaffSelect.setIsSelect(JSON.toJSONString(map));
                this.updateById(uacMemberStaffSelect);
            }
        }
    }
}
