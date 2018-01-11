package com.magic.microspider.springbootspider.base.bean.engine;

import com.magic.microspider.springbootspider.base.enums.ProxyStatus;
import com.magic.microspider.util.CommonUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by liunn on 2018/1/11.
 */
public class Proxy extends BaseProxy implements Delayed {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 4107171416462152816L;

    public static final int SUCCESS = 200;
    public static final int FAIL = 500;

    private Long delayInterval = 1l;//获取代理延迟时间:ms

    private Long canUseTime;//延迟后可以使用的时刻，该值根据delayInterval计算所得

    private Long reviveInterval;//复活时间:ms

    private int invalidNum = 1;//失效次数

    private Long lastBorrowTime = System.currentTimeMillis();//上次使用时间

    private AtomicLong responseTime = new AtomicLong(0);//响应时长

    private AtomicInteger failedNum = new AtomicInteger(0);//历史失效次数
    private AtomicInteger succNum = new AtomicInteger(0);//历史成功次数
    private AtomicInteger borrowNum = new AtomicInteger(0);//历史使用次数

    private AtomicInteger currFailedNum = new AtomicInteger(0);//本次任务执行失效次数
    private AtomicInteger currSuccNum = new AtomicInteger(0);//本次任务执行成功次数
    private AtomicInteger currBorrowNum = new AtomicInteger(0);//本次任务执行使用次数

    private AtomicInteger continueFailedNum = new AtomicInteger(0);//本次任务连续失败次数

    private Set<Integer> failedTypes = new HashSet<Integer>();

    public Proxy() {

    }

    public Proxy(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Long getDelayInterval() {
        return delayInterval;
    }

    public void setDelayInterval(Long delayInterval) {
        this.delayInterval = delayInterval;
        this.canUseTime = CommonUtil.getCurrentMillSecTime() + delayInterval;
    }

    public Long getReviveInterval() {
        return reviveInterval;
    }

    public void setReviveInterval(Long reviveInterval) {
        this.reviveInterval = reviveInterval;
    }

    public Long getCanUseTime() {
        return canUseTime;
    }

    public void setCanUseTime(Long canUseTime) {
        this.canUseTime = canUseTime;
    }

    public void canUseTime(Long canUseTime) {
        this.canUseTime = CommonUtil.getCurrentMillSecTime() + canUseTime;
    }

    public void success() {
        succNum.incrementAndGet();
        currSuccNum.incrementAndGet();
        failedTypes.clear();
    }

    public Long getLastBorrowTime() {
        return lastBorrowTime;
    }

    public void setLastBorrowTime(Long lastBorrowTime) {
        this.lastBorrowTime = lastBorrowTime;
    }

    public long recordResponse() {
        return responseTime.addAndGet(System.currentTimeMillis() - lastBorrowTime);
    }

    public Set<Integer> getFailedTypes() {
        return failedTypes;
    }

    public void fail(Integer failType) {
        this.failedTypes.add(failType);
        failedNum.incrementAndGet();
        currFailedNum.incrementAndGet();
        continueFailedNum.incrementAndGet();
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long minus = canUseTime - CommonUtil.getCurrentMillSecTime();
        return unit.convert(minus, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        Proxy that = (Proxy) o;
        return canUseTime > that.canUseTime ? 1 : (canUseTime < that.canUseTime ? -1 : 0);

    }

    public void borrow() {
        this.borrowNum.incrementAndGet();
        currBorrowNum.incrementAndGet();
        lastBorrowTime = CommonUtil.getCurrentMillSecTime();
    }

    public Boolean isRemoved() {
        return isRemoved == ProxyStatus.IS_ACTIVE.code() ? false : true;
    }

    public void remove() {
        this.isRemoved = ProxyStatus.IS_REMOVED.code();
    }

    public int getInvalidNum() {
        return invalidNum;
    }

    public void setInvalidNum(int invalidNum) {
        this.invalidNum = invalidNum;
    }

    public AtomicLong getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(AtomicLong responseTime) {
        this.responseTime = responseTime;
    }

    public AtomicInteger getFailedNum() {
        return failedNum;
    }

    public void setFailedNum(AtomicInteger failedNum) {
        this.failedNum = failedNum;
    }

    public AtomicInteger getSuccNum() {
        return succNum;
    }

    public void setSuccNum(AtomicInteger succNum) {
        this.succNum = succNum;
    }

    public AtomicInteger getBorrowNum() {
        return borrowNum;
    }

    public void setBorrowNum(AtomicInteger borrowNum) {
        this.borrowNum = borrowNum;
    }

    public void setFailedTypes(Set<Integer> failedTypes) {
        this.failedTypes = failedTypes;
    }


    public AtomicInteger getCurrFailedNum() {
        return currFailedNum;
    }

    public void setCurrFailedNum(AtomicInteger currFailedNum) {
        this.currFailedNum = currFailedNum;
    }

    public AtomicInteger getCurrSuccNum() {
        return currSuccNum;
    }

    public void setCurrSuccNum(AtomicInteger currSuccNum) {
        this.currSuccNum = currSuccNum;
    }

    public AtomicInteger getCurrBorrowNum() {
        return currBorrowNum;
    }

    public void setCurrBorrowNum(AtomicInteger currBorrowNum) {
        this.currBorrowNum = currBorrowNum;
    }

    public AtomicInteger getContinueFailedNum() {
        return continueFailedNum;
    }

    public void setContinueFailedNum(AtomicInteger continueFailedNum) {
        this.continueFailedNum = continueFailedNum;
    }
}
