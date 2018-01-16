package com.magic.microspider.springbootspider.dao.engine;

import com.magic.microspider.springbootspider.base.bean.engine.Request;
import com.magic.microspider.springbootspider.dao.redis.AbstractRedisDao;
import org.springframework.stereotype.Repository;

/**
 * Created by liunn on 2018/1/16.
 */
@Repository
public class RequestRedisDao extends AbstractRedisDao<Request> {
}
