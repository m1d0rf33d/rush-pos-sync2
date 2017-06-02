package com.rush.controller;

import com.rush.model.dto.UserDTO;
import com.rush.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by aomine on 5/24/17.
 */
@RestController
@RequestMapping(value = "/backdoor")
public class BackdoorResource {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public ResponseEntity<UserDTO> postUser(@RequestBody UserDTO userDTO) {

        return new ResponseEntity<>(userService.postUser(userDTO), HttpStatus.OK);
    }

}
