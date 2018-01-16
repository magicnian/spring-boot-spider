package com.magic.microspider.springbootspider.base.httpclient;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;

/**
 *
 * Created by liunn on 2018/1/16.
 */
public class HttpRequestRetryHandlerImpl implements HttpRequestRetryHandler {

    private static Logger LOGGER = LoggerFactory.getLogger(HttpRequestRetryHandlerImpl.class);

    private int retryNum;

    public HttpRequestRetryHandlerImpl(int retryNum)
    {
        this.retryNum = retryNum;
    }

    @Override
    public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
        HttpClientContext clientContext = HttpClientContext.adapt(context);
        HttpRequest request = clientContext.getRequest();
        if (executionCount >= retryNum)// 重试次数
        {
            return false;
        }
        if (exception instanceof NoHttpResponseException)// 如果服务器丢掉了连接，那么就重试
        {
            return true;
        }
        if (exception instanceof SSLHandshakeException)// 重试SSL握手异常
        {
            return true;
        }
        if (exception instanceof InterruptedIOException)// 超时
        {
            return true;
        }
        if (exception instanceof UnknownHostException)// 目标服务器不可达
        {
            return true;
        }
        if (exception instanceof ConnectTimeoutException)// 连接被拒绝
        {
            return true;
        }
        if (exception instanceof SSLException)// SSL握手异常
        {
            return true;
        }
        if (!(request instanceof HttpEntityEnclosingRequest))// 如果请求是幂等的，就再次尝试
        {
            LOGGER.warn(request.getRequestLine().getUri() + " retry again," + " executionCount:" + executionCount + ",retryNum:" + retryNum);
            return true;
        }
        return false;
    }
}
