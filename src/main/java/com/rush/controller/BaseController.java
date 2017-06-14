package com.rush.controller;

import org.omg.CORBA.Request;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by aomine on 6/13/17.
 */
@Controller
public class BaseController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String goToIndexPage() {

        return "index";
    }


    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String goToLoginPage() {

        return "login";
    }
}
