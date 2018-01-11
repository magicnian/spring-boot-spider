package com.magic.microspider.springbootspider.base.bean.engine;

import com.magic.microspider.springbootspider.base.enums.SiteStatus;
import com.magic.microspider.util.CommonUtil;
import com.magic.microspider.util.annotation.ParamValidator;
import lombok.ToString;

import java.io.Serializable;
import java.util.*;

/**
 * 爬虫站点信息
 * Created by liunn on 2018/1/11.
 */
@ToString
public class Site implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -168680959077924656L;

    private String siteId;

    /**
     * 站点域名,unique
     */
    @ParamValidator(required = true)
    private String siteDomain;


    /**
     * 站点名称，可以用中文描述
     */
    @ParamValidator(required = true)
    private String siteName;

    /**
     * 种子url
     */
    @ParamValidator(required = true)
    private String siteSeed;

    /**
     * 反射类全称
     */
    @ParamValidator(required = true)
    private String processorClz;

    /**
     * cron表达式
     */
    @ParamValidator(required = true)
    private String cronTab;

    /**
     * 任务开始执行时间
     */
    @ParamValidator(required = true, isNumber = true)
    private Long startTime;

    /**
     * 任务完成执行时间
     */
    @ParamValidator(required = true, isNumber = true)
    private Long endTime;

    private String extraParams;

    private String userAgent;

    private String defaultCookies;

    private String defaultHeaders;

    /**
     * 字符集
     */
    @ParamValidator(required = true)
    private String charSet;


    @ParamValidator(required = true, isUnsignInt = true)
    private Integer sleepTime;


    @ParamValidator(required = true, isUnsignInt = true)
    private Integer useGzip;

    private String succCodes = "200";

    /**
     * 线程数
     */
    @ParamValidator(required = true, isUnsignInt = true)
    private Integer threadNum;

    /**
     * 下载超时时间
     */
    @ParamValidator(required = true, isUnsignInt = true)
    private Integer downTimeout;

    /**
     * 下载失败时重试次数
     */
    @ParamValidator(required = true, isUnsignInt = true)
    private Integer retryTimes;

    @ParamValidator(required = true, isUnsignInt = true)
    private Integer concurCon;

    @ParamValidator(required = true, isUnsignInt = true)
    private Integer timeoutMs;

    @ParamValidator(required = true, isUnsignInt = true)
    private Integer cycleRetryTimes;

    @ParamValidator(required = true, isUnsignInt = true)
    private Integer cycleSleepTime;

    /**
     * 是否使用代理（1：使用）
     */
    @ParamValidator(required = true, isUnsignInt = true)
    private Integer useProxy;

    @ParamValidator(isUnsignInt = true)
    private Integer proxyDelayMs;

    @ParamValidator(isUnsignInt = true)
    private Integer proxyInvalidNum;

    @ParamValidator(isUnsignInt = true)
    private Integer proxyReviveMs;

    private Integer currStatus;

    private Integer runningError;

    private Long latestStartTime;

    private Long latestEndTime;

    private Long createTime;

    private Set<Integer> acceptStatCodes = new HashSet<>();

    private Map<String, String> cookieMap = new HashMap<>();

    private Map<String, String> headerMap = new HashMap<>();

    private final List<String> proxyList = new ArrayList<>();

    private String taskId;//当前JOB执行任务ID

    private boolean autoRedirect = false;//是否自动跳转

    private boolean useDefaultCookieSpec = false;

    private String proxySiteName;

    private boolean isUseLaxRedirectStrategy = false;

    private static boolean shutdown = false; //是否停止

    public static Site me() {
        return new Site();
    }

    public Crawler toTask() {
        return new Crawler() {
            @Override
            public String getUUID() {
                return Site.this.getSiteDomain();
            }

            @Override
            public Site getSite() {
                return Site.this;
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Site site = (Site) o;
        if (siteDomain != null ? !siteDomain.equals(site.siteDomain) : site.siteDomain != null)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = siteDomain != null ? siteDomain.hashCode() : 0;
        return result;
    }

    public Boolean isCompleted() {
        return this.currStatus == SiteStatus.COMPLETED.code() ? true : false;
    }

    public Boolean isRunning() {
        return this.currStatus == SiteStatus.RUNNING.code() ? true : false;
    }

    public void start() {
        this.currStatus = SiteStatus.RUNNING.code();
        this.latestStartTime = CommonUtil.getCurrentSecondTime();
        this.latestEndTime = null;
    }

    public void complete() {
        this.currStatus = SiteStatus.COMPLETED.code();
        this.latestEndTime = CommonUtil.getCurrentSecondTime();
    }

    public boolean useProxy() {
        return this.getUseProxy() == 1 ? true : false;
    }

    public boolean useGzip() {
        return this.getUseGzip() == 1 ? true : false;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getSiteDomain() {
        return siteDomain;
    }

    public void setSiteDomain(String siteDomain) {
        this.siteDomain = siteDomain;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getSiteSeed() {
        return siteSeed;
    }

    public void setSiteSeed(String siteSeed) {
        this.siteSeed = siteSeed;
    }

    public String getProcessorClz() {
        return processorClz;
    }

    public void setProcessorClz(String processorClz) {
        this.processorClz = processorClz;
    }

    public String getCronTab() {
        return cronTab;
    }

    public void setCronTab(String cronTab) {
        this.cronTab = cronTab;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public String getExtraParams() {
        return extraParams;
    }

    public void setExtraParams(String extraParams) {
        this.extraParams = extraParams;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getDefaultCookies() {
        return defaultCookies;
    }

    public void setDefaultCookies(String defaultCookies) {
        this.defaultCookies = defaultCookies;
        if (!CommonUtil.isEmptyStr(defaultCookies)) {
            String[] cooks = defaultCookies.split(";");
            for (String cook : cooks) {
                String[] coos = cook.split(":");
                cookieMap.put(coos[0], coos[1]);
            }
        }
    }

    public String getDefaultHeaders() {
        return defaultHeaders;
    }

    public void setDefaultHeaders(String defaultHeaders) {
        this.defaultHeaders = defaultHeaders;
        if (!CommonUtil.isEmptyStr(defaultHeaders)) {
            String[] heads = defaultHeaders.split(";");
            for (String head : heads) {
                String[] hs = head.split(":");
                headerMap.put(hs[0], hs[1]);
            }
        }
    }

    public String getCharSet() {
        return charSet;
    }

    public void setCharSet(String charSet) {
        this.charSet = charSet;
    }

    public Integer getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(Integer sleepTime) {
        this.sleepTime = sleepTime;
    }

    public Integer getUseGzip() {
        return useGzip;
    }

    public void setUseGzip(Integer useGzip) {
        this.useGzip = useGzip;
    }

    public String getSuccCodes() {
        return succCodes;
    }

    public void setSuccCodes(String succCodes) {
        this.succCodes = succCodes;
        if (!CommonUtil.isEmptyStr(succCodes)) {
            String[] stateCodes = succCodes.split(",");
            for (String stateCode : stateCodes) {
                acceptStatCodes.add(Integer.parseInt(stateCode));
            }
        }
    }

    public Integer getThreadNum() {
        return threadNum;
    }

    public void setThreadNum(Integer threadNum) {
        this.threadNum = threadNum;
    }

    public Integer getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(Integer retryTimes) {
        this.retryTimes = retryTimes;
    }

    public Integer getConcurCon() {
        return concurCon;
    }

    public void setConcurCon(Integer concurCon) {
        this.concurCon = concurCon;
    }

    public Integer getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(Integer timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public Integer getCycleRetryTimes() {
        return cycleRetryTimes;
    }

    public void setCycleRetryTimes(Integer cycleRetryTimes) {
        this.cycleRetryTimes = cycleRetryTimes;
    }

    public Integer getCycleSleepTime() {
        return cycleSleepTime;
    }

    public void setCycleSleepTime(Integer cycleSleepTime) {
        this.cycleSleepTime = cycleSleepTime;
    }

    public Integer getUseProxy() {
        return useProxy;
    }

    public void setUseProxy(Integer useProxy) {
        this.useProxy = useProxy;
    }

    public Integer getProxyDelayMs() {
        return proxyDelayMs;
    }

    public void setProxyDelayMs(Integer proxyDelayMs) {
        this.proxyDelayMs = proxyDelayMs;
    }

    public Integer getProxyInvalidNum() {
        return proxyInvalidNum;
    }

    public void setProxyInvalidNum(Integer proxyInvalidNum) {
        this.proxyInvalidNum = proxyInvalidNum;
    }

    public Integer getProxyReviveMs() {
        return proxyReviveMs;
    }

    public void setProxyReviveMs(Integer proxyReviveMs) {
        this.proxyReviveMs = proxyReviveMs;
    }

    public Integer getCurrStatus() {
        return currStatus;
    }

    public void setCurrStatus(Integer currStatus) {
        this.currStatus = currStatus;
    }

    public Integer getRunningError() {
        return runningError;
    }

    public void setRunningError(Integer runningError) {
        this.runningError = runningError;
    }

    public Long getLatestStartTime() {
        return latestStartTime;
    }

    public void setLatestStartTime(Long latestStartTime) {
        this.latestStartTime = latestStartTime;
    }

    public Long getLatestEndTime() {
        return latestEndTime;
    }

    public void setLatestEndTime(Long latestEndTime) {
        this.latestEndTime = latestEndTime;
    }

    public Set<Integer> getAcceptStatCodes() {
        return acceptStatCodes;
    }

    public Map<String, String> getCookieMap() {
        return cookieMap;
    }

    public Map<String, String> getHeaderMap() {
        return headerMap;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Integer getDownTimeout() {
        return downTimeout;
    }

    public void setDownTimeout(Integer downTimeout) {
        this.downTimeout = downTimeout;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public static void shutdown() {
        shutdown = true;
    }

    public boolean isAutoRedirect() {
        return autoRedirect;
    }

    public void setAutoRedirect(boolean autoRedirect) {
        this.autoRedirect = autoRedirect;
    }

    public boolean isUseDefaultCookieSpec() {
        return useDefaultCookieSpec;
    }

    public void setUseDefaultCookieSpec(boolean useDefaultCookieSpec) {
        this.useDefaultCookieSpec = useDefaultCookieSpec;
    }

    public boolean isUseLaxRedirectStrategy() {
        return isUseLaxRedirectStrategy;
    }

    public void setUseLaxRedirectStrategy(boolean useLaxRedirectStrategy) {
        isUseLaxRedirectStrategy = useLaxRedirectStrategy;
    }


    public String getProxySiteName() {
        return proxySiteName;
    }

    public void setProxySiteName(String proxySiteName) {
        this.proxySiteName = proxySiteName;
    }

    public static boolean isShutdown() {
        return shutdown;
    }

}
