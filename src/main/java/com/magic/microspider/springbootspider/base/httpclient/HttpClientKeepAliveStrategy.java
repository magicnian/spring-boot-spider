package com.magic.microspider.springbootspider.base.httpclient;

import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HttpContext;

/**
 * Created by liunn on 2018/1/16.
 */
public class HttpClientKeepAliveStrategy implements ConnectionKeepAliveStrategy {

    private int keepAliveTimeOut = 5;

    public HttpClientKeepAliveStrategy() {
    }


    @Override
    public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
        BasicHeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator("Keep-Alive"));
        while (true) {
            String param;
            String value;
            do {
                do {
                    if (!it.hasNext()) {
                        return (long) (this.keepAliveTimeOut * 1000);
                    }

                    HeaderElement he = it.nextElement();
                    param = he.getName();
                    value = he.getValue();
                } while (value == null);
            } while (!param.equalsIgnoreCase("timeout"));

            try {
                return Long.parseLong(value) * 1000L;
            } catch (NumberFormatException var8) {
                ;
            }
        }
    }

    public void setKeepAliveTimeOut(int keepAliveTimeOut) {
        this.keepAliveTimeOut = keepAliveTimeOut;
    }
}
