package com.rush.service;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.util.Arrays;

/**
 * Created by aomine on 6/16/17.
 */
@Aspect
public class LoggingService {

    private static final Logger LOG = Logger.getLogger(LoggingService.class);


    @Pointcut("within(com.rush.controller.WidgetController)")
    public void widgetPointCut() {

    }

    @After("widgetPointCut()")
    public void logWidgetAccess(JoinPoint joinPoint) {
       try {
           LOG.info("Entered -> " + joinPoint.getSignature().getName());
           StringBuilder sb = new StringBuilder();
           sb.append("Arguments -> ");
           Object[] args = joinPoint.getArgs();
           Arrays.asList(args).forEach(arg -> {
               sb.append(arg);
           });
           LOG.info(sb.toString());
       } catch (Throwable throwable) {
           throwable.printStackTrace();
       }
    }



}
