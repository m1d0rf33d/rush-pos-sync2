package com.rush.controller;

import com.rush.model.ApiResponse;
import com.rush.model.dto.MerchantDTO;
import com.rush.model.enums.MerchantStatus;
import com.rush.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;


/**
 * Created by aomine on 6/13/17.
 */
@RestController
public class WebResource {

    @Autowired
    private MerchantService merchantService;

    @RequestMapping(value = "/merchant", method = RequestMethod.GET)
    public ResponseEntity<List<MerchantDTO>> getMerchants() {
        return new ResponseEntity<>(merchantService.getMerchants().getData(), HttpStatus.OK);
    }

    @RequestMapping(value = "/merchant", method = RequestMethod.POST)
    public ResponseEntity<MerchantDTO> postMerchant(@RequestBody MerchantDTO merchantDTO) {
        return new ResponseEntity<>(merchantService.updateMerchant(merchantDTO).getData(), HttpStatus.OK);
    }

    @RequestMapping(value = "/merchant/status", method = RequestMethod.GET)
    public ResponseEntity<List<MerchantStatus>> getMerchantStatuses() {
        return new ResponseEntity<>(Arrays.asList(MerchantStatus.values()), HttpStatus.OK);
    }

}
