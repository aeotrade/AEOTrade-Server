package com.aeotrade.provider.service;

import com.aeotrade.provider.model.UacUser;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author aeo
 * @since 2023-11-03
 */
public interface UacUserService extends IService<UacUser> {
    public void bindingMobile(String mobile, Long staffId, String staffname, String url) throws Exception;
    public Integer findByMobile(String mobile);
    public int memberMobile(Long memberId, String phone);
    public void LoginBanding(String mobile, Long staffId, String staffname, String url) throws Exception;
}
