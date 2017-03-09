package com.rush.controller;

import com.rush.model.ApiResponse;
import com.rush.model.WidgetResponse;
import com.rush.model.dto.LoginDTO;
import com.rush.service.widget.WidgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by aomine on 3/9/17.
 */
@RestController
public class WidgetController {

    @Autowired
    private WidgetService widgetService;

    @RequestMapping(value = "/initialize/{merchantKey}", method = RequestMethod.GET)
    public WidgetResponse initializeWidget(@PathVariable String merchantKey) {
        return widgetService.initializeWidget(merchantKey);
    }

    @RequestMapping(value = "/api/widget/login/employee", method = RequestMethod.POST)
    public WidgetResponse loginEmployee(@RequestBody LoginDTO loginDTO) {
        return widgetService.loginEmployee(loginDTO);
    }

    

}
