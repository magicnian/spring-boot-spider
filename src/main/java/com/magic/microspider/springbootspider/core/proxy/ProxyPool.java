package com.magic.microspider.springbootspider.core.proxy;

import com.alibaba.fastjson.JSON;
import com.magic.microspider.springbootspider.base.bean.engine.Proxy;
import com.magic.microspider.springbootspider.base.bean.engine.Site;
import com.magic.microspider.util.CommonUtil;
import com.magic.microspider.util.ProxyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.springframework.beans.BeanUtils;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by liunn on 2018/1/11.
 */
@Slf4j
public class ProxyPool {

    /**
     * 站点对应的所有代理
     */
    private static Map<Site, Set<Proxy>> SITE_ALLPROXY_MAP = new ConcurrentHashMap<Site, Set<Proxy>>();

    /**
     * 站点对应的可用代理
     */
    private static Map<Site, DelayQueue<Proxy>> SITE_PROXY_MAP = new ConcurrentHashMap<Site, DelayQueue<Proxy>>();

    private static String PROXY_DIR;

    static {
        String dataDir = System.getProperty("data.dir");
        if (CommonUtil.isEmptyStr(dataDir)) {
            File home = new File(System.getProperty("user.home"));
            dataDir = home + File.separator + "data" + File.separator + "Spider";
        }
        dataDir += File.separator + "proxy";
        File dir = new File(dataDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        PROXY_DIR = dataDir;
    }

    /**
     * 动态新增/修改代理：代理池中的所有站点均增加/修改该代理
     *
     * @param proxy
     * @return
     */
    public static boolean dynamicPut(Proxy proxy) {
        log.info("ProxyPool dynamicPut ProxyModel:" + JSON.toJSONString(proxy));
        if (proxy.isRemoved()) {
            log.warn("Dynamic add proxy:" + proxy.getKey() + " fail,this proxy is removed!");
            return false;
        }
        Set<Map.Entry<Site, Set<Proxy>>> allProxys = SITE_ALLPROXY_MAP.entrySet();
        if (null != allProxys && !allProxys.isEmpty()) {
            for (Map.Entry<Site, Set<Proxy>> siteProxys : allProxys) {
                Site site = siteProxys.getKey();

                //复制站点设置的代理信息
                copySiteProxy(site, proxy);

                Set<Proxy> proxys = siteProxys.getValue();
                boolean succ = proxys.add(proxy);
                if (succ)//不存在则新增
                {
                    DelayQueue<Proxy> queue = SITE_PROXY_MAP.get(site);
                    queue.put(proxy);
                } else //已存在则修改Auth
                {
                    in:
                    for (Proxy p : proxys) {
                        if (p.getKey().equals(proxy.getKey())) {
                            p.setAuthName(proxy.getAuthName());
                            p.setAuthPwd(proxy.getAuthPwd());
                            p.setProxyType(proxy.getProxyType());
                            break in;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * 动态删除代理
     *
     * @param proxy
     * @return
     */
    public static boolean dynamicRemove(Proxy proxy) {
        try {
            Collection<Set<Proxy>> allProxys = SITE_ALLPROXY_MAP.values();
            Collection<DelayQueue<Proxy>> queueProxys = SITE_PROXY_MAP.values();
            if (null != allProxys && !allProxys.isEmpty()) {
                for (Set<Proxy> proxys : allProxys) {
                    Iterator<Proxy> it = proxys.iterator();
                    in:
                    while (it.hasNext()) {
                        Proxy p = it.next();
                        if (p.getKey().equals(proxy.getKey())) {
                            p.remove();//设置已删除判断代理是否放回可用代理池中
                            it.remove();
                            break in;
                        }
                    }
                }
            }

            if (null != queueProxys && !queueProxys.isEmpty()) {
                for (DelayQueue<Proxy> proxys : queueProxys) {
                    Iterator<Proxy> it = proxys.iterator();
                    in:
                    while (it.hasNext()) {
                        Proxy p = it.next();
                        if (p.getKey().equals(proxy.getKey())) {
                            it.remove();
                            break in;
                        }
                    }
                }
            }

//            WebDriverPool.removeProxy(proxy);//删除代理的WebDriver
        } catch (Exception e) {
            log.warn("dynamic remove proxy:" + proxy.getKey() + " error!", e);
            return false;
        }
        log.info("dynamic remove proxy:" + proxy.getKey() + " end!");
        return true;
    }

    /**
     * 从持久化文件中读取Proxy初始化站点代理
     *
     * @param site
     * @return
     */
    public static void init(Site site) {
        //从持久化文件中读取Proxy
        List<Proxy> persistProxys = readPersist(site.getSiteDomain());
        if (null != persistProxys && !persistProxys.isEmpty()) {
            addProxy(persistProxys, site, false);
        }
    }

    /**
     * 添加站点代理
     *
     * @param srcProxys
     * @param site
     */
    public static void addProxy(List<Proxy> srcProxys, Site site, boolean validate) {
        if (null != srcProxys && !srcProxys.isEmpty()) {
            Set<Proxy> allProxys = SITE_ALLPROXY_MAP.get(site);
            DelayQueue<Proxy> queue = SITE_PROXY_MAP.get(site);
            for (Proxy srcProxy : srcProxys) {
                //复制站点设置的代理属性
                copySiteProxy(site, srcProxy);

                Proxy proxy = new Proxy();
                try {
                    BeanUtils.copyProperties(srcProxy, proxy);
                    proxy.canUseTime((long) (proxy.getSpeed() * 1000));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (null == allProxys) {
                    allProxys = Collections.synchronizedSet(new HashSet<Proxy>());
                    SITE_ALLPROXY_MAP.put(site, allProxys);
                }
                if (null == queue) {
                    queue = new DelayQueue<Proxy>();
                    SITE_PROXY_MAP.put(site, queue);
                }
                Proxy existProxy = null;
                Iterator<Proxy> allIt = allProxys.iterator();
                loop:
                while (allIt.hasNext())//如果已经存在则修改
                {
                    Proxy p = allIt.next();
                    if (p.getKey().equals(proxy.getKey()))//修改用户名密码
                    {
                        p.setAuthName(proxy.getAuthName());
                        p.setAuthPwd(proxy.getAuthPwd());
                        p.setProxyType(proxy.getProxyType());
                        existProxy = p;
                        break loop;
                    }
                }
                if (existProxy == null) {
                    allProxys.add(proxy);
                    queue.put(proxy);
                } else {
                    if (!queue.contains(existProxy)) {
                        queue.put(existProxy);
                    }
                }

            }

            //清空已删除的proxy
            allProxys.retainAll(srcProxys);
            queue.retainAll(srcProxys);

            //验证代理
            if (validate) {
                Iterator<Proxy> it = queue.iterator();
                while (it.hasNext()) {
                    Proxy p = it.next();
                    HttpHost host = new HttpHost(p.getHost(), p.getPort());
                    if (!ProxyUtil.validateProxy(host)) {
                        it.remove();
                    }
                }
            }
            log.info(site.getSiteDomain() + " ProxyPool size:" + queue.size());
        }
    }

    private static void copySiteProxy(Site site, Proxy proxy) {
        //复制站点设置的代理信息
        proxy.setDelayInterval(site.getProxyDelayMs().longValue());
        proxy.setReviveInterval(site.getProxyReviveMs().longValue());
        proxy.setInvalidNum(site.getProxyInvalidNum());
    }

    /**
     * 获取某个站点的代理
     *
     * @param site
     * @return
     */
    public static Proxy getProxy(Site site) {
        try {
            DelayQueue<Proxy> queue = SITE_PROXY_MAP.get(site);
            Proxy proxy = queue.poll(site.getDownTimeout(), TimeUnit.MILLISECONDS);
            if (null != proxy) {
                proxy.borrow();
            } else {
                //判断代理是否都已经失效
                boolean allIsInvalid = true;
                Set<Proxy> allProxys = SITE_ALLPROXY_MAP.get(site);
                if (null != allProxys && !allProxys.isEmpty()) {
                    loop:
                    for (Proxy allProxy : allProxys) {
                        if (allProxy.getCurrFailedNum().intValue() <= allProxy.getInvalidNum()) {
                            allIsInvalid = false;
                            break loop;
                        }
                    }
                }
                if (allIsInvalid) {
                    log.warn(site.getSiteDomain() + " ProxyPool is empty after " + site.getDownTimeout() + " ms,and all proxys is invalid!");
                } else {
                    log.warn(site.getSiteDomain() + " ProxyPool is empty after " + site.getDownTimeout() + " ms,but other proxys is valid!");
                    return null;
                }
            }
            return proxy;
        } catch (Exception e) {
            log.warn(site.getSiteDomain() + " get proxy error", e);
        }
        return null;
    }

    /**
     * 返回代理
     *
     * @param site
     * @param p
     * @param statusCode
     */
    public static void returnProxy(Site site, Proxy p, int statusCode) {
        if (null == p) {
            log.warn(site.getSiteDomain() + " ProxyPool returnProxy is null");
            return;
        }
        long canUseInterval = p.getDelayInterval();
        switch (statusCode) {
            case Proxy.SUCCESS:
                if (p.getContinueFailedNum().intValue() < p.getInvalidNum())//如果连续失败多少次则失效
                {
                    p.canUseTime(p.getDelayInterval());
                    p.success();
                    p.getContinueFailedNum().set(0);
                    p.recordResponse();
                }
                break;
            default:
                p.fail(statusCode);
                p.recordResponse();
                canUseInterval = p.getDelayInterval() * p.getCurrFailedNum().intValue();
                p.canUseTime(canUseInterval);
                break;
        }
        DelayQueue<Proxy> queue = SITE_PROXY_MAP.get(site);
        if (p.getContinueFailedNum().intValue() > p.getInvalidNum())//如果连续失败多少次则失效
        {
            p.canUseTime(p.getReviveInterval());
            log.warn("Suspend ProxyModel:" + p.getKey() + " of " + site.getSiteDomain() + " for " + p.getReviveInterval() + " ms,remain proxy size:" + queue.size());
            return;
        }
        if (!p.isRemoved())//如果已经删除则不放入可用代理池中
        {
            queue.put(p);
        } else {
            log.warn("ProxyModel:" + p.getKey() + " is removed,it will not be returned to pool");
        }
    }

    /**
     * 获取所有站点代理信息
     *
     * @return
     */
    public static Map<Site, Set<Proxy>> getAllSiteProxys() {
        return SITE_ALLPROXY_MAP;
    }

    /**
     * 获取所有站点
     *
     * @return
     */
    public static Set<Site> getAllSites() {
        return SITE_ALLPROXY_MAP.keySet();
    }

    /**
     * 获取某个站点的可用代理个数
     *
     * @param site
     * @return
     */
    public int getIdleNum(Site site) {
        DelayQueue<Proxy> queue = SITE_PROXY_MAP.get(site);
        return queue.size();
    }

    /**
     * 销毁某个站点的代理
     *
     * @param site
     */
    public static void destroy(Site site) {
        log.info("start to clear Spider:" + site.getSiteDomain() + " ProxyPool...");
        SITE_PROXY_MAP.remove(site);
        SITE_ALLPROXY_MAP.remove(site);
        String persitPath = getPersistFile(site.getSiteDomain());
        File file = new File(persitPath);
        if (file.exists()) {
            file.delete();
        }
        log.info("clear Spider:" + site.getSiteDomain() + " ProxyPool success!");
    }

    /**
     * 读取持久化代理信息
     *
     * @param site
     * @return
     */
    private static List<Proxy> readPersist(String site) {
        String persitPath = getPersistFile(site);
        File file = new File(persitPath);
        if (!file.exists()) {
            return null;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(persitPath)));
            StringBuilder builder = new StringBuilder();
            String data;
            while (null != (data = reader.readLine())) {
                builder.append(data);
            }
            if (!CommonUtil.isEmptyStr(builder.toString())) {
                return JSON.parseArray(builder.toString(), Proxy.class);
            }
        } catch (Exception e) {
            log.warn("ProxyPool readPersist:" + site + " error", e);
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 获取持久化文件
     *
     * @param site
     * @return
     */
    public static String getPersistFile(String site) {
        return PROXY_DIR + File.separator + site + ".json";
    }

    /**
     * 持久化某个站点的代理信息到文件
     *
     * @param site
     */
    public static void persist(Site site) {
        String persitPath = getPersistFile(site.getSiteDomain());
        BufferedWriter writer = null;
        try {
            File file = new File(persitPath);
            if (!file.exists()) {
                file.createNewFile();
            }
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            Set<Proxy> proxys = SITE_ALLPROXY_MAP.get(site);
            if (null != proxys) {
                writer.write(JSON.toJSONString(proxys));
            }
            writer.flush();
        } catch (Exception e) {
            log.warn("ProxyPool persist:" + site + " error", e);
        } finally {
            if (null != writer) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 初始化本次任务执行次数
     *
     * @param site
     */
    public static void initCurrNum(Site site) {
        Set<Proxy> allProxys = SITE_ALLPROXY_MAP.get(site);
        if (null != allProxys && !allProxys.isEmpty()) {
            for (Proxy proxy : allProxys) {
                if (!site.isRunning()) {
                    proxy.getCurrBorrowNum().set(0);
                    proxy.getCurrFailedNum().set(0);
                    proxy.getCurrSuccNum().set(0);
                    proxy.getContinueFailedNum().set(0);
                }
                proxy.canUseTime(proxy.getDelayInterval() * proxy.getCurrFailedNum().intValue());
            }
        }
    }
}
