package com.rush.service;

import org.apache.log4j.Logger;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * Created by aomine on 6/21/17.
 */
@Aspect
public class ErrorLogger {

    private static final Logger LOG = Logger.getLogger(ErrorLogger.class);


    @Pointcut("within(com.rush.controller.WidgetController)")
    public void widgetPointCut() {

    }

    @AfterThrowing(value = "execution(* com.rush.controller.WidgetController.*(..))", throwing = "ex")
    public void logAfterThrowingError(Exception ex) throws Throwable {
        StackTraceElement[] err = ex.getStackTrace();
        LOG.info(ex.toString());
        LOG.info(err[0].toString());
    }


}
