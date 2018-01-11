package com.magic.microspider.springbootspider.base.bean.engine;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by liunn on 2018/1/11.
 */
public class SiteProcessInfo implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -4195475323442877510L;

    /**
     * 节点ID
     */
    private String workerId;

    /**
     * 处理个数
     */
    private AtomicInteger processCnt;

    /**
     * 最近一次处理时间
     */
    private Long lastProcessTime;

    private Map<String, Object> processParam;

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public AtomicInteger getProcessCnt() {
        return processCnt;
    }

    public void setProcessCnt(AtomicInteger processCnt) {
        this.processCnt = processCnt;
    }

    public Long getLastProcessTime() {
        return lastProcessTime;
    }

    public void setLastProcessTime(Long lastProcessTime) {
        this.lastProcessTime = lastProcessTime;
    }

    public Map<String, Object> getProcessParam() {
        return processParam;
    }

    public void setProcessParam(Map<String, Object> processParam) {
        this.processParam = processParam;
    }

    public void incre() {
        if (null == processCnt) {
            processCnt = new AtomicInteger(0);
        }
        processCnt.incrementAndGet();
    }

}
