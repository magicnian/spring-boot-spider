package com.magic.microspider.util;

import org.apache.commons.net.telnet.TelnetClient;
import org.apache.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Enumeration;

/**
 * Created by liunn on 2018/1/11.
 */
public class ProxyUtil {

    private static String NET_ADDR;

    private static final int TELNET_TIME_OUT = 3000;

    private static final Logger logger = LoggerFactory.getLogger(ProxyUtil.class);

    static {
        init();
    }

    private static void init() {
        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            outer:
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = netInterfaces.nextElement();
                Enumeration<InetAddress> addrs = netInterface.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    if (!addr.isSiteLocalAddress() && !addr.isLoopbackAddress() && addr.getHostAddress().indexOf(":") == -1) // 外网IP
                    {
                        NET_ADDR = addr.getHostAddress();
                        break outer;
                    }

                }
            }
            if (CommonUtil.isEmptyStr(NET_ADDR)) {
                NET_ADDR = InetAddress.getLocalHost().getHostAddress();
            }
            logger.info("Net Address:" + NET_ADDR);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * telnet ip:port
     *
     * @param ip
     * @param port
     * @return
     */
    public static boolean telnetProxy(String ip, int port) {
        TelnetClient client = new TelnetClient();
        try {
            client.setConnectTimeout(TELNET_TIME_OUT);
            client.connect(ip, port);
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (client.isConnected()) {
                    client.disconnect();
                }
            } catch (IOException ignored) {
            }
        }
    }

    public static boolean validateProxy(HttpHost p) {
        if (NET_ADDR == null) {
            logger.warn("cannot get local IP");
            return false;
        }
        boolean isReachable = false;
        Socket socket = null;
        try {
            socket = new Socket();
            socket.bind(new InetSocketAddress(NET_ADDR, 0));
            InetSocketAddress endpointSocketAddr = new InetSocketAddress(p.getHostName(), p.getPort());
            socket.connect(endpointSocketAddr, 3000);
            logger.info("SUCCESS - connection established! Local: " + NET_ADDR + " remote: " + p);
            isReachable = true;
        } catch (IOException e) {
            logger.warn("FAILRE - CAN not connect! Local: " + NET_ADDR + " remote: " + p);
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    logger.warn("Error occurred while closing socket of validating proxy", e);
                }
            }
        }
        return isReachable;
    }

    /**
     * 根据配置获取代理
     *
     * @param redisCacheHandler
     * @param baseConfig
     * @return
     */
//    public static Proxy getProxyByConfig(RedisCacheHandler redisCacheHandler, BaseConfig baseConfig) {
//        if (1 == baseConfig.getIsUseProxy()) {
//            return getProxyFromRedis(redisCacheHandler);
//        } else {
//            return null;
//        }
//    }

    /**
     * 获取一个可用
     *
     * @param redisCacheHandler
     * @return
     */
//    public static Proxy getProxyFromRedis(RedisCacheHandler redisCacheHandler) {
//        Proxy proxy = null;
//        List<Proxy> proxies = getAllProxyFromRedis(redisCacheHandler, null);
//        if (CollectionUtils.isNotEmpty(proxies)) {
//            int index = new Random().nextInt(proxies.size());
//            proxy = proxies.get(index);
//        }
//
//        return proxy;
//    }

//    public static Proxy getProxyFromRedisForCMCC(RedisCacheHandler redisCacheHandler) {
//        Proxy proxy = null;
//        List<Proxy> proxies = getAllProxyFromRedisForCMCC(redisCacheHandler);
//        if (CollectionUtils.isNotEmpty(proxies)) {
//            int index = new Random().nextInt(proxies.size());
//            proxy = proxies.get(index);
//        }
//
//        return proxy;
//    }

    /**
     * 获取一个可用(根据站点)
     *
     * @param redisCacheHandler
     * @return
     */
//    public static Proxy getProxyFromRedisBySiteName(RedisCacheHandler redisCacheHandler, String siteName) {
//        Proxy proxy = null;
//        List<Proxy> proxies = getAllProxyFromRedis(redisCacheHandler, siteName);
//        if (CollectionUtils.isNotEmpty(proxies)) {
//            int index = new Random().nextInt(proxies.size());
//            proxy = proxies.get(index);
//        }
//
//        return proxy;
//    }

    /**
     * 剔除一个不可用代理(根据站点)
     *
     * @param redisCacheHandler
     * @return
     */
    public static void deleteProxyFromRedisBySiteName(RedisCacheHandler redisCacheHandler, String proxyStr, String siteName) {
        redisCacheHandler.sRemove(siteName, proxyStr);
    }

    /**
     * 获取一个可用
     *
     * @param redisCacheHandler
     * @return
     */
//    public static List<Proxy> getAllProxyFromRedis(RedisCacheHandler redisCacheHandler, String cacheName) {
//        Proxy proxy;
//        List<Proxy> proxys = new ArrayList<>();
//        Collection<String> proxyStrs;
//        if (cacheName != null) {
//            proxyStrs = redisCacheHandler.sMemeber(cacheName, String.class);
//        } else {
//            proxyStrs = redisCacheHandler.get(PROXY.code(), List.class);
//        }
//
//        if (proxyStrs != null)
//            for (String proxyStr : proxyStrs) {
//                String proxyIp = proxyStr.split(":")[0];
//                int proxyPort = Integer.valueOf(proxyStr.split(":")[1]);
////                if (telnetProxy(proxyIp, proxyPort)) {
////
////                }
//                proxy = new Proxy(proxyIp, proxyPort);
//                proxys.add(proxy);
//            }
//        return proxys;
//    }

//    public static List<Proxy> getAllProxyFromRedisForCMCC(RedisCacheHandler redisCacheHandler) {
//        Proxy proxy;
//        List<Proxy> proxys = new ArrayList<>();
//        Collection<String> proxyStrs;
//        proxyStrs = redisCacheHandler.get(CMCC_PROXY.code(), List.class);
//        if (proxyStrs != null)
//            for (String proxyStr : proxyStrs) {
//                String proxyIp = proxyStr.split(":")[0];
//                int proxyPort = Integer.valueOf(proxyStr.split(":")[1]);
//                if (telnetProxy(proxyIp, proxyPort)) {
//                    proxy = new Proxy(proxyIp, proxyPort);
//                    proxys.add(proxy);
//                }
//            }
//        return proxys;
//    }

    /**
     * 将ip:port形式的字符串转换成httphost对象
     *
     * @param proxyStr
     * @return
     */
    public static HttpHost getHttpHostFromString(String proxyStr) {
        String[] proxyArr = proxyStr.split(":");
        String host = proxyArr[0];
        Integer port = Integer.valueOf(proxyArr[1]);
        return new HttpHost(host, port);
    }

}
