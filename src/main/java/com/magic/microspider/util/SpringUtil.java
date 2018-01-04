package com.magic.microspider.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Created by liunn on 2018/1/4.
 */
@Component
public class SpringUtil implements ApplicationContextAware {

    private static ApplicationContext context;

    private SpringUtil() {
    }

    private static void setContext(ApplicationContext context) {
        SpringUtil.context = context;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        SpringUtil.setContext(applicationContext);
    }

    public static <T> T getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }

    public static Object getBean(String beanName) {
        return context.getBean(beanName);
    }

    public static String getProperty(String key) {
        return context.getEnvironment().getProperty(key);
    }

    public static boolean isProfileActive(String profile) {
        return context.getEnvironment().acceptsProfiles(profile);
    }


}
