package org.oneog.uppick.auction.common.config.datasource;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Order(0) // 트랜잭션보다 먼저 실행되어야 함
@Component
public class DataSourceAspect {

    @Around("@annotation(transactional)")
    public Object setDataSourceType(ProceedingJoinPoint joinPoint, Transactional transactional) throws Throwable {

        try {
            MethodSignature signature = (MethodSignature)joinPoint.getSignature();
            Method method = signature.getMethod();

            DataSourceType dataSourceType;

            // 1. 우선순위: @UseMasterDataSource 어노테이션
            if (method.isAnnotationPresent(UseMasterDataSource.class)) {
                dataSourceType = DataSourceType.MASTER;
                log.debug(">>> [AOP] 메서드: {}, @UseMasterDataSource 지정 -> MASTER",
                    method.getName());
            }
            // 2. 우선순위: @UseSlaveDataSource 어노테이션
            else if (method.isAnnotationPresent(UseSlaveDataSource.class)) {
                dataSourceType = DataSourceType.SLAVE;
                log.debug(">>> [AOP] 메서드: {}, @UseSlaveDataSource 지정 -> SLAVE",
                    method.getName());
            }
            // 3. 기본: @Transactional의 readOnly 값에 따라 결정
            else {
                boolean actualReadOnly = TransactionSynchronizationManager.isActualTransactionActive() ?
                    TransactionSynchronizationManager.isCurrentTransactionReadOnly() : transactional.readOnly();

                dataSourceType = actualReadOnly ? DataSourceType.SLAVE : DataSourceType.MASTER;
                log.debug(">>> [AOP] 메서드: {}, @Transactional(readOnly={}) -> {}",
                    method.getName(),
                    transactional.readOnly(),
                    dataSourceType);
            }

            DataSourceContextHolder.setDataSourceType(dataSourceType);

            return joinPoint.proceed();
        } finally {
            DataSourceContextHolder.clearDataSourceType();
        }
    }

}
