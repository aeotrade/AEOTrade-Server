package com.aeotrade.provider.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 企业同步体检智囊日志记录表
 * </p>
 *
 * @author aeo
 * @since 2023-11-03
 */
@Getter
@Setter
@TableName("uac_member_synchronization")
public class UacMemberSynchronization {

    private Long id;

    /**
     * 同步企业id
     */
    private Long memberId;

    /**
     * 同步企业名
     */
    private String memberNaem;

    /**
     * 同步企业uscc
     */
    private String memberUscc;

    /**
     * 同步状态0失败1成功
     */
    private Integer synchronousStatus;

    /**
     * 同步结果
     */
    private String returnResult;
}
