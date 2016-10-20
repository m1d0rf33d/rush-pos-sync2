package com.rush.controller;

import com.rush.model.MerchantDTO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by aomine on 10/20/16.
 */
@Controller
public class MerchantResource {

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String index() {
        return "index";
    }

    @ResponseBody
    @RequestMapping(value = "/api/merchant/", method = RequestMethod.GET)
    public MerchantDTO getMerchants() {
        MerchantDTO merchantDTO = new MerchantDTO();
        merchantDTO.setName("TAF");
        merchantDTO.setId("1");
        return merchantDTO;
    }

}
