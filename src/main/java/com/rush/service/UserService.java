package com.rush.service;

import com.rush.model.User;
import com.rush.model.dto.UserDTO;
import com.rush.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Created by aomine on 5/24/17.
 */
@Service
public class UserService {


    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserDTO postUser(UserDTO userDTO) {

        if (userDTO != null) {

            User user = new User();
            if (userDTO.getUserId() != null) {
                user = userRepository.findOne(userDTO.getUserId());
            }

            user.setUsername(userDTO.getUsername());
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            userRepository.save(user);
            return userDTO;
         }

        return null;

    }

}
