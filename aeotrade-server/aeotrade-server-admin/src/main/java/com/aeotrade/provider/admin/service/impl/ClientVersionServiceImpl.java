package com.aeotrade.provider.admin.service.impl;


import com.aeotrade.provider.admin.entiy.ClientVersion;
import com.aeotrade.provider.admin.mapper.ClientVersionMapper;
import com.aeotrade.provider.admin.service.ClientVersionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-10
 */
@Service
public class ClientVersionServiceImpl extends ServiceImpl<ClientVersionMapper, ClientVersion> implements ClientVersionService {

    @Override
    public ClientVersion findByNumber(String number) {
        List<ClientVersion> list = this.lambdaQuery().eq(ClientVersion::getVersionNumber, number).list();
        return list.size()>0?list.get(0):null;
    }

    @Override
    public ClientVersion findByTypeAndNumber(String appType,String number) {
        List<ClientVersion> list = this.lambdaQuery()
                .eq(StringUtils.isNotBlank(appType),ClientVersion::getAppType,appType)
                .isNull(StringUtils.isBlank(appType),ClientVersion::getAppType)
                .eq(ClientVersion::getVersionNumber, number).list();
        return list.size()>0?list.get(0):null;
    }

    @Override
    public int insert(ClientVersion clientVersion) {
        List<ClientVersion> list = this.lambdaQuery()
                .eq(StringUtils.isNotBlank(clientVersion.getAppType()),ClientVersion::getAppType,clientVersion.getAppType())
                .isNull(StringUtils.isBlank(clientVersion.getAppType()),ClientVersion::getAppType)
                .eq(ClientVersion::getVersionNumber, clientVersion).list();
        ClientVersion newList = list.size()>0?list.get(0):null;
        if (null != newList) {
            return 3;
        }
        this.save(clientVersion);
        return 1;
    }

    @Override
    public int updateClientVersion(ClientVersion clientVersion) {
        if(clientVersion.getStatus()==1){
            this.updateById(clientVersion);
            return 1;
        }else{
            List<ClientVersion> list = this.lambdaQuery().eq(ClientVersion::getVersionNumber, clientVersion).list();
            ClientVersion newList = list.size()>0?list.get(0):null;
            if (null != newList && !newList.getId().equals(clientVersion.getId())) {
                return 3;
            }
            this.updateById(clientVersion);
            return 1;
        }
    }

    /**
     * 获取最新版本
     * 逻辑备注：
     *      如果有全量优先更新全量，更新完全量再更新增量
     * @param appType 终端类型
     * @param number 版本号
     * @return
     */
    @Override
    public ClientVersion findNewList(String appType,String number) {
//        //用户当前版本
//        ClientVersion newList = clientVersionMapper.findNewList(number);
        List<ClientVersion> list = this.lambdaQuery()
                .eq(StringUtils.isNotBlank(appType),ClientVersion::getAppType,appType)
                .isNull(StringUtils.isBlank(appType),ClientVersion::getAppType)
                .eq(ClientVersion::getVersionNumber, number)
                .eq(ClientVersion::getStatus, 0)
                .orderByDesc(ClientVersion::getCreatTime).list();
        ClientVersion newList =list.size()>0?list.get(0):null;

        //最新的全量更新版本
        List<ClientVersion> versions = this.lambdaQuery()
                .eq(StringUtils.isNotBlank(appType),ClientVersion::getAppType,appType)
                .isNull(StringUtils.isBlank(appType),ClientVersion::getAppType)
                .eq(ClientVersion::getUpdateMode,1)
                .eq(ClientVersion::getStatus,0)
                .eq(ClientVersion::getIsPublic,1)
                .orderByDesc(ClientVersion::getCreatTime).list();
        //最新的增量更新版本
        List<ClientVersion> versionList = this.lambdaQuery()
                .eq(StringUtils.isNotBlank(appType),ClientVersion::getAppType,appType)
                .isNull(StringUtils.isBlank(appType),ClientVersion::getAppType)
                .eq(ClientVersion::getUpdateMode,2)
                .eq(ClientVersion::getStatus,0)
                .eq(ClientVersion::getIsPublic,1)
                .orderByDesc(ClientVersion::getCreatTime).list();

        if(versions.size()==0 && versionList.size()==0 && null==newList){
            ClientVersion clientVersion=new ClientVersion();
            clientVersion.setVersionNumber(number);
            return clientVersion;
        }

        if(versions.size()==0 && versionList.size()!=0){
            if(null==newList){
                return versionList.get(0);
            }else{
                if (newList.getIsPublic() == 0){
                    return newList;
                }else {
                    versionList.get(0).setIsUpdate(newList.getIsUpdate());
                    return versionList.get(0);
                }
            }
        }
        if(versions.size()!=0 && versionList.size()==0){
            if(null==newList){
                return versions.get(0);
            }else{
                if (newList.getIsPublic() == 0){
                    return newList;
                }else {
                    versions.get(0).setIsUpdate(newList.getIsUpdate());
                    return versions.get(0);
                }
            }
        }
        if (null == newList) {
            return versions.get(0);
        } else {
            if (newList.getIsPublic() == 0) {
                return newList;
            }else{
                if (versions.get(0).getCreatTime().toInstant(ZoneOffset.of("+8")).toEpochMilli() > versionList.get(0).getCreatTime().toInstant(ZoneOffset.of("+8")).toEpochMilli()) {
                    //全量大于增量
                    versions.get(0).setIsUpdate(newList.getIsUpdate());
                    return versions.get(0);
                } else {
                    //全量小于增量
                    if (newList.getCreatTime().toInstant(ZoneOffset.of("+8")).toEpochMilli() >= versions.get(0).getCreatTime().toInstant(ZoneOffset.of("+8")).toEpochMilli()) {
                        versionList.get(0).setIsUpdate(newList.getIsUpdate());
                        return versionList.get(0);
                    } else {
                        versions.get(0).setIsUpdate(newList.getIsUpdate());
                        return versions.get(0);
                    }
                }
            }
        }
    }

    @Override
    public ClientVersion findNew(String appType) {
        List<ClientVersion> versions = this.lambdaQuery()
                .eq(StringUtils.isNotBlank(appType),ClientVersion::getAppType,appType)
                .isNull(StringUtils.isBlank(appType),ClientVersion::getAppType)
                .eq(ClientVersion::getStatus,0)
                .eq(ClientVersion::getUpdateMode,1)
                .eq(ClientVersion::getIsPublic,1)
                .orderByDesc(ClientVersion::getCreatTime).list();
        if(null!=versions && versions.size()!=0){
            return versions.get(0);
        }
        return null;
    }

    @Override
    public Page<ClientVersion> findListByPage(String appType,Integer pageSize, Integer pageNo) {
        LambdaQueryWrapper<ClientVersion> clientVersionLambdaQueryChainWrapper = new LambdaQueryWrapper<>();
        clientVersionLambdaQueryChainWrapper
        .eq(StringUtils.isNotBlank(appType),ClientVersion::getAppType,appType)
                .isNull(StringUtils.isBlank(appType),ClientVersion::getAppType);
        clientVersionLambdaQueryChainWrapper.eq(ClientVersion::getStatus,0);
        clientVersionLambdaQueryChainWrapper.orderByDesc(ClientVersion::getCreatTime);
        Page<ClientVersion> page = this.page(new Page<>(pageNo, pageSize), clientVersionLambdaQueryChainWrapper);
        return page;
    }
}
