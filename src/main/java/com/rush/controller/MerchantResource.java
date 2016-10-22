package com.rush.controller;

import com.rush.model.ApiResponse;
import com.rush.model.MerchantDTO;
import com.rush.model.RoleDTO;
import com.rush.model.UserDTO;
import com.rush.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Created by aomine on 10/20/16.
 */
@RestController
@RequestMapping(value = "/api/merchant")
public class MerchantResource {

    @Autowired
    private MerchantService merchantService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ApiResponse getMerchants() {
        return merchantService.getMerchants();
    }


    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ApiResponse saveMerchant(@RequestBody MerchantDTO merchantDTO) {
        return merchantService.updateMerchant(merchantDTO);
    }

    @RequestMapping(value = "/validate", method = RequestMethod.POST)
    public ApiResponse validateMerchant(@RequestBody MerchantDTO merchantDTO) {
        return merchantService.validateMerchant(merchantDTO.getUniqueKey());
    }

    @RequestMapping(value = "/{merchantId}/accounts", method = RequestMethod.GET)
    public ApiResponse getMerchantAccounts(@PathVariable long merchantId) {
        return merchantService.getMerchantAccounts(merchantId);
    }
    @RequestMapping(value = "/accounts/update", method = RequestMethod.POST)
    public ApiResponse updateMerchantAccounts(@RequestBody UserDTO userDTO) {
        return merchantService.updateMerchantAccounts(userDTO);
    }

    @RequestMapping(value = "/{merchantId}/screens", method = RequestMethod.GET)
    public ApiResponse getScreens(@PathVariable Long merchantId) {
        return merchantService.getScreens(merchantId);
    }

    @RequestMapping(value = "/access/update", method = RequestMethod.POST)
    public ApiResponse accessUpdate(@RequestBody RoleDTO roleDTO) {
        return merchantService.updateRoleAccess(roleDTO);
    }
}
