package com.rush.controller;

import org.omg.CORBA.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by aomine on 11/23/16.
 */
@Controller
@PropertySource("classpath:api.properties")
public class TestController {

    @Autowired
    private Environment env;

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public void test() {
        String x = env.getProperty("base.url");
        String y = "";
    }



}
