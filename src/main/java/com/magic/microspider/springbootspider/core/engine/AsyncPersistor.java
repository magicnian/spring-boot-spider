package com.magic.microspider.springbootspider.core.engine;

import com.magic.microspider.springbootspider.base.bean.engine.Site;
import com.magic.microspider.springbootspider.core.proxy.ProxyPool;
import com.magic.microspider.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

/**
 * 异步持久化
 * Created by liunn on 2018/1/11.
 */
@Slf4j
public class AsyncPersistor extends Thread {

    private boolean shutdown = false;

    private SpiderListener spiderListener;

    public AsyncPersistor(SpiderListener spiderListener) {
        this.spiderListener = spiderListener;
    }

    public void shutdown() {
        shutdown = true;
    }

    public void run() {
        while (!shutdown) {
            CommonUtil.sleep(30000);
            process();
        }
        process();
        log.info("AsyncPersistor is stopped!");
    }


    private void process() {
        //持久化处理信息
        Set<String> domains = spiderListener.getAllSites();
        if (!CommonUtil.isEmpty(domains)) {
            for (String site : domains) {
                spiderListener.persist(site);
            }
        }

        //持久化代理信息
        Set<Site> sites = ProxyPool.getAllSites();
        if (!CommonUtil.isEmpty(sites)) {
            for (Site site : sites) {
                ProxyPool.persist(site);
            }
        }
    }
}
