package com.magic.microspider.springbootspider.service.consumer;

import com.magic.microspider.springbootspider.dao.engine.SiteRedisDao;
import com.magic.microspider.springbootspider.dao.redis.StringRedisDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by liunn on 2018/1/16.
 */
public class SiteTaskConsumer extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(SiteTaskConsumer.class);

    private StringRedisDao stringRedisDao;

    private SiteRedisDao siteRedisDao;

}
