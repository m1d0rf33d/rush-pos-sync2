package com.rush.service;

import com.rush.model.User;
import com.rush.model.UserHistory;
import com.rush.model.dto.UserDTO;
import com.rush.model.dto.UserHistoryDTO;
import com.rush.model.enums.Activity;
import com.rush.repository.UserHistoryRepository;
import com.rush.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aomine on 5/24/17.
 */
@Service
public class UserService {


    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserHistoryRepository userHistoryRepository;

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

    public List<UserHistoryDTO> getUserHistories() {

        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd");

        List<UserHistory> userHistories = userHistoryRepository.findAll();
        List<UserHistoryDTO> userHistoryDTOs = new ArrayList<>();

        userHistories.forEach(userHistory -> {
            UserHistoryDTO userHistoryDTO = new UserHistoryDTO();
            userHistoryDTO.setUser(userHistory.getUser());
            userHistoryDTO.setActivity(userHistory.getActivity().toString());
            userHistoryDTO.setDate(sdf.format(userHistory.getDateCreated()));
            if (userHistory.getActivity().equals(Activity.CREATE_MERCHANT) || userHistory.getActivity().equals(Activity.UPDATE_MERCHANT)) {
                userHistoryDTO.setDetail(userHistory.getMerchant());
            }

            if (userHistory.getActivity().equals(Activity.UPDATE_BRANCH)) {
                userHistoryDTO.setDetail(userHistory.getBranch());
            }

            if (userHistory.getActivity().equals(Activity.UPDATE_ACCOUNT)) {
                userHistoryDTO.setDetail(userHistory.getAccount());
            }

            if (userHistory.getActivity().equals(Activity.CREATE_ROLE) || userHistory.getActivity().equals(Activity.UPDATE_ROLE)) {
                userHistoryDTO.setDetail(userHistory.getRole());
            }
            userHistoryDTOs.add(userHistoryDTO);
        });
        return userHistoryDTOs;
    }

}
