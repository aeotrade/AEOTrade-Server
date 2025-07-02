package com.aeotrade.provider.admin.service.impl;



import com.aeotrade.provider.admin.entiy.UacMemberTag;
import com.aeotrade.provider.admin.mapper.UacMemberTagMapper;
import com.aeotrade.provider.admin.service.UacMemberTagService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 企业特点 服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-02
 */
@Service
public class UacMemberTagServiceImpl extends ServiceImpl<UacMemberTagMapper, UacMemberTag> implements UacMemberTagService {


    /**
     * 根据主键 [id] 获取一条记录(企业特点)
     * @param id 主键 ID
     * @return 返回主键对应的对象
     */
    public UacMemberTag get(Long id) {
        return this.getById(id);
    }
}
