package com.nearstar.sftpmanager.util;

import com.jcraft.jsch.*;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class SftpConnectionPool {

    private final GenericObjectPool<ChannelSftp> pool;

    public SftpConnectionPool() {
        GenericObjectPoolConfig<ChannelSftp> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(10);
        config.setMaxIdle(5);
        config.setMinIdle(2);
        config.setMaxWait(Duration.ofSeconds(30));
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);

        this.pool = new GenericObjectPool<>(new SftpChannelFactory(), config);
    }

    public ChannelSftp borrowObject(String host, int port, String username, String password)
            throws Exception {
        // For now, create new connections
        // In production, implement proper pooling with connection parameters
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, host, port);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setTimeout(30000);
        session.connect();

        ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();

        return channel;
    }

    public void returnObject(ChannelSftp channel) {
        if (channel != null && channel.isConnected()) {
            Session session = null;
            try {
                session = channel.getSession();
                channel.disconnect();
            } catch (JSchException e) {
                // Log error
            } finally {
                if (session != null && session.isConnected()) {
                    session.disconnect();
                }
            }
        }
    }

    private static class SftpChannelFactory extends BasePooledObjectFactory<ChannelSftp> {
        @Override
        public ChannelSftp create() throws Exception {
            // Implement connection creation
            return null;
        }

        @Override
        public PooledObject<ChannelSftp> wrap(ChannelSftp channel) {
            return new DefaultPooledObject<>(channel);
        }

        @Override
        public boolean validateObject(PooledObject<ChannelSftp> p) {
            ChannelSftp channel = p.getObject();
            return channel != null && channel.isConnected();
        }

        @Override
        public void destroyObject(PooledObject<ChannelSftp> p) throws Exception {
            ChannelSftp channel = p.getObject();
            if (channel != null && channel.isConnected()) {
                Session session = channel.getSession();
                channel.disconnect();
                if (session != null && session.isConnected()) {
                    session.disconnect();
                }
            }
        }
    }
}