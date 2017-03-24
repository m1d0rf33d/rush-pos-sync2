package com.rush.controller;

import com.rush.model.ApiResponse;
import com.rush.model.WidgetResponse;
import com.rush.model.dto.LoginDTO;
import com.rush.model.dto.LoginMemberDTO;
import com.rush.service.widget.MemberService;
import com.rush.service.widget.WidgetService;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by aomine on 3/9/17.
 */
@RestController
public class WidgetController {

    @Autowired
    private WidgetService widgetService;
    @Autowired
    private MemberService memberService;

    @RequestMapping(value = "/initialize/{merchantKey}", method = RequestMethod.GET)
    public JSONObject initializeWidget(@PathVariable String merchantKey) {
        return widgetService.initializeWidget(merchantKey);
    }

    @RequestMapping(value = "/api/widget/login/employee", method = RequestMethod.POST)
    public JSONObject loginEmployee(@RequestBody JSONObject requestBody) {
        return widgetService.loginEmployee(requestBody);
    }

    @RequestMapping(value = "/api/widget/login/member", method = RequestMethod.POST)
    public JSONObject loginMember(@RequestBody JSONObject requestBody) {
        return memberService.loginMember(requestBody);
    }

    @RequestMapping(value = "/api/widget/register", method = RequestMethod.POST)
    public JSONObject registerMember(@RequestBody JSONObject requestBody) {
        return memberService.registerMember(requestBody);
    }

    @RequestMapping(value = "/api/widget/earn", method = RequestMethod.POST)
    public JSONObject earnPoints(@RequestBody JSONObject requestBody) {
        return memberService.earnPoints(requestBody);
    }

    @RequestMapping(value = "/api/widget/guestPurchase", method = RequestMethod.POST)
    public JSONObject guestPurchase(@RequestBody JSONObject requestBody) {
        return memberService.guestPurchase(requestBody);
    }

    @RequestMapping(value = "/api/widget/redeem", method = RequestMethod.POST)
    public JSONObject redeemReward (@RequestBody  JSONObject requestBody) {
        return memberService.redeemReward(requestBody);
    }

    @RequestMapping(value = "/api/widget/payWithPoints", method = RequestMethod.POST)
    public JSONObject payWithPoints(@RequestBody JSONObject requestBody) {
        return memberService.payWithPoints(requestBody);
    }

    @RequestMapping(value = "/api/widget/issueReward", method = RequestMethod.POST)
    public JSONObject issueReward(@RequestBody JSONObject requestBody) {
        return memberService.issueReward(requestBody);
    }
    @RequestMapping(value = "/api/widget/sendOffline", method = RequestMethod.POST)
    public JSONObject sendOfflineTransactions(@RequestBody JSONObject requestBody) {
        return memberService.sendOfflineTransacions(requestBody);
    }

    @RequestMapping(value = "/api/widget/earnStamp", method = RequestMethod.POST)
    public JSONObject earnStamp(@RequestBody JSONObject requestBody) {
        return memberService.earnStamp(requestBody);
    }

    @RequestMapping(value = "/api/widget/redeemStamp", method = RequestMethod.POST)
    public JSONObject redeemStamp(@RequestBody JSONObject requestBody) {
        return memberService.redeemStamp(requestBody);
    }

    @RequestMapping(value = "/api/widget/issueStampReward", method = RequestMethod.POST)
    public JSONObject issueStampReward(@RequestBody JSONObject requestBody) {
        return memberService.issueStampReward(requestBody);
    }
}
