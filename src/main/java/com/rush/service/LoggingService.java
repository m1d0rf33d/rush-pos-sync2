package com.rush.service;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

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

    @Around("widgetPointCut()")
    public void logWidgetAccess(ProceedingJoinPoint joinPoint) {
       try {
           LOG.info("Entered -> " + joinPoint.getSignature().getName());
           LOG.info("Arguments -> ");
           Object[] args = joinPoint.getArgs();
           LOG.info(String.valueOf(args));
           LOG.info("Response -> ");
    //       LOG.info(String.valueOf(joinPoint.proceed()));
           joinPoint.proceed();
       } catch (Throwable throwable) {
           throwable.printStackTrace();
       }
    }



}
