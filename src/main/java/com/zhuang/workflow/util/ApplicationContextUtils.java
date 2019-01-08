package com.zhuang.workflow.util;


import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ApplicationContextUtils {

    private volatile static ApplicationContext applicationContext;

    public static ApplicationContext getApplicationContext() {

        if (applicationContext == null) {
            synchronized (ApplicationContextUtils.class) {
                if (applicationContext == null) {
                    applicationContext = new ClassPathXmlApplicationContext(new String[]{"spring/applicationContext.xml"});
                }
            }
        }

        return applicationContext;
    }

    public static void setApplicationContext(ApplicationContext applicationContext) {
        ApplicationContextUtils.applicationContext = applicationContext;
    }
}
