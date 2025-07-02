package com.aeotrade.provider.mamber.vo;

import com.aeotrade.provider.mamber.entity.UawWorkbenchMenu;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Auther: 吴浩
 * @Date: 2023-11-20 14:41
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UawWorkbenchMenuVo extends UawWorkbenchMenu {
    private MenuMetaDto meta;
}
