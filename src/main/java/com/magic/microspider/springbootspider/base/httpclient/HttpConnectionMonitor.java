package com.magic.microspider.springbootspider.base.httpclient;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * HTTP链接监控线程
 * Created by liunn on 2018/1/16.
 */
public class HttpConnectionMonitor extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpConnectionMonitor.class);

    private boolean shutdown = false;

    /**
     * 空闲时长，单位：s
     */
    private static final Long IDLE_SECS = 60L;

    public void run() {
        LOGGER.info("Start to monitor Http Connection...");
        while (!shutdown) {
            PoolingHttpClientConnectionManager connMgr = HttpClientPool.getConnMgr();
            if (null != connMgr) {
                connMgr.closeExpiredConnections();
                connMgr.closeIdleConnections(IDLE_SECS, TimeUnit.SECONDS);
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void shutdown() {
        this.shutdown = true;
    }

}
