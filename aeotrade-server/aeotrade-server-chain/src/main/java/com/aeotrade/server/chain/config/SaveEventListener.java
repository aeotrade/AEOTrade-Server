package com.aeotrade.server.chain.config;

import com.aeotrade.chainmaker.config.AutoIncKey;
import com.aeotrade.utlis.SnowflakeIdWorker;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * @Author yewei
 * @Date 2022/6/1 09:13
 * @Description:
 * @Version 1.0
 */
//@Component泛指组件，把SaveEventListener 加入容器
@Component
public class SaveEventListener extends AbstractMongoEventListener<Object> {



    @Override
    public void onBeforeConvert(BeforeConvertEvent<Object> event) {
        final Object source = event.getSource();
        if (source != null) {
            ReflectionUtils.doWithFields(source.getClass(), new ReflectionUtils.FieldCallback() {
                @Override
                public void doWith(Field field) throws IllegalArgumentException,
                        IllegalAccessException {
                    //将一个字段设置为可读写，主要针对private字段；
                    ReflectionUtils.makeAccessible(field);
                    // 如果字段添加了我们自定义的AutoValue注解
                    if (field.isAnnotationPresent(AutoIncKey.class)
                            &&( field.get(source)==null || field.get(source) .equals(0L))) {
                        // 设置自增ID
                        field.set(source, getNextAutoId(source.getClass().getSimpleName()));
                    }
                }
            });
        }
    }

    // 获取下一个自增ID
    private Long getNextAutoId(String collName) {
        SnowflakeIdWorker idWorker = new SnowflakeIdWorker(1, 1);
        //根据bizKey调用分布式ID生成
        long id =idWorker.nextId();
        return id;
    }
}