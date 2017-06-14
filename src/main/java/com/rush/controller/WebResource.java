package com.rush.controller;

import com.rush.model.ApiResponse;
import com.rush.model.dto.BranchDTO;
import com.rush.model.dto.MerchantDTO;
import com.rush.model.dto.RoleDTO;
import com.rush.model.dto.UserDTO;
import com.rush.model.enums.MerchantStatus;
import com.rush.model.enums.Screen;
import com.rush.service.MerchantService;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @RequestMapping(value = "/branch", method = RequestMethod.GET)
    public ResponseEntity<List<BranchDTO>> getBranchesByMerchant(@RequestParam(value = "merchant") Long merchantId) {
        return new ResponseEntity<>(merchantService.getBranches(merchantId).getData(), HttpStatus.OK);
    }

    @RequestMapping(value = "/branch", method = RequestMethod.POST)
    public ResponseEntity<BranchDTO> postBranch(@RequestBody BranchDTO branchDTO) {
        return new ResponseEntity<>(merchantService.updateBranch(branchDTO).getData(), HttpStatus.OK );
    }

    @RequestMapping(value = "/account", method = RequestMethod.GET)
    public ResponseEntity<List<JSONObject>> getAccounts(@RequestParam(value = "merchant") Long merchantId) {
        return new ResponseEntity<>(merchantService.getMerchantAccounts(merchantId).getData(), HttpStatus.OK);
    }
    @RequestMapping(value = "/account", method = RequestMethod.POST)
    public ResponseEntity<UserDTO> postAccount(@RequestBody UserDTO userDTO) {
        return new ResponseEntity<>(merchantService.updateMerchantAccounts(userDTO).getData(), HttpStatus.OK);
    }

    @RequestMapping(value = "/role", method = RequestMethod.GET)
    public ResponseEntity<List<RoleDTO>> getRoles(@RequestParam(value = "merchant") Long merchantId) {
        return new ResponseEntity<>(merchantService.getRoles(merchantId).getData(), HttpStatus.OK);
    }

    @RequestMapping(value = "/role", method = RequestMethod.POST)
    public ResponseEntity<RoleDTO> postRole(@RequestBody RoleDTO roleDTO) {
        return new ResponseEntity<>(merchantService.postRole(roleDTO), HttpStatus.OK);
    }

    @RequestMapping(value = "/screens", method = RequestMethod.GET)
    public ResponseEntity<List<Screen>> getScreens() {
        return new ResponseEntity<>(Arrays.asList(Screen.values()), HttpStatus.OK);
    }
}
