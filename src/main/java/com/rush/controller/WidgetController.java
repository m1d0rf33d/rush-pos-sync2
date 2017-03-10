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
    public JSONObject loginMember(@RequestBody JSONObject jsonObject) {
        return memberService.loginMember(jsonObject);
    }

    @RequestMapping(value = "/api/widget/register", method = RequestMethod.POST)
    public JSONObject registerMember(@RequestBody JSONObject jsonObject) {

        return memberService.registerMember(jsonObject);
    }


}
