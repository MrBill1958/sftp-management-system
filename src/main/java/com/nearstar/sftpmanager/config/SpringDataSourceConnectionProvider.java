/**
 * NearStar, Inc.
 * 410 E. Main Street
 * Lewisville, Texas  76057
 * Tel: 1.972.221.4068
 * <p>
 * Copyright � 2025 NearStar Incorporated. All rights reserved.
 * <p>
 * <p>
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF NEARSTAR Inc.
 * <p>
 * THIS COPYRIGHT NOTICE DOES NOT EVIDENCE ANY
 * ACTUAL OR INTENDED PUBLICATION OF SUCH SOURCE CODE.
 * This software and its source code are proprietary and confidential to NearStar Incorporated.
 * Unauthorized copying, modification, distribution, or use of this software, in whole or in part,
 * is strictly prohibited without the prior written consent of the copyright holder.
 * Portions of this software may utilize or be derived from open-source software
 * and publicly available frameworks licensed under their respective licenses.
 * <p>
 * This code may also include contributions developed with the assistance of AI-based tools.
 * All open-source dependencies are used in accordance with their applicable licenses,
 * and full attribution is maintained in the corresponding documentation (e.g., NOTICE or LICENSE files).
 * For inquiries regarding licensing or usage, please make request by going to nearstar.com.
 *
 * @file ${NAME}.java
 * @author ${USER} <${USER}@nearstar.com>
 * @version 1.0.0
 * @date ${DATE}
 * @project SFTP Site Management System
 * @package com.nearstar.sftpmanager
 * <p>
 * Copyright    ${YEAR} Nearstar
 * @license Proprietary
 * @modified
 */
package com.nearstar.sftpmanager.config;

import org.quartz.utils.ConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Spring DataSource Connection Provider for Quartz
 * This class bridges Spring's DataSource with Quartz's connection management
 */
@Component
public class SpringDataSourceConnectionProvider implements ConnectionProvider
{

    private DataSource dataSource;

    @Autowired
    public void setDataSource( DataSource dataSource )
    {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getConnection() throws SQLException
    {
        if ( dataSource == null )
        {
            throw new SQLException( "DataSource is not configured" );
        }
        return dataSource.getConnection();
    }

    @Override
    public void shutdown() throws SQLException
    {
        // No shutdown required for Spring-managed DataSource
        // Spring will handle the lifecycle of the DataSource
    }

    @Override
    public void initialize() throws SQLException
    {
        // No initialization required for Spring-managed DataSource
        // Spring has already initialized the DataSource
    }

    public DataSource getDataSource()
    {
        return dataSource;
    }
}