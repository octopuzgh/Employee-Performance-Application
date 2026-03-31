package com.octopuz.platform.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("开始插入填充...");
        //Employee的createdAt字段
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime::now, LocalDateTime.class);
        //Employee的updatedAt字段
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime::now, LocalDateTime.class);
        //Performance的createTime字段和OperationLog的createTime字段
        this.strictInsertFill(metaObject, "createTime", LocalDateTime::now, LocalDateTime.class);

    }
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("开始插入填充...");
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime::now, LocalDateTime.class);
    }

}
