package com.aeotrade.provider.mamber.service.impl;

import com.aeotrade.provider.mamber.entity.PmsAppMember;
import com.aeotrade.provider.mamber.mapper.PmsAppMemberMapper;
import com.aeotrade.provider.mamber.service.PmsAppMemberService;
import com.aeotrade.suppot.PageList;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Auther: 吴浩
 * @Date: 2023-11-14 11:53
 */
@Service
public class PmsAppMemberServiceImpl extends ServiceImpl<PmsAppMemberMapper, PmsAppMember> implements PmsAppMemberService {
    @Override
    public List<PmsAppMember> findByMemberId(Long memberId) {
        return this.lambdaQuery().eq(PmsAppMember::getMemberId,memberId).list();
    }
}
