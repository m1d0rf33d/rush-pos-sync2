package com.rush.service;

import com.rush.model.User;
import com.rush.model.UserHistory;
import com.rush.model.dto.BranchDTO;
import com.rush.model.dto.MerchantDTO;
import com.rush.model.dto.RoleDTO;
import com.rush.model.dto.UserDTO;
import com.rush.model.enums.Activity;
import com.rush.repository.UserHistoryRepository;
import com.rush.repository.UserRepository;
import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;

/**
 * Created by aomine on 6/16/17.
 */
@Aspect
@Service
public class LoggingService {

    private static final Logger LOG = Logger.getLogger(LoggingService.class);

    @Autowired
    private UserHistoryRepository userHistoryRepository;
    @Autowired
    private UserRepository userRepository;

    @Pointcut("within(com.rush.controller.WidgetController)")
    public void widgetPointCut() {

    }

    @After("widgetPointCut()")
    public void logWidgetAccess(JoinPoint joinPoint) {
       try {
           LOG.info("Entered -> " + joinPoint.getSignature().getName());
           StringBuilder sb = new StringBuilder();
           sb.append("Arguments -> ");
           Object[] args = joinPoint.getArgs();
           Arrays.asList(args).forEach(arg -> {
               sb.append(arg);
           });
           LOG.info(sb.toString());
       } catch (Throwable throwable) {
           throwable.printStackTrace();
       }
    }

    @Pointcut("within(com.rush.controller.WebResource)")
    public void webResourcePointcut() {

    }

    @After("webResourcePointcut()")
    public void persistHistory(JoinPoint joinPoint) {

        Signature signature = joinPoint.getSignature();
        String sigName = signature.getName();
        if (sigName.contains("postMerchant") || sigName.contains("postBranch") ||
                sigName.contains("postAccount") || sigName.contains("postRole")) {
            UserHistory userHistory = new UserHistory();
            Activity activity = null;
            String merchant = null;
            String branch = null;
            String account = null;
            String role = null;


            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String user = userDetails.getUsername();

            if (sigName.contains("postMerchant")) {
                MerchantDTO merchantDTO = (MerchantDTO) joinPoint.getArgs()[0];
                if (merchantDTO.getId() == null) {
                    activity = Activity.CREATE_MERCHANT;
                } else {
                    activity = Activity.UPDATE_MERCHANT;
                }
                merchant = merchantDTO.getName();
            }

            if (sigName.contains("postBranch")) {
                BranchDTO branchDTO = (BranchDTO) joinPoint.getArgs()[0];
                activity = Activity.UPDATE_BRANCH;
                branch = branchDTO.getBranchName();
            }

            if (sigName.contains("postAccount")) {
                UserDTO userDTO = (UserDTO) joinPoint.getArgs()[0];
                User acc = userRepository.findOneByUuid(userDTO.getUuid());
                activity = Activity.UPDATE_ACCOUNT;
                account = acc.getName();
            }

            if (sigName.contains("postRole")) {
                RoleDTO roleDTO = (RoleDTO) joinPoint.getArgs()[0];
                role = roleDTO.getName();
                if (roleDTO.getRoleId() == null) {
                    activity = Activity.CREATE_ROLE;
                } else {
                    activity = Activity.UPDATE_ROLE;
                }

            }

            userHistory.setDateCreated(new Date());
            userHistory.setRole(role);
            userHistory.setAccount(account);
            userHistory.setBranch(branch);
            userHistory.setActivity(activity);
            userHistory.setMerchant(merchant);
            userHistory.setUser(user);
            userHistoryRepository.save(userHistory);
        }

    }


}
