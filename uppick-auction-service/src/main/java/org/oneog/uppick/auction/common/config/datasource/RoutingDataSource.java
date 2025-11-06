package org.oneog.uppick.auction.common.config.datasource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

@Slf4j
public class RoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {

        DataSourceType dataSourceType = DataSourceContextHolder.getDataSourceType();

        // 명시적으로 설정되지 않았다면 MASTER 사용
        if (dataSourceType == null) {
            log.debug(">>> DataSource 타입이 설정되지 않아 MASTER 사용");
            return DataSourceType.MASTER;
        }

        log.debug(">>> 현재 사용할 DataSource: {}", dataSourceType);
        return dataSourceType;
    }

}
