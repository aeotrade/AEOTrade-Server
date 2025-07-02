package com.aeotrade.provider.mamber.service;

import com.aeotrade.provider.mamber.entity.PmsAppMember;
import com.aeotrade.suppot.PageList;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Auther: 吴浩
 * @Date: 2023-11-14 11:52
 */
public interface PmsAppMemberService extends IService<PmsAppMember> {
    List<PmsAppMember> findByMemberId(Long memberId);
}
