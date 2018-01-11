package com.magic.microspider.springbootspider.base.bean.engine;

import com.magic.microspider.springbootspider.base.enums.ProxyStatus;
import com.magic.microspider.util.annotation.ParamValidator;

import java.io.Serializable;

/**
 * Created by liunn on 2018/1/11.
 */
public class BaseProxy implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -6193075736722714606L;

    protected String proxyId;

    @ParamValidator(required = true)
    protected String host;

    @ParamValidator(required = true, isUnsignInt = true)
    protected Integer port;

    protected String authName;

    protected String authPwd;

    @ParamValidator(required = true)
    protected Integer proxyType;// 代理类型

    protected Integer isRemoved = ProxyStatus.IS_ACTIVE.code();

    protected Float speed = 0f;// 速度

    protected Long updateTime;// 更新时间

    protected String srcCode;// 来源编码

    public String getKey() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getHost())
                .append(":")
                .append(this.getPort());
        return builder.toString();
    }

    @Override
    public int hashCode() {
        return this.getKey().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (obj instanceof Proxy) {
            Proxy other = (Proxy) obj;
            return other.getKey().equals(this.getKey());
        }
        return false;
    }

    public String getProxyId() {
        return proxyId;
    }

    public void setProxyId(String proxyId) {
        this.proxyId = proxyId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getAuthName() {
        return authName;
    }

    public void setAuthName(String authName) {
        this.authName = authName;
    }

    public String getAuthPwd() {
        return authPwd;
    }

    public void setAuthPwd(String authPwd) {
        this.authPwd = authPwd;
    }

    public Integer getProxyType() {
        return proxyType;
    }

    public void setProxyType(Integer proxyType) {
        this.proxyType = proxyType;
    }

    public Integer getIsRemoved() {
        return isRemoved;
    }

    public void setIsRemoved(Integer isRemoved) {
        this.isRemoved = isRemoved;
    }

    public Float getSpeed() {
        return speed;
    }

    public void setSpeed(Float speed) {
        this.speed = speed;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public String getSrcCode() {
        return srcCode;
    }

    public void setSrcCode(String srcCode) {
        this.srcCode = srcCode;
    }
}
