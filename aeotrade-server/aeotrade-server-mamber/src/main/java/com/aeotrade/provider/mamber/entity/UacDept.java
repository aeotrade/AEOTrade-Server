package com.aeotrade.provider.mamber.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Auther: 吴浩
 * @Date: 2023-11-30 10:40
 */
@Getter
@Setter
@TableName("uac_dept")
public class UacDept {

    private Long id;

    private String deptName;

    private String deptDescription;

    private Long deptCount;

    private Long parentId;

    private Long memberId;

    private LocalDateTime createdTime;

    private LocalDateTime updateTime;

    @TableField(exist = false)
    private List<UacDept> children;
}
