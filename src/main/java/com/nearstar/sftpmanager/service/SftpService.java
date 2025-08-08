/**
 * NearStar, Inc.
 * 410 E. Main Street
 * Lewisville, Texas  76057
 * Tel: 1.972.221.4068
 * <p>
 * Copyright © 2025 NearStar Incorporated. All rights reserved.
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
package com.nearstar.sftpmanager.service;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.nearstar.sftpmanager.model.entity.Site;
import com.nearstar.sftpmanager.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Slf4j
@Service
@RequiredArgsConstructor
public class SftpService
{

    private final EncryptionUtil encryptionUtil;

    public TestConnectionResult testConnection( Site site )
    {
        Session session = null;
        ChannelSftp channel = null;

        try
        {
            log.info( "Testing connection to site: {} ({}:{})",
                    site.getSiteName(), site.getIpAddress(), site.getPort() );

            JSch jsch = new JSch();
            session = jsch.getSession( site.getUsername(), site.getIpAddress(), site.getPort() );

            String password = encryptionUtil.decrypt( site.getEncryptedPassword() );
            session.setPassword( password );

            Properties config = new Properties();
            config.put( "StrictHostKeyChecking", "no" );
            config.put( "PreferredAuthentications", "password" );
            session.setConfig( config );
            session.setTimeout( 30000 );

            session.connect();
            log.info( "✓ SSH connection established" );

            channel = (ChannelSftp) session.openChannel( "sftp" );
            channel.connect( 10000 );
            log.info( "✓ SFTP channel opened" );

            String pwd = channel.pwd();
            log.info( "✓ Current directory: {}", pwd );

            return new TestConnectionResult( true,
                    "Connection successful. Current directory: " + pwd );

        }
        catch (Exception e)
        {
            log.error( "Connection test failed: {}", e.getMessage() );
            return new TestConnectionResult( false,
                    "Connection failed: " + e.getMessage() );
        }
        finally
        {
            if ( channel != null && channel.isConnected() )
            {
                channel.disconnect();
            }
            if ( session != null && session.isConnected() )
            {
                session.disconnect();
            }
        }
    }

    public static class TestConnectionResult
    {
        private final boolean success;
        private final String message;

        public TestConnectionResult( boolean success, String message )
        {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess()
        {
            return success;
        }

        public String getMessage()
        {
            return message;
        }
    }
}