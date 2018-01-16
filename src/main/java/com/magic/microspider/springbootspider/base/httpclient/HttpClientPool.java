package com.magic.microspider.springbootspider.base.httpclient;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.magic.microspider.springbootspider.base.bean.engine.Proxy;
import com.magic.microspider.springbootspider.base.bean.engine.Request;
import com.magic.microspider.springbootspider.base.bean.engine.Site;
import com.magic.microspider.springbootspider.base.constants.SpiderConts;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局HttpClient池管理
 * Created by liunn on 2018/1/16.
 */
public class HttpClientPool {

    private static Logger logger = LoggerFactory.getLogger(HttpClientPool.class);

    /**
     * 站点与HttpClient映射,key:Site,value:CloseableHttpClient
     */
    private static Map<String, CloseableHttpClient> SITE_HTTP_CLIENT_MAP = new ConcurrentHashMap<>();

    /**
     * 站点PoolingHttpClientConnectionManager管理器
     */
    private static PoolingHttpClientConnectionManager manager;

    static {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (arg0, arg1) -> true).build();
            HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
            Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.INSTANCE).register("https", sslSocketFactory)
                    .build();
            manager = new PoolingHttpClientConnectionManager(reg);
            //handshake timeout，采用默认值60s
            manager.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(60000).build());
            manager.setDefaultMaxPerRoute(200);
            manager.setMaxTotal(200);
        } catch (Exception e) {
            logger.error("warn happened init PoolingHttpClientConnectionManager ", e);
        }
    }


    /**
     * 根据site生成httpclient
     *
     * @param site
     * @return
     */
    private static synchronized CloseableHttpClient generateClient(Site site) {
        // 请求重试处理
        HttpRequestRetryHandler retryHandler = new HttpRequestRetryHandlerImpl(site.getRetryTimes());
        HttpClientBuilder clientBuilder = HttpClients.custom().setConnectionManager(manager).setRetryHandler(retryHandler).setKeepAliveStrategy(new HttpClientKeepAliveStrategy());

        if (site.getUserAgent() != null) {
            clientBuilder.setUserAgent(site.getUserAgent());
        }

        if (site.useGzip()) {
            clientBuilder.addInterceptorFirst((HttpRequestInterceptor) (request, context) -> {
                if (!request.containsHeader("Accept-Encoding")) {
                    request.addHeader("Accept-Encoding", "gzip");
                }
            });
        }

        if (site.isUseLaxRedirectStrategy()) {
            clientBuilder.setRedirectStrategy(new LaxRedirectStrategy());
        }

        CloseableHttpClient client = clientBuilder.build();
        SITE_HTTP_CLIENT_MAP.put(site.getSiteDomain(), client);

        return client;
    }

    /**
     * 获取所有的连接池管理器
     *
     * @return
     */
    public static PoolingHttpClientConnectionManager getConnMgr() {
        return manager;
    }

    /**
     * 每次请求的请求参数设置
     * 部分站点需要对每次请求做定制化
     * 如新浪微博，赶集等
     *
     * @param request
     * @param site
     * @param proxy
     * @return
     */
    public static HttpUriRequest httpRequestProvider(Request request, Site site, Proxy proxy) {
        RequestBuilder reqBuilder = selectRequestMethod(request).setUri(request.getUrl());

        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        requestConfigBuilder.setConnectionRequestTimeout(site.getTimeoutMs()).setSocketTimeout(site.getTimeoutMs()).setConnectTimeout(site.getTimeoutMs());
        requestConfigBuilder.setCookieSpec(site.isUseDefaultCookieSpec() ? CookieSpecs.STANDARD_STRICT : CookieSpecs.IGNORE_COOKIES);

        if (!site.isAutoRedirect()) {
            requestConfigBuilder.setRedirectsEnabled(false);
        }

        if (site.useProxy() && request.isUseProxy()) {
            requestConfigBuilder.setProxy(new HttpHost(proxy.getHost(), proxy.getPort()));
        }

        reqBuilder.setConfig(requestConfigBuilder.build());

        Map<String, String> headers = site.getHeaderMap();
        if (headers != null) {
            for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
                reqBuilder.addHeader(headerEntry.getKey(), headerEntry.getValue());
            }

            // added by wudj@fenqi.im start
            Object headerObj = request.getExtra(SpiderConts.HEADER);
            if (headerObj != null) {
                if (headerObj instanceof Map) {
                    Map header = (Map) headerObj;
                    for (Object key : header.keySet()) {
                        String keyStr = (String) key;
                        reqBuilder.removeHeaders(keyStr);
                        reqBuilder.addHeader(keyStr, (String) header.get(keyStr));
                    }
                }
            }
            // added by wudj@fenqi.im end
        }
        return reqBuilder.build();
    }



    private static RequestBuilder selectRequestMethod(Request request) {
        String method = request.getMethod();
        if (method == null || method.equalsIgnoreCase(HttpConstant.Method.GET)) {
            return RequestBuilder.get();
        } else if (method.equalsIgnoreCase(HttpConstant.Method.POST)) {
            RequestBuilder requestBuilder = RequestBuilder.post();

            // FIX IF PARAMETER IS JUST A RAW JSON. <wudj@fenqi.im>
            String rawJsonString = (String) request.getExtra(SpiderConts.RAWJSON);
            if (StringUtils.isNotEmpty(rawJsonString)) {
                StringEntity entity = new StringEntity(rawJsonString, ContentType.APPLICATION_JSON);
                requestBuilder.setEntity(entity);
                return requestBuilder;
            }

            JSONArray nameValuePairs = (JSONArray) request.getExtra(SpiderConts.NAMEVALUEPAIRS);
            if (nameValuePairs.isEmpty()) {
                return requestBuilder;
            }

            for (int i = 0; i < nameValuePairs.size(); i++) {
                JSONObject nameValuePair = nameValuePairs.getJSONObject(i);
                requestBuilder.addParameter(nameValuePair.getString("name"), nameValuePair.getString("value"));
            }

            return requestBuilder;
        } else if (method.equalsIgnoreCase(HttpConstant.Method.HEAD)) {
            return RequestBuilder.head();
        } else if (method.equalsIgnoreCase(HttpConstant.Method.PUT)) {
            return RequestBuilder.put();
        } else if (method.equalsIgnoreCase(HttpConstant.Method.DELETE)) {
            return RequestBuilder.delete();
        } else if (method.equalsIgnoreCase(HttpConstant.Method.TRACE)) {
            return RequestBuilder.trace();
        }
        throw new IllegalArgumentException("Illegal HTTP Method " + method);
    }

}
