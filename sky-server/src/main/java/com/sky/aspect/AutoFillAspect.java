package com.sky.aspect;


import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    /**
     * 定义切入点
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){}

    @Before("autoFillPointCut()")
    public void autFill(JoinPoint joinPoint){
        log.info("公共字段自动填充");
        //获取到当前被拦截的方法上的数据库操作类型
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = methodSignature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();
        //获取到当前被拦截方法的参数--实体对象
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0){
            return;
        }
        Object entity = args[0];
        //准备赋值(当前修改人id 当前修改时间)
        Long currentId = BaseContext.getCurrentId();
        LocalDateTime now = LocalDateTime.now();
        //根据对应的数据库操作类型，为对应的属性赋值
        if(operationType == OperationType.INSERT){
            //为四个公共字段赋值
            try {
                Method setCreateTime = entity.getClass().getMethod(AutoFillConstant.SET_CREATE_TIME,LocalDateTime.class);
                Method updateTime = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
                Method setCreateUser = entity.getClass().getMethod(AutoFillConstant.SET_CREATE_USER,Long.class);
                Method updateUser =  entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);
                //通过反射为对象赋值
                setCreateTime.invoke(entity,now);
                updateTime.invoke(entity,now);
                setCreateUser.invoke(entity,currentId);
                updateUser.invoke(entity,currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            //为两个公共字段赋值
            try {
                Method updateTime = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
                Method updateUser =  entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);
                //通过反射为对象赋值
                updateTime.invoke(entity,now);
                updateUser.invoke(entity,currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }
}
