package com.magic.microspider.springbootspider.core.engine;

import com.magic.microspider.springbootspider.base.bean.engine.SiteProcessInfo;

import java.util.Set;

/**
 * 爬虫监听器
 * Created by liunn on 2018/1/11.
 */
public interface SpiderListener {

    /**
     * 从持久化文件中读取处理个数
     *
     * @param site
     */
    void init(String site);


    /**
     * 获取持久化文件
     *
     * @param site
     * @return
     */
    String getPersistFile(String site);

    /**
     * 持久化某个站点的代理信息到文件
     *
     * @param site
     */
    void persist(String site);

    /**
     * 销毁某个站点的处理个数信息
     *
     * @param site
     */
    void destroy(String site);

    /**
     * 获取某个站点的处理情况
     *
     * @param site
     * @return
     */
    SiteProcessInfo getProcessInfo(String site);

    /**
     * 增加处理个数
     *
     * @param site
     */
    void increProcess(String site);

    /**
     * 获取所有站点
     *
     * @return
     */
    Set<String> getAllSites();


    /**
     * 开始回调
     *
     * @param siteId
     * @param startTime
     * @param siteDomain
     */
    void start(String siteId, Long startTime, String siteDomain);

    /**
     * 完成回调
     *
     * @param siteId
     * @param endTime
     * @param siteDomain
     */
    void complete(String siteId, Long endTime, String siteDomain);

    /**
     * 运行出错
     *
     * @param runningErr
     * @param siteId
     */
    void runningError(int runningErr, String siteId);

    /**
     * 获取错误个数
     *
     * @param site
     * @return
     */
    int getErrCnt(String site);
}
