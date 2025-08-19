package com.skillexchange.config;

import jakarta.annotation.PostConstruct;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Configuration
public class LiquibaseRunner {

    private final DataSource dataSource;

    @Value("${spring.liquibase.change-log:classpath:db/changelog/changelog-master.yaml}")
    private String changeLog;

    @Value("${spring.liquibase.enabled:true}")
    private boolean liquibaseEnabled;

    public LiquibaseRunner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void runLiquibase() throws SQLException, LiquibaseException {
        if (!liquibaseEnabled) {
            return;
        }
        try (Connection connection = dataSource.getConnection()) {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(
                new liquibase.database.jvm.JdbcConnection(connection)
            );
            Liquibase liquibase = new Liquibase(resolveChangeLogPath(), new ClassLoaderResourceAccessor(), database);
            liquibase.update(new Contexts(), new LabelExpression());
        }
    }

    private String resolveChangeLogPath() {
        // Remove classpath: prefix if present for Liquibase constructor
        if (changeLog.startsWith("classpath:")) {
            return changeLog.substring("classpath:".length());
        }
        return changeLog;
    }
}

