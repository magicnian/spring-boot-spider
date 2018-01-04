package com.magic.microspider.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by liunn on 2018/1/4.
 */

@SuppressWarnings({"rawtypes", "unchecked"})
@Component
public class RedisCacheHandler {

    private static final Logger logger = LoggerFactory.getLogger(RedisCacheHandler.class);

    @Getter
    @Setter
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 缓存是否开启，默认开启,true开启，false不开启
     */
    private volatile boolean redisSwitch = true;

    /**
     * 添加redis缓存，设置超时时间
     * 此超时时间会覆盖掉前一个超时时间
     *
     * @param redisKey 缓存key
     * @param object   缓存对象
     * @param timeout  超时时间
     * @param unit     超时单位
     */
    public void set(String redisKey, Object object, long timeout, TimeUnit unit) {
        if (redisSwitch) {
            try {
                if (!ObjectUtils.isEmpty(object)) {
                    redisTemplate.opsForValue().set(redisKey, JSON.toJSONString(object), timeout, unit);
                }
            } catch (Throwable e) {
                logger.warn("set error:", e);
            }
        }
    }

    /**
     * 不设置超时时间的set方法
     *
     * @param redisKey
     * @param object
     */
    public void setNoExpire(String redisKey, Object object) {
        if (redisSwitch) {
            try {
                if (!ObjectUtils.isEmpty(object)) {
                    BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);
                    operations.set(JSON.toJSONString(object));
                }
            } catch (Throwable e) {
                logger.warn("set error:", e);
            }
        }
    }

    public void hsetNoExpire(String redisKey, String hashKey, Object object) {
        if (redisSwitch) {
            try {
                BoundHashOperations<String, String, String> boundHashOperations = redisTemplate.boundHashOps(redisKey);
                boundHashOperations.put(hashKey, JSON.toJSONString(object));
            } catch (Throwable e) {
                logger.warn("hset error:", e);
            }
        }
    }

    public void expire(String redisKey, int seconds) {
        if (redisSwitch) {
            try {
                BoundValueOperations opt = redisTemplate.boundValueOps(redisKey);
                opt.expire(seconds, TimeUnit.SECONDS);
            } catch (Throwable e) {
                logger.warn("expire error:", e);
            }
        }
    }

    public void expireAt(String redisKey, Date date) {
        if (redisSwitch) {
            try {
                BoundValueOperations opt = redisTemplate.boundValueOps(redisKey);
                opt.expireAt(date);
            } catch (Throwable e) {
                logger.warn("expireAt error:", e);
            }
        }
    }

    /**
     * 添加redis的map缓存，设置超时时间
     *
     * @param redisKey 缓存key
     * @param hashKey
     * @param object   缓存对象
     * @param date     超时时间点
     */
    public void hset(String redisKey, String hashKey, Object object, Date date) {
        if (redisSwitch) {
            try {
                if (!ObjectUtils.isEmpty(object)) {
                    BoundHashOperations boundHashOperations = redisTemplate.boundHashOps(redisKey);
                    boundHashOperations.put(hashKey, JSON.toJSONString(object));
                    boundHashOperations.expireAt(date);
                }
            } catch (Throwable e) {
                logger.warn("hset error:", e);
            }
        }
    }

    /**
     * 添加redis的map缓存，设置超时时间
     * 此超时时间会覆盖掉前一个超时时间
     *
     * @param redisKey 缓存key
     * @param hashKey
     * @param object   缓存对象
     * @param timeout  超时时间
     * @param unit     超时单位
     */
    public void hset(String redisKey, String hashKey, Object object, long timeout, TimeUnit unit) {
        if (redisSwitch) {
            try {
                if (!ObjectUtils.isEmpty(object)) {
                    BoundHashOperations boundHashOperations = redisTemplate.boundHashOps(redisKey);
                    boundHashOperations.put(hashKey, JSON.toJSONString(object));
                    boundHashOperations.expire(timeout, unit);
                }
            } catch (Throwable e) {
                logger.warn("hset error:", e);
            }
        }
    }

    public void hsetAll(String redisKey, Map<String, String> map, long timeout, TimeUnit unit) {
        if (redisSwitch) {
            try {
                BoundHashOperations boundHashOperations = redisTemplate.boundHashOps(redisKey);
                boundHashOperations.putAll(map);
                boundHashOperations.expire(timeout, unit);
            } catch (Throwable e) {
                logger.warn("hsetAll error:", e);
            }
        }
    }

    /**
     * 获取redis非list缓存
     *
     * @param redisKey
     * @param clazz
     * @return
     */
    public <T> T get(String redisKey, Class<T> clazz) {
        if (redisSwitch) {
            try {
                String objectJson = (String) redisTemplate.opsForValue().get(redisKey);
                if (StringUtils.isBlank(objectJson)) {
                    return null;
                }
                return JSON.parseObject(objectJson, clazz);
            } catch (Throwable e) {
                logger.warn("get error:", e);
            }
        }
        return null;
    }

    /**
     * Description: 获取hash缓存内容，如果key/field不存在则返回null
     *
     * @param key
     * @param field
     * @return key-field对应的value
     */
    public String get(String key, String field) {
        return hget(key, field);
    }

    public Long increment(String redisKey, String hashKey, long delta) {
        if (redisSwitch) {
            return redisTemplate.opsForHash().increment(redisKey, hashKey, delta);
        }
        return null;
    }

    /**
     * 获取redis的list缓存
     *
     * @param redisKey
     * @param clazz
     * @return
     */
    public <T> List<T> getList(String redisKey, Class<T> clazz) {
        if (redisSwitch) {
            try {
                String objectJson = (String) redisTemplate.opsForValue().get(redisKey);
                if (StringUtils.isBlank(objectJson)) {
                    return new ArrayList<T>();
                }
                return JSON.parseArray(objectJson, clazz);
            } catch (Throwable e) {
                logger.warn("getList error:", e);
            }
        }

        return new ArrayList<T>();
    }

    /**
     * 获取redis的map缓存
     *
     * @param redisKey
     * @param hashKey
     * @param clazz
     * @return
     */
    public <T> T hget(String redisKey, String hashKey, Class<T> clazz) {
        if (redisSwitch) {
            try {
                String objectJson = (String) redisTemplate.opsForHash().get(redisKey, hashKey);
                if (StringUtils.isBlank(objectJson)) {
                    return null;
                }
                return JSON.parseObject(objectJson, clazz);
            } catch (Throwable e) {
                logger.warn("hget error:", e);
            }
        }
        return null;
    }

    /**
     * 从Hash中获取对象,转换成制定类型
     */
    public <T> T hget(String key, String field, Type clazz) {

        String jsonContext = get(key, field);

        return (T) JSONObject.parseObject(jsonContext, clazz);
    }

    /**
     * 从哈希表key中获取field的value
     *
     * @param key
     * @param field
     */

    public String hget(String key, String field) {
        if (redisSwitch) {
            try {
                String objectJson = (String) redisTemplate.opsForHash().get(key, field);
                if (StringUtils.isBlank(objectJson)) {
                    return null;
                }
                return objectJson;
            } catch (Throwable e) {
                logger.warn("hget error:", e);
            }
        }
        return null;
    }

    /**
     * 获取redis中map的list缓存
     *
     * @param redisKey
     * @param hashKey
     * @param clazz
     * @return
     */
    public <T> List<T> hgetList(String redisKey, String hashKey, Class<T> clazz) {
        if (redisSwitch) {
            try {
                String objectJson = (String) redisTemplate.opsForHash().get(redisKey, hashKey);
                if (StringUtils.isBlank(objectJson)) {
                    return null;
                }
                return JSON.parseArray(objectJson, clazz);
            } catch (Throwable e) {
                logger.warn("hgetList error:", e);
            }
        }
        return new ArrayList<T>();
    }

    /**
     * 删除redis某个key的缓存
     *
     * @param redisKey
     */
    public void delete(String redisKey) {
        if (redisSwitch) {
            try {
                redisTemplate.delete(redisKey);
            } catch (Throwable e) {
                logger.warn("delete error:", e);
            }
        }
    }

    /**
     * 删除redis中map的key
     *
     * @param redisKey
     * @param hashKeys
     */
    public void hdelete(String redisKey, String... hashKeys) {
        if (redisSwitch) {
            try {
                redisTemplate.boundHashOps(redisKey).delete((Object) hashKeys);
            } catch (Throwable e) {
                logger.warn("hdelete error:", e);
            }
        }
    }

    public Map<String, String> hgetall(String key) {
        if (redisSwitch) {
            try {
                HashOperations<String, String, String> opt = redisTemplate.opsForHash();
                return opt.entries(key);
            } catch (Throwable e) {
                logger.warn("hgetall error:", e);
            }
        }
        return null;
    }

    /**
     * 根据hashkeys列表获取缓存列表
     *
     * @param key
     * @param hashKeys
     * @return
     */
    public List<String> hgetall(String key, Collection<String> hashKeys) {
        if (redisSwitch) {
            try {
                HashOperations<String, String, String> opt = redisTemplate.opsForHash();
                return opt.multiGet(key, hashKeys);
            } catch (Throwable e) {
                logger.warn("hgetall error:", e);
            }
        }
        return null;
    }

    public <T> List<T> hgetall(String key, Collection<String> hashKeys, Class<T> targetClass) {
        if (redisSwitch) {
            try {
                HashOperations<String, String, String> opt = redisTemplate.opsForHash();
                List<String> jsonList = opt.multiGet(key, hashKeys);
                if (jsonList != null && !jsonList.isEmpty()) {
                    return JSON.parseArray(jsonList.toString(), targetClass);
                }
            } catch (Throwable e) {
                logger.warn("hgetall error:", e);
            }
        }
        return null;
    }

    /**
     * 重置redis的过期时间
     *
     * @param redisKey
     * @return
     */
    public boolean resetExpireTime(String redisKey, long timeout, TimeUnit unit) {
        if (redisSwitch) {
            try {
                return redisTemplate.boundValueOps(redisKey).expire(timeout, unit);
            } catch (Exception e) {
                logger.warn("resetExpireTime error:", e);
                return false;
            }
        }
        return true;
    }

    public int incrAtDayTime(String key, int i) {
        if (redisSwitch) {
            Long l = redisTemplate.boundValueOps(key).increment(i);
            if (l != null) {
                return l.intValue();
            }
        }
        return -1;
    }

    public int incr(String key, int i) {
        if (redisSwitch) {
            Long l = redisTemplate.boundValueOps(key).increment(i);
            if (l != null) {
                return l.intValue();
            }
        }
        return -1;
    }

    /**
     * hash自增
     *
     * @param key
     * @param hashKey
     * @param i
     * @return
     */
    public int incr(String key, String hashKey, int i) {
        if (redisSwitch) {
            Long l = redisTemplate.boundHashOps(key).increment(hashKey, i);
            if (l != null) {
                return l.intValue();
            }
        }
        return -1;
    }

    /**
     * hash自减
     *
     * @param key
     * @param i
     * @return
     */
    public int decr(String key, String hashKey, int i) {
        if (redisSwitch) {
            Long l = redisTemplate.boundHashOps(key).increment(hashKey, -i);
            if (l != null) {
                return l.intValue();
            }
        }
        return -1;
    }

    public int decr(String key, int i) {
        if (redisSwitch) {
            Long l = redisTemplate.boundValueOps(key).increment(-i);
            if (l != null) {
                return l.intValue();
            }
        }
        return -1;
    }

    /**
     * 从队列弹出头部元素
     *
     * @param key
     * @return
     */
    public <T> T lPop(final String key, Class<T> entityClass) {
        return (T) redisTemplate.execute((RedisCallback<T>) con -> {
            if (con.exists(key.getBytes())) {
                byte[] bts = con.lPop(key.getBytes());
                if (null != bts) {
                    return JSON.parseObject(new String(bts), entityClass);
                }
            }
            return null;
        });
    }

    /**
     * 添加元素到队头
     *
     * @param key
     * @param value
     * @return
     */
    public <T> T lPush(String key, T value) {
        return (T) redisTemplate.execute((RedisCallback<Long>) con -> con.lPush(key.getBytes(), JSON.toJSONString(value).getBytes()));
    }

    /**
     * 添加元素到队尾
     *
     * @param key
     * @param value
     * @return
     */
    public <T> T rPush(String key, T value) {
        return (T) redisTemplate.execute((RedisCallback<Long>) con -> con.rPush(key.getBytes(), JSON.toJSONString(value).getBytes()));
    }

    /**
     * 存储差集(无序)
     *
     * @param key
     * @param destKey
     * @param collection
     * @return
     */
    public long sDiffstore(String key, String destKey, Collection collection) {
        return redisTemplate.opsForSet().differenceAndStore(key, collection, destKey);
    }

    /**
     * 是否在集合(无序)
     *
     * @param key
     * @param value
     * @return
     */
    public <T> boolean sIsmember(String key, T value) {
        return (boolean) redisTemplate.execute((RedisCallback<Boolean>) con -> con.sIsMember(key.getBytes(), JSON.toJSONString(value).getBytes()));
    }

    /**
     * 添加到无序集
     *
     * @param key
     * @param value
     * @return
     */
    public <T> long sAdd(String key, T value) {
        return (long) redisTemplate.execute((RedisCallback<Long>) con -> con.sAdd(key.getBytes(), JSON.toJSONString(value).getBytes()));
    }

    /**
     * 从无序集删除
     *
     * @param key
     * @param value
     * @return
     */
    public <T> Boolean sRemove(String key, T value) {
        return (Boolean) redisTemplate.execute((RedisCallback<Boolean>) con -> {
            if (con.exists(key.getBytes())) {
                Long cnt = con.sRem(key.getBytes(), JSON.toJSONString(value).getBytes());
                if (null == cnt || cnt.intValue() != 1) {
                    return false;
                }
            }
            return true;
        });

    }

    /**
     * 从无序集取出
     *
     * @param key
     * @return
     */
    public <T> List<T> sMemeber(String key, Class<T> entityClass) {
        return (List<T>) redisTemplate.execute((RedisCallback<List<T>>) con -> {
            List<T> list = new ArrayList<>();
            Set<byte[]> sets = con.sMembers(key.getBytes());
            if (null != sets && !sets.isEmpty()) {
                for (byte[] bts : sets) {
                    T t = JSON.parseObject(new String(bts), entityClass);
                    list.add(t);
                }
            }
            return list;
        });
    }

    /**
     * 获取有序集数量
     *
     * @param key
     * @return
     */
    public long zCard(String key) {
        return redisTemplate.opsForZSet().zCard(key);
    }

    /**
     * 获取有序集score
     *
     * @param key
     * @param value
     * @return
     */
    public Double zScore(String key, Object value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    /**
     * 添加SET到有序集
     *
     * @param key
     * @param value {@link java.util.Set}valueSet
     * @return
     */
    public long zAddSet(String key, Set<? extends ZSetOperations.TypedTuple> value) {
        return redisTemplate.opsForZSet().add(key, value);
    }

    /**
     * 添加到有序集
     *
     * @param key
     * @param value
     * @param score
     * @return
     */
    public boolean zAdd(String key, Object value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * 获取有序集区间（按score）
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    public Set zRangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    /**
     * 获取有序集区间,包含Scores（按score）
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    public Set<ZSetOperations.TypedTuple<String>> zRangeByScoreWithScores(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max);
    }

    /**
     * 获取有序集区间（按score）
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    public Set zRangeByScore(String key, double min, double max, long offset, long count) {
        return redisTemplate.opsForZSet().rangeByScore(key, min, max, offset, count);
    }

    /**
     * 获取有序集区间,包含Scores（按score）
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    public Set<ZSetOperations.TypedTuple<String>> zRangeByScoreWithScores(String key, double min, double max, long offset, long count) {
        return redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max, offset, count);
    }


    public long zRemove(String key, Object... value) {
        return redisTemplate.opsForZSet().remove(key, value);
    }
}
