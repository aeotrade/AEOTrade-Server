package com.aeotrade.provider.admin.service;


import com.aeotrade.provider.admin.entiy.ClientVersion;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author aeo
 * @since 2023-11-10
 */
public interface ClientVersionService extends IService<ClientVersion> {

    ClientVersion findByNumber(String number);
    ClientVersion findByTypeAndNumber(String appType,String number);

    int insert(ClientVersion clientVersion);

    int updateClientVersion(ClientVersion clientVersion);

    ClientVersion findNewList(String appType,String number);

    ClientVersion findNew(String appType);

    Page<ClientVersion> findListByPage(String appType,Integer pageSize, Integer pageNo);
}
