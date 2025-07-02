package com.aeotrade.provider.mamber.vo;


import com.aeotrade.provider.mamber.entity.UawWorkbenchMenu;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkbenchVo extends UawWorkbenchMenuVo implements Comparable{

//    List<WorkbenchVo> children;
    List<WorkbenchVo> button;

    @Override
    public int compareTo(@NotNull Object o) {
          WorkbenchVo w=(WorkbenchVo)(o);
          return this.getSort()-w.getSort();
    }
}
