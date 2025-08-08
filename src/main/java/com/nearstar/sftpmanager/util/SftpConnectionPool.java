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
package com.nearstar.sftpmanager.util;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
public class SftpConnectionPool
{
    private final GenericObjectPool<ChannelSftp> pool;

    public SftpConnectionPool()
    {
        GenericObjectPoolConfig<ChannelSftp> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal( 10 );
        config.setMaxIdle( 5 );
        config.setMinIdle( 2 );
        config.setMaxWait( Duration.ofSeconds( 30 ) );
        config.setTestOnBorrow( true );
        config.setTestOnReturn( true );
        this.pool = new GenericObjectPool<>( new SftpChannelFactory(), config );
    }

    public ChannelSftp borrowObject( String host, int port, String username, String password )
            throws Exception
    {
        // For now, create new connections
        // In production, implement proper pooling with connection parameters
        JSch jsch = new JSch();
        Session session = jsch.getSession( username, host, port );
        session.setPassword( password );
        session.setConfig( "StrictHostKeyChecking", "no" );
        session.setTimeout( 30000 );
        session.connect();
        ChannelSftp channel = (ChannelSftp) session.openChannel( "sftp" );
        channel.connect();
        return channel;
    }

    public void returnObject( ChannelSftp channel )
    {
        if ( channel != null && channel.isConnected() )
        {
            Session session = null;
            try
            {
                session = channel.getSession();
                channel.disconnect();
            }
            catch (JSchException e)
            {
                log.error( "Error disconnecting SFTP channel", e );
            }
            finally
            {
                if ( session != null && session.isConnected() )
                {
                    session.disconnect();
                }
            }
        }
    }

    private static class SftpChannelFactory extends BasePooledObjectFactory<ChannelSftp>
    {
        @Override
        public ChannelSftp create() throws Exception
        {
            // Implement connection creation
            // This is a placeholder - in a real implementation, you'd need
            // to store connection parameters and create connections here
            return null;
        }

        @Override
        public PooledObject<ChannelSftp> wrap( ChannelSftp channel )
        {
            return new DefaultPooledObject<>( channel );
        }

        @Override
        public boolean validateObject( PooledObject<ChannelSftp> p )
        {
            ChannelSftp channel = p.getObject();
            return channel != null && channel.isConnected();
        }

        @Override
        public void destroyObject( PooledObject<ChannelSftp> p ) throws Exception
        {
            ChannelSftp channel = p.getObject();
            if ( channel != null && channel.isConnected() )
            {
                Session session = channel.getSession();
                channel.disconnect();
                if ( session != null && session.isConnected() )
                {
                    session.disconnect();
                }
            }
        }
    }
}
