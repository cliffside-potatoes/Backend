package com.potatoes.Naengu.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
public class RoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        DataSourceType type = TransactionSynchronizationManager.isCurrentTransactionReadOnly()
                ? DataSourceType.SLAVE
                : DataSourceType.MASTER;
        log.info("[DataSource] routing -> {}", type);
        return type;
    }
}
