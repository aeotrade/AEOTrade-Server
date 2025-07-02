package com.aeotrade.provider.admin.adminVo;
import com.aeotrade.provider.admin.entiy.UawWorkbenchMenu;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 后台菜单节点封装
 *
 */
@Getter
@Setter
public class UacMenuDto extends UawWorkbenchMenu {
    //@ApiModelProperty(value = "子级菜单")
    private List<UawWorkbenchMenu> children;
}
