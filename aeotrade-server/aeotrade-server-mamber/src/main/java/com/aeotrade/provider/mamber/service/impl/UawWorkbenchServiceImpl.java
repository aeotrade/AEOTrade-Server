package com.aeotrade.provider.mamber.service.impl;


import com.aeotrade.provider.mamber.entity.UacStaff;
import com.aeotrade.provider.mamber.entity.UawWorkbench;
import com.aeotrade.provider.mamber.mapper.UawWorkbenchMapper;
import com.aeotrade.provider.mamber.service.UacStaffService;
import com.aeotrade.provider.mamber.service.UawWorkbenchService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-07
 */
@Service
public class UawWorkbenchServiceImpl extends ServiceImpl<UawWorkbenchMapper, UawWorkbench> implements UawWorkbenchService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UacStaffService uacStaffService;

    public void updateDefultWorkbench(Long staffId, Long memberId, Long workBenchId) {
        UacStaff staff = uacStaffService.findByid(staffId);
        if(workBenchId!=1){
            UawWorkbench uawWorkbench = this.getById(workBenchId);
            staff.setLastMemberId(memberId);
            staff.setLastWorkbenchId(workBenchId);
            staff.setChannelColumnsId(uawWorkbench.getChannelColumnsId());
        }else{
            staff.setLastMemberId(memberId);
            staff.setLastWorkbenchId(workBenchId);
            staff.setChannelColumnsId(0L);
        }
        uacStaffService.updateStaff(staff);
        if(memberId!=null) {
            Long expire = stringRedisTemplate.getExpire("MEMBER_WORKBENCH:"+staffId + memberId);
            if (expire == -2) {
                stringRedisTemplate.opsForValue().append("MEMBER_WORKBENCH:"+staffId + memberId,String.valueOf(workBenchId));
            } else {
                stringRedisTemplate.delete("MEMBER_WORKBENCH:"+staffId + memberId);
                stringRedisTemplate.opsForValue().append("MEMBER_WORKBENCH:"+staffId + memberId,String.valueOf(workBenchId));
            }
        }
    }
}
