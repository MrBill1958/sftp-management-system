package com.nearstar.sftpmanager.config;

import org.quartz.utils.ConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class SpringDataSourceConnectionProvider implements ConnectionProvider {

    @Autowired
    private DataSource dataSource;

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void shutdown() throws SQLException {
        // Spring manages the DataSource lifecycle
    }

    @Override
    public void initialize() throws SQLException {
        // Spring manages the DataSource lifecycle
    }
}