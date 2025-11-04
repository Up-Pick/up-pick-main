package org.oneog.uppick.auction.common.config.datasource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataSourceContextHolder {

    private static final ThreadLocal<DataSourceType> CONTEXT = new ThreadLocal<>();

    public static void setDataSourceType(DataSourceType dataSourceType) {
        log.info(">>> DataSource 전환: {}", dataSourceType);
        CONTEXT.set(dataSourceType);
    }

    public static DataSourceType getDataSourceType() {
        return CONTEXT.get();
    }

    public static void clearDataSourceType() {
        CONTEXT.remove();
    }
}
