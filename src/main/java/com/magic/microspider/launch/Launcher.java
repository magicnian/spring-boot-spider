package com.magic.microspider.launch;

import com.magic.microspider.springbootspider.base.httpclient.HttpConnectionMonitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * 启动器
 * Created by liunn on 2018/1/11.
 */
@Configuration
@Slf4j
public class Launcher implements ApplicationListener<ContextRefreshedEvent> {

    private static ApplicationContext context;

    private static HttpConnectionMonitor httpMonitor;


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

    }
}
