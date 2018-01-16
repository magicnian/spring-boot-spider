package com.magic.microspider.springbootspider.dao.redis;

import com.alibaba.fastjson.JSON;
import com.magic.microspider.util.CommonUtil;
import com.magic.microspider.util.ReflectionUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by liunn on 2018/1/4.
 */
public abstract class AbstractRedisDao<T> {

    public static final Integer BATCH_DEL_SIZE = 200;

    @Autowired
    protected RedisTemplate<String, Object> redisTemplate;

    protected Class<T> entityClass;

    @SuppressWarnings("unchecked")
    public AbstractRedisDao()
    {
        this.entityClass = ReflectionUtil.getSuperClassGenricType(getClass(), 0);
    }

    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate)
    {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 以字符串保存单个对象
     * @param key
     * @param value
     * @return
     * @see [类、类#方法、类#成员]
     */
    public Boolean saveAsJSONStr(final String key, final T value)
    {
        return redisTemplate.execute(new RedisCallback<Boolean>()
        {
            @Override
            public Boolean doInRedis(RedisConnection con) throws DataAccessException
            {
                con.set(key.getBytes(), JSON.toJSONString(value).getBytes());
                return true;
            }
        });
    }

    /**
     * 以字符串保存多个对象
     * @param key
     * @param values
     * @return
     * @see [类、类#方法、类#成员]
     */
    public Boolean saveAsJSONStr(final Map<String, T> values)
    {
        return redisTemplate.execute(new RedisCallback<Boolean>()
        {
            @Override
            public Boolean doInRedis(RedisConnection con) throws DataAccessException
            {
                if (null == values || values.isEmpty())
                {
                    return true;
                }
                Map<byte[], byte[]> map = new HashMap<byte[], byte[]>();
                Set<String> keys = values.keySet();
                for (String key : keys)
                {
                    map.put(key.getBytes(), JSON.toJSONString(values.get(key)).getBytes());
                }
                con.mSet(map);
                return true;
            }
        });
    }

    /**
     * 以字符串保存单个对象
     * @param key
     * @param value
     * @param expireSecs 过期时间，单位:秒
     * @return
     * @see [类、类#方法、类#成员]
     */
    public Boolean saveAsJSONStr(final String key, final T value,final long expireSecs)
    {
        return redisTemplate.execute(new RedisCallback<Boolean>()
        {
            @Override
            public Boolean doInRedis(RedisConnection con) throws DataAccessException
            {
                con.set(key.getBytes(), JSON.toJSONString(value).getBytes());
                con.expire(key.getBytes(), expireSecs);
                return true;
            }
        });
    }

    /**
     * 从字符串获取单个对象
     * @param key
     * @return
     * @see [类、类#方法、类#成员]
     */
    public T readFromJSONStr(final String key)
    {
        return redisTemplate.execute(new RedisCallback<T>()
        {
            @Override
            public T doInRedis(RedisConnection con) throws DataAccessException
            {
                if (con.exists(key.getBytes()))
                {
                    return JSON.parseObject(new String(con.get(key.getBytes())), entityClass);
                }
                return null;
            }
        });
    }

    /**
     * 以字符串保存单个对象
     * @param key
     * @param value
     * @return
     * @see [类、类#方法、类#成员]
     */
    public Boolean saveStr(final String key, final String value,final long expireSecs)
    {
        return redisTemplate.execute(new RedisCallback<Boolean>()
        {
            @Override
            public Boolean doInRedis(RedisConnection con) throws DataAccessException
            {
                con.set(key.getBytes(), value.getBytes());
                con.expire(key.getBytes(), expireSecs);
                return true;
            }
        });
    }

    /**
     * 以字符串保存单个对象
     * @param key
     * @param value
     * @return
     * @see [类、类#方法、类#成员]
     */
    public Boolean saveStr(final String key, final String value)
    {
        return redisTemplate.execute(new RedisCallback<Boolean>()
        {
            @Override
            public Boolean doInRedis(RedisConnection con) throws DataAccessException
            {
                con.set(key.getBytes(), value.getBytes());
                return true;
            }
        });
    }

    /**
     * 以字符串保存多个对象
     * @param key
     * @param values
     * @return
     * @see [类、类#方法、类#成员]
     */
    public Boolean saveStr(final Map<String, String> values)
    {
        return redisTemplate.execute(new RedisCallback<Boolean>()
        {
            @Override
            public Boolean doInRedis(RedisConnection con) throws DataAccessException
            {
                if (null == values || values.isEmpty())
                {
                    return true;
                }
                Map<byte[], byte[]> map = new HashMap<byte[], byte[]>();
                Set<String> keys = values.keySet();
                for (String key : keys)
                {
                    map.put(key.getBytes(), values.get(key).getBytes());
                }
                con.mSet(map);
                return true;
            }
        });
    }

    /**
     * 读取字符串
     * @param key
     * @return
     * @see [类、类#方法、类#成员]
     */
    public String readStr(final String key)
    {
        return redisTemplate.execute(new RedisCallback<String>()
        {
            @Override
            public String doInRedis(RedisConnection con) throws DataAccessException
            {
                if (con.exists(key.getBytes()))
                {
                    return new String(con.get(key.getBytes()));
                }
                return null;
            }
        });
    }

    /**
     * 读取字符串
     * @param keys
     * @return 与key顺序一一对应的结果对象，如果key查不到则为null
     * @see [类、类#方法、类#成员]
     */
    public List<String> readStr(final List<String> keys)
    {
        return redisTemplate.execute(new RedisCallback<List<String>>()
        {
            @Override
            public List<String> doInRedis(RedisConnection con) throws DataAccessException
            {
                int size = keys.size();
                byte[][] bts = new byte[size][];
                for (int i = 0; i < size; i ++)
                {
                    bts[i] = keys.get(i).getBytes();
                }
                List<byte[]> results = con.mGet(bts);
                if (null != results && !results.isEmpty())
                {
                    List<String> list = new ArrayList<String>();
                    for (byte[] result : results)
                    {
                        if (null != result)
                        {
                            list.add(new String(result));
                        }
                        else
                        {
                            list.add(null);
                        }
                    }
                    return list;
                }
                return null;
            }
        });
    }

    /**
     * 从字符串获取单个对象
     * @param keys
     * @return 与key顺序一一对应的结果对象，如果key查不到则为null
     * @see [类、类#方法、类#成员]
     */
    public List<T> readFromJSONStr(final List<String> keys)
    {
        return redisTemplate.execute(new RedisCallback<List<T>>()
        {
            @Override
            public List<T> doInRedis(RedisConnection con) throws DataAccessException
            {
                int size = keys.size();
                byte[][] bts = new byte[size][];
                for (int i = 0; i < size; i ++)
                {
                    bts[i] = keys.get(i).getBytes();
                }
                List<byte[]> results = con.mGet(bts);
                if (null != results && !results.isEmpty())
                {
                    List<T> list = new ArrayList<T>();
                    for (byte[] result : results)
                    {
                        if (null != result)
                        {
                            list.add(JSON.parseObject(new String(result), entityClass));
                        }
                        else
                        {
                            list.add(null);
                        }
                    }
                    return list;
                }
                return null;
            }
        });
    }

    /**
     * 以HashSet保存单个对象
     * @param key
     * @param value
     * @return
     * @see [类、类#方法、类#成员]
     */
    public Boolean saveAsHash(final String key, final T value)
    {
        return redisTemplate.execute(new RedisCallback<Boolean>()
        {
            @Override
            public Boolean doInRedis(RedisConnection con) throws DataAccessException
            {
                Field[] fields = entityClass.getDeclaredFields();
                Map<byte[], byte[]> hashes = new HashMap<byte[], byte[]>();
                for (int i = 0; i < fields.length; i++)
                {
                    String hashKey = fields[i].getName();
                    if (hashKey.equals("serialVersionUID"))
                    {
                        continue;
                    }
                    Object hashValue = ReflectionUtil.getFieldValue(value, hashKey);
                    hashes.put(hashKey.getBytes(), JSON.toJSONString(hashValue).getBytes());
                }
                con.hMSet(key.getBytes(), hashes);
                return true;
            }
        });
    }

    /**
     * 以HashSet保存对象
     * @param key
     * @param value
     * @param expireSecs 过期时间，单位：秒
     * @return
     * @see [类、类#方法、类#成员]
     */
    public Boolean saveAsHash(final String key, final T value,final long expireSecs)
    {
        return redisTemplate.execute(new RedisCallback<Boolean>()
        {
            @Override
            public Boolean doInRedis(RedisConnection con) throws DataAccessException
            {
                Field[] fields = entityClass.getDeclaredFields();
                Map<byte[], byte[]> hashes = new HashMap<byte[], byte[]>();
                for (int i = 0; i < fields.length; i++)
                {
                    String hashKey = fields[i].getName();
                    if (hashKey.equals("serialVersionUID"))
                    {
                        continue;
                    }
                    Object hashValue = ReflectionUtil.getFieldValue(value, hashKey);
                    hashes.put(hashKey.getBytes(), JSON.toJSONString(hashValue).getBytes());
                }
                con.hMSet(key.getBytes(), hashes);
                con.expire(key.getBytes(), expireSecs);
                return true;
            }
        });
    }

    /**
     * 从HashSet获取单个对象
     * @param key
     * @return
     * @see [类、类#方法、类#成员]
     */
    public T readFromHash(final String key)
    {
        return redisTemplate.execute(new RedisCallback<T>()
        {
            @Override
            public T doInRedis(RedisConnection con) throws DataAccessException
            {
                if (con.exists(key.getBytes()))
                {
                    Field[] fields = entityClass.getDeclaredFields();
                    try
                    {
                        T obj = (T)entityClass.newInstance();
                        for (int i = 0,len = fields.length; i < len; i++)
                        {
                            String hashKey = fields[i].getName();
                            if (con.hExists(key.getBytes(), hashKey.getBytes()))
                            {
                                ReflectionUtil.setFieldValue(obj,
                                        hashKey,
                                        JSON.parseObject(con.hGet(key.getBytes(), hashKey.getBytes()), fields[i].getType()));
                            }
                        }
                        return obj;
                    }
                    catch (InstantiationException | IllegalAccessException e)
                    {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        });
    }

    /**
     * 更新对象某个属性的值
     * @param key
     * @param fieldName
     * @param fieldVal
     * @return
     * @see [类、类#方法、类#成员]
     */
    public boolean updateFromHash(final String key,final String fieldName,final Object fieldVal)
    {
        return redisTemplate.execute(new RedisCallback<Boolean>()
        {
            @Override
            public Boolean doInRedis(RedisConnection con) throws DataAccessException
            {
                return con.hSet(key.getBytes(), fieldName.getBytes(), JSON.toJSONString(fieldVal).getBytes());
            }
        });
    }

    /**
     * 从JsonArray获取对象列表
     *
     * @param key主键
     * @return 对象列表
     */
    public Boolean saveListAsJSONStr(final String key,final List<T> values)
    {
        return redisTemplate.execute(new RedisCallback<Boolean>()
        {
            @Override
            public Boolean doInRedis(RedisConnection con) throws DataAccessException
            {
                if (null == values || values.isEmpty())
                {
                    return true;
                }
                byte[] bytes = JSON.toJSONString(values).getBytes();
                con.set(key.getBytes(), bytes);
                return true;
            }
        });
    }

    /**
     * 从JsonArray获取对象列表
     *
     * @param key主键
     * @return 对象列表
     */
    public List<T> readFromJSONListStr(final String key)
    {
        return redisTemplate.execute(new RedisCallback<List<T>>()
        {
            @Override
            public List<T> doInRedis(RedisConnection con) throws DataAccessException
            {
                if (con.exists(key.getBytes()))
                {
                    return JSON.parseArray(new String(con.get(key.getBytes())), entityClass);
                }
                return null;
            }
        });
    }

    /**
     * 以SortSet保存单个对象
     * @param key
     * @param value
     * @return
     * @see [类、类#方法、类#成员]
     */
    public Boolean save2SortSet(final String key, final T value)
    {
        return redisTemplate.execute(new RedisCallback<Boolean>()
        {
            @Override
            public Boolean doInRedis(RedisConnection con) throws DataAccessException
            {
                return con.zAdd(key.getBytes(), getSetScore(value), JSON.toJSONString(value).getBytes());
            }
        });
    }

    /**
     * 添加元素到Set
     * @param key
     * @param value
     * @return
     */
    public Long save2Set(final String key,final T value)
    {
        return redisTemplate.execute(new RedisCallback<Long>()
        {
            @Override
            public Long doInRedis(RedisConnection con) throws DataAccessException
            {
                return con.sAdd(key.getBytes(), JSON.toJSONString(value).getBytes());
            }
        });
    }

    /**
     * 从Set读取元素
     * @param key
     * @param value
     * @return
     */
    public List<T> readSet(final String key)
    {
        return redisTemplate.execute(new RedisCallback<List<T>>()
        {
            @Override
            public List<T> doInRedis(RedisConnection con) throws DataAccessException
            {
                List<T> list = new ArrayList<T>();
                Set<byte[]> sets = con.sMembers(key.getBytes());
                if (null != sets && !sets.isEmpty())
                {
                    for (byte[] bts : sets)
                    {
                        T t = JSON.parseObject(new String(bts), entityClass);
                        list.add(t);
                    }
                }
                return list;
            }
        });
    }

    /**
     * 判断某个元素是否存在Set中
     * @param key
     * @param value
     * @return
     */
    public Boolean existsInSet(final String key,final T value)
    {
        return redisTemplate.execute(new RedisCallback<Boolean>()
        {
            @Override
            public Boolean doInRedis(RedisConnection con) throws DataAccessException
            {
                return con.sIsMember(key.getBytes(), JSON.toJSONString(value).getBytes());
            }
        });
    }

    /**
     * 获取Set长度
     * @param key
     * @return
     */
    public Long getSetSize(final String key)
    {
        return redisTemplate.execute(new RedisCallback<Long>()
        {
            @Override
            public Long doInRedis(RedisConnection con) throws DataAccessException
            {
                if (con.exists(key.getBytes()))
                {
                    return con.sCard(key.getBytes());
                }
                return 0l;
            }
        });
    }

    /**
     * 添加元素到队尾
     * @param key
     * @param value
     * @return
     */
    public Long push2Queue(final String key,final T value)
    {
        return redisTemplate.execute(new RedisCallback<Long>()
        {
            @Override
            public Long doInRedis(RedisConnection con) throws DataAccessException
            {
                return con.rPush(key.getBytes(), JSON.toJSONString(value).getBytes());
            }
        });
    }

    /**
     * 添加元素到队尾
     * @param key
     * @param value
     * @return
     */
    public Long push2Queue(final String key,final List<T> values)
    {
        return redisTemplate.execute(new RedisCallback<Long>()
        {
            @Override
            public Long doInRedis(RedisConnection con) throws DataAccessException
            {
                int size = values.size();
                byte[][] bts = new byte[size][];
                for (int i = 0; i < size; i ++)
                {
                    bts[i] = JSON.toJSONString(values.get(i)).getBytes();
                }
                return con.rPush(key.getBytes(), bts);
            }
        });
    }

    /**
     * 添加元素到队头
     * @param key
     * @param value
     * @return
     */
    public Long push2HeadQueue(final String key,final T value)
    {
        return redisTemplate.execute(new RedisCallback<Long>()
        {
            @Override
            public Long doInRedis(RedisConnection con) throws DataAccessException
            {
                return con.lPush(key.getBytes(), JSON.toJSONString(value).getBytes());
            }
        });
    }

    /**
     * 从队列弹出头部元素
     * @param key
     * @return
     */
    public T popFromQueue(final String key)
    {
        return redisTemplate.execute(new RedisCallback<T>()
        {
            @Override
            public T doInRedis(RedisConnection con) throws DataAccessException
            {
                if (con.exists(key.getBytes()))
                {
                    byte[] bts = con.lPop(key.getBytes());
                    if (null != bts)
                    {
                        return JSON.parseObject(new String(bts), entityClass);
                    }
                }
                return null;
            }
        });
    }

    /**
     * 获取Queue长度
     * @param key
     * @return
     */
    public Long queueSize(final String key)
    {
        return redisTemplate.execute(new RedisCallback<Long>()
        {
            @Override
            public Long doInRedis(RedisConnection con) throws DataAccessException
            {
                if (con.exists(key.getBytes()))
                {
                    return con.lLen(key.getBytes());
                }
                return 0l;
            }
        });
    }

    /**
     * 以SortSet保存单个对象
     * @param key
     * @param value
     * @return
     * @see [类、类#方法、类#成员]
     */
    public Boolean save2SortSet(final String key, final T value,final double score)
    {
        return redisTemplate.execute(new RedisCallback<Boolean>()
        {
            @Override
            public Boolean doInRedis(RedisConnection con) throws DataAccessException
            {
                return con.zAdd(key.getBytes(), score, JSON.toJSONString(value).getBytes());
            }
        });
    }

    /**
     * 以SortSet保存多个对象
     * @param key
     * @param values
     * @return
     * @see [类、类#方法、类#成员]
     */
    public Boolean save2SortSet(final String key, final List<T> values)
    {
        return redisTemplate.execute(new RedisCallback<Boolean>()
        {
            @Override
            public Boolean doInRedis(RedisConnection con) throws DataAccessException
            {
                if (null == values || values.isEmpty())
                {
                    return true;
                }
                con.openPipeline();
                for (T value : values)
                {
                    con.zAdd(key.getBytes(), getSetScore(value), JSON.toJSONString(value).getBytes());
                }
                con.closePipeline();
                return true;
            }
        });
    }

    /**
     * 以SortSet保存多个对象
     * @param key
     * @param values
     * @return
     * @see [类、类#方法、类#成员]
     */
    public Boolean save2SortSet(final String key, final Map<Double,T> values)
    {
        return redisTemplate.execute(new RedisCallback<Boolean>()
        {
            @Override
            public Boolean doInRedis(RedisConnection con) throws DataAccessException
            {
                if (null == values || values.isEmpty())
                {
                    return true;
                }
                con.openPipeline();
                Set<Double> scores = values.keySet();
                for (Double score : scores)
                {
                    T value = values.get(score);
                    con.zAdd(key.getBytes(),score, JSON.toJSONString(value).getBytes());
                }
                con.closePipeline();
                return true;
            }
        });
    }

    /**
     * 从SortSet获取对象
     * @param key
     * @return
     * @see [类、类#方法、类#成员]
     */
    public List<T> readFromSortSet(final String key)
    {
        return redisTemplate.execute(new RedisCallback<List<T>>()
        {
            @Override
            public List<T> doInRedis(RedisConnection con) throws DataAccessException
            {
                if (con.exists(key.getBytes()))
                {
                    List<T> valueList = new ArrayList<T>();
                    Set<byte[]> byteList = con.zRange(key.getBytes(), 0, -1);
                    for (byte[] value : byteList)
                    {
                        valueList.add(JSON.parseObject(new String(value), entityClass));
                    }
                    return valueList;
                }
                return null;
            }
        });
    }

    /**
     * 从SortSet删除单个对象
     * @param key
     * @param value
     * @return
     * @see [类、类#方法、类#成员]
     */
    public Boolean removeFromSortSet(final String key, final T value)
    {
        return redisTemplate.execute(new RedisCallback<Boolean>()
        {
            @Override
            public Boolean doInRedis(RedisConnection con) throws DataAccessException
            {
                if(con.exists(key.getBytes()))
                {
                    Long cnt = con.zRem(key.getBytes(), JSON.toJSONString(value).getBytes());
                    if (null == cnt || cnt.intValue() != 1)
                    {
                        return false;
                    }
                }
                return true;
            }
        });
    }

    /**
     * 从SortSet删除多个对象
     * @param key
     * @param values
     * @return
     * @see [类、类#方法、类#成员]
     */
    public Boolean removeFromSortSet(final String key, final List<T> values)
    {
        return redisTemplate.execute(new RedisCallback<Boolean>()
        {
            @Override
            public Boolean doInRedis(RedisConnection con) throws DataAccessException
            {
                if (null == values || values.isEmpty())
                {
                    return true;
                }
                if(con.exists(key.getBytes()))
                {
                    int size = values.size();
                    byte[][] bts = new byte[size][];
                    for (int i = 0; i < size; i ++)
                    {
                        bts[i] = JSON.toJSONString(values.get(i)).getBytes();
                    }
                    Long cnt = con.zRem(key.getBytes(), bts);
                    if (null == cnt || cnt.intValue() != size)
                    {
                        return false;
                    }
                }
                return true;
            }
        });
    }

    /**
     * long计数器加法
     *
     * @param key主键， value
     * @return long
     */
    public long incrHashValueBy(final String key, final String fieldName, final long value)
    {
        return redisTemplate.execute(new RedisCallback<Long>()
        {
            @Override
            public Long doInRedis(RedisConnection con) throws DataAccessException
            {
                return con.hIncrBy(key.getBytes(), fieldName.getBytes(), value);
            }
        });
    }

    /**
     * double计数器加法
     *
     * @param key主键， value
     * @return long
     */
    public double incrHashValueBy(final String key, final String fieldName, final double value)
    {
        return redisTemplate.execute(new RedisCallback<Double>()
        {
            @Override
            public Double doInRedis(RedisConnection con) throws DataAccessException
            {
                return con.hIncrBy(key.getBytes(), fieldName.getBytes(), value);
            }
        });
    }

    /**
     * long计数器加法
     *
     * @param key主键， value
     * @return long
     */
    public long incrValueBy(final String key, final long value)
    {
        return redisTemplate.execute(new RedisCallback<Long>()
        {
            @Override
            public Long doInRedis(RedisConnection con) throws DataAccessException
            {
                return con.incrBy(key.getBytes(), value);
            }
        });
    }

    /**
     * long计数器减法
     *
     * @param key主键， value
     * @return long
     */
    public long decrValueBy(final String key, final long value)
    {
        return redisTemplate.execute(new RedisCallback<Long>()
        {
            @Override
            public Long doInRedis(RedisConnection con) throws DataAccessException
            {
                return con.decrBy(key.getBytes(), value);
            }
        });
    }

    /**
     * double计数器加法
     *
     * @param key主键， value
     * @return long
     */
    public double incrValueBy(final String key,  final double value)
    {
        return redisTemplate.execute(new RedisCallback<Double>()
        {
            @Override
            public Double doInRedis(RedisConnection con) throws DataAccessException
            {
                return con.incrBy(key.getBytes(), value);
            }
        });
    }

    /**
     * 获取Redis ZSet的排序权重值
     *
     * @param value对象
     * @return double
     */
    protected double getSetScore(T value)
    {
        return CommonUtil.getCurrentMillSecTime();
    }

    /**
     * 从Redis删除对象
     *
     * @param key主键
     * @return void
     */
    public void remove(final String key)
    {
        redisTemplate.delete(key);
    }

    /**
     * 从Redis删除对象列表
     *
     * @param key主键
     * @return void
     */
    public void removeKeys(final List<String> keys)
    {
        int size = keys.size();
        if (size <= BATCH_DEL_SIZE)
        {
            redisTemplate.delete(keys);
        }
        else
        {
            int mod = size % BATCH_DEL_SIZE;
            int temp = size / BATCH_DEL_SIZE;
            int total = mod == 0 ? temp : (temp + 1);
            for (int i = 0; i < total; i ++)
            {
                int start = i * BATCH_DEL_SIZE;
                int end = (i + 1) * BATCH_DEL_SIZE;
                if (end >= size)
                {
                    end = size;
                }
                List<String> batch = keys.subList(start, end);
                redisTemplate.delete(batch);
            }
        }
    }

    /**
     * 判断某个key是否存在
     * @param key
     * @return
     */
    public boolean exists(final String key)
    {
        return redisTemplate.hasKey(key);
    }

    /**
     * set if not exists
     * @param key
     * @param value
     * @return
     */
    @SuppressWarnings("rawtypes")
    public Boolean setNX(final String key, final String value,final long expireSecs)
    {
        return redisTemplate.execute((RedisCallback<Boolean>) con -> {
            byte[] b = con.get(key.getBytes());
            List result = null;
            if (null == b)
            {
                con.watch(key.getBytes());
                con.multi();//开启一个事物
                con.set(key.getBytes(), value.getBytes());
                con.expire(key.getBytes(), expireSecs);
                result = con.exec();
            }
            return CollectionUtils.isNotEmpty(result);
        });
    }

    /**
     * set if not exists
     * @param key
     * @param value
     * @return
     */
    public Boolean setNX(final String key, final String value)
    {
        return redisTemplate.execute(new RedisCallback<Boolean>()
        {
            @Override
            public Boolean doInRedis(RedisConnection con) throws DataAccessException
            {
                return con.setNX(key.getBytes(), value.getBytes());
            }
        });
    }

    /**
     * 以JSON字符串保存单个对象
     * @param key
     * @param value
     * @param expireSecs 过期时间，单位:秒
     * @return
     * @see [类、类#方法、类#成员]
     */
    @SuppressWarnings("rawtypes")
    public Boolean setNXJSON(final String key, final T value,final long expireSecs)
    {
        return redisTemplate.execute((RedisCallback<Boolean>) con -> {
            byte[] b = con.get(key.getBytes());
            List result = null;
            if (null == b)
            {
                con.watch(key.getBytes());
                con.multi();//开启一个事物
                con.set(key.getBytes(), JSON.toJSONString(value).getBytes());
                con.expire(key.getBytes(), expireSecs);
                result = con.exec();
            }
            return CollectionUtils.isNotEmpty(result);
        });
    }

}
