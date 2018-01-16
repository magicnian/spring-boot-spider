package com.magic.microspider.springbootspider.dao.engine;

import com.magic.microspider.springbootspider.base.bean.engine.Site;
import com.magic.microspider.springbootspider.dao.redis.AbstractRedisDao;
import org.springframework.stereotype.Repository;

/**
 * Created by liunn on 2018/1/16.
 */
@Repository
public class SiteRedisDao extends AbstractRedisDao<Site> {
}
