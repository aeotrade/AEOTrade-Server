package com.aeotrade.provider.mamber.utils;
import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.mamber.entity.UawVipMessage;
import com.aeotrade.provider.mamber.service.impl.UawVipMessageServiceImpl;
import com.aeotrade.provider.mamber.service.impl.UawVipTypeServiceImpl;
import com.aeotrade.utlis.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class EndTimeUlit {
    @Autowired
    private UawVipMessageServiceImpl uawVipMessageService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void task1() {
        try {
            System.out.println("开始执行结束时间审核");
            List<UawVipMessage> list = uawVipMessageService.findUawVipMessage();
            if(!CommonUtil.isEmpty(list)){
            for (UawVipMessage uawVipMessage : list) {
                LocalDateTime endTime = uawVipMessage.getEndTime();
                if (endTime.isBefore(LocalDateTime.now())) {
                    uawVipMessage.setVipStatus(0);
                    uawVipMessageService.updateById(uawVipMessage);
                }
            }
        }
            System.out.println("执行结束，等待下一轮");
        } catch (Exception e) {
            throw new AeotradeException(e.getMessage());
        }
    }

}
    
