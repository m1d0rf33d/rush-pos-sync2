package com.rush.service;

import com.rush.model.*;
import com.rush.model.dto.*;
import com.rush.model.enums.MerchantClassification;
import com.rush.model.enums.MerchantStatus;
import com.rush.model.enums.RushTokenType;
import com.rush.model.enums.Screen;
import com.rush.repository.*;
import com.rush.service.widget.TokenService;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by aomine on 10/18/16.
 */
@Service
@PropertySource("classpath:api.properties")
public class MerchantService {

    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private APIService apiService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private MerchantScreenRepository merchantScreenRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private BranchRepository branchRepository;

    @Value("${rush.host}")
    private String rushHost;
    @Value("${merchant.employees.endpoint}")
    private String merchantEmployeesEndpoint;
    @Value("${rush.auth.endpoint}")
    private String authorizationEndpoint;
    @Value("${branches.endpoint}")
    private String branchesEndpoint;
    @Value("${sg.auth.endpoint}")
    private String sgAuthEndpoint;
    @Value("${sg.merchant.employees.endpoint}")
    private String sgMerchantEmployeesEndpoint;
    @Value("${sg.branches.endpoint}")
    private String sgBranchesEndpoint;

    @Autowired
    private TokenService tokenService;


    public ApiResponse<List<MerchantDTO>> getMerchants() {
        List<MerchantDTO> data = new ArrayList<>();
        List<Merchant> merchants = merchantRepository.findAll();
        merchants.stream()
                .forEach(merchant -> {
                    data.add(convertToDTO(merchant));
                });
        ApiResponse<List<MerchantDTO>> apiResponse = new ApiResponse<>();
        apiResponse.setData(data);
        apiResponse.setResponseCode("200");
        return apiResponse;
    }

    public ApiResponse updateMerchant(MerchantDTO merchantDTO) {
        Merchant merchant;
        if (merchantDTO.getId() == null) {
            merchant= new Merchant();
            merchantDTO.setStatus(MerchantStatus.ACTIVE.toString());

        } else {
            merchant = merchantRepository.findOne(merchantDTO.getId());
        }
        merchant.setName(merchantDTO.getName());
        merchant.setUniqueKey(merchantDTO.getMerchantApiKey());
        merchant.setMerchantApiKey(merchantDTO.getMerchantApiKey());
        merchant.setMerchantApiSecret(merchantDTO.getMerchantApiSecret());
        merchant.setCustomerApiKey(merchantDTO.getCustomerApiKey());
        merchant.setCustomerApiSecret(merchantDTO.getCustomerApiSecret());
        if (merchantDTO.getStatus().equals("ACTIVE")) {
            merchant.setStatus(MerchantStatus.ACTIVE);
        } else {
            merchant.setStatus(MerchantStatus.INACTIVE);
        }
        merchantRepository.save(merchant);

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setResponseCode("200");
        return apiResponse;
    }

    private MerchantDTO convertToDTO(Merchant merchant) {
        MerchantDTO merchantDTO = new MerchantDTO();
        merchantDTO.setId(merchant.getId());
        merchantDTO.setName(merchant.getName());
        merchantDTO.setStatus(merchant.getStatus().toString());
        merchantDTO.setMerchantApiKey(merchant.getMerchantApiKey());
        merchantDTO.setMerchantApiSecret(merchant.getMerchantApiSecret());
        merchantDTO.setCustomerApiKey(merchant.getCustomerApiKey());
        merchantDTO.setCustomerApiSecret(merchant.getCustomerApiSecret());
        return merchantDTO;
    }

    public ApiResponse validateMerchant(String merchantKey) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setResponseCode("200");
        Merchant merchant = merchantRepository.findOneByUniqueKeyAndStatus(merchantKey, MerchantStatus.ACTIVE);
        if (merchant != null) {
            apiResponse.setData(merchant);
        } else {
          apiResponse.setResponseCode("300");
        }
        return apiResponse;
    }

    public ApiResponse getMerchantAccounts(Long id) {
        ApiResponse apiResponse = new ApiResponse();
        try {
            Merchant merchant = merchantRepository.findOne(id);
                if (merchant != null) {
                String token = tokenService.getRushtoken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());

                String endpoint;
                if (merchant.getMerchantClassification().equals(MerchantClassification.GLOBE_SG)) {
                    endpoint = sgMerchantEmployeesEndpoint;
                } else {
                    endpoint = merchantEmployeesEndpoint;
                }
                String url = rushHost + endpoint;
                JSONObject jsonObj = apiService.call(url, null, "GET", token);
                if (jsonObj == null) {
                    tokenService.refreshToken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                    return getMerchantAccounts(id);
                }

                List<JSONObject> accounts = (ArrayList) jsonObj.get("data");
                for (JSONObject account : accounts) {
                    String uuid = (String) account.get("uuid");
                    User user = userRepository.findOneByUuid(uuid);
                    if (user != null) {
                        List<UserRole> userRoles = userRoleRepository.findByUser(user);
                        userRoles.stream()
                                .forEach(ur-> {
                                    account.put("roleId", ur.getRole().getId());
                                    account.put("role", ur.getRole().getName());
                                });
                    }
                }
                apiResponse.setData(accounts);
                apiResponse.setResponseCode("200");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return apiResponse;
    }

    public ApiResponse updateMerchantAccounts(UserDTO userDTO) {
        User user = userRepository.findOneByUuid(userDTO.getUuid());
        if (user == null) {
            user = new User();
        } else {
            //clear user roles
            List<UserRole> userRoles = userRoleRepository.findByUser(user);
            for (UserRole ur : userRoles) {
                userRoleRepository.delete(ur);
            }
        }
        user.setName(userDTO.getName());
        user.setUuid(userDTO.getUuid());
        user = userRepository.save(user);
        Role role = roleRepository.findOne(userDTO.getRoleId());
        UserRole userRole = new UserRole();
        userRole.setRole(role);
        userRole.setUser(user);
        userRoleRepository.save(userRole);

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setResponseCode("200");
        return apiResponse;
    }

    public ApiResponse getScreens(Long merchantId) {

        List<RoleDTO> roleDTOs = new ArrayList<>();
        Merchant merchant = merchantRepository.findOne(merchantId);
        List<Role> roles = roleRepository.findByMerchant(merchant);
        roles.forEach(role -> {
            RoleDTO roleDTO = new RoleDTO();
            List<ScreenDTO> screens = new ArrayList<>();
            roleDTO.setName(role.getName());
            List<MerchantScreen> merchantScreens = merchantScreenRepository.findByRoleAndMerchant(role, merchant);

            for (int x=0; x < Screen.values().length; x++) {
                String scr = Screen.values()[x].toString();
                ScreenDTO screenDTO = new ScreenDTO();
                screenDTO.setName(Screen.values()[x].toString());
                screenDTO.setChecked(false);
                for (MerchantScreen merchantScreen : merchantScreens) {
                    if (merchantScreen.getScreen().toString().equals(scr)) {
                        screenDTO.setChecked(true);
                    }
                }
                screens.add(screenDTO);
            }
            roleDTO.setMerchantId(merchantId);
            roleDTO.setRoleId(role.getId());
            roleDTO.setScreens(screens);
            roleDTOs.add(roleDTO);
        });
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("roleDTOs", roleDTOs);
        jsonObject.put("screens", Screen.values());

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setResponseCode("200");
        apiResponse.setData(jsonObject);
        return apiResponse;
    }

    public ApiResponse updateRoleAccess(RoleDTO roleDTO) {

        Merchant merchant = merchantRepository.findOne(roleDTO.getMerchantId());
        Role role = roleRepository.findOne(roleDTO.getRoleId());

        List<MerchantScreen> merchantScreens = merchantScreenRepository.findByRoleAndMerchant(role, merchant);
        for (MerchantScreen merchantScreen : merchantScreens) {
            merchantScreenRepository.delete(merchantScreen);
        }

        for (ScreenDTO screenDTO : roleDTO.getScreens()) {
            if (screenDTO.getChecked()) {
                MerchantScreen merchantScreen = new MerchantScreen();
                merchantScreen.setMerchant(merchant);
                merchantScreen.setRole(role);
                merchantScreen.setScreen(Screen.valueOf(screenDTO.getName()));
                merchantScreenRepository.save(merchantScreen);
            }
        }

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setResponseCode("200");
        return apiResponse;
    }

    public  ApiResponse getRoles(long merchantId) {
        Merchant merchant = merchantRepository.findOne(merchantId);
        List<Role> roles = roleRepository.findByMerchant(merchant);

        List<RoleDTO> roleDTOs = new ArrayList<>();
        roles.stream()
                .forEach(role -> {
                    RoleDTO roleDTO = new RoleDTO();
                    roleDTO.setName(role.getName());
                    roleDTO.setRoleId(role.getId());
                    roleDTO.setMerchantId(role.getMerchant().getId());
                    roleDTOs.add(roleDTO);
                });
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setResponseCode("200");
        apiResponse.setData(roleDTOs);
        return apiResponse;
    }

    public ApiResponse updateRole(RoleDTO roleDTO) {
        Role role;
        if (roleDTO.getRoleId() == null) {
            role = new Role();
        } else {
            role = roleRepository.findOne(roleDTO.getRoleId());
        }
        Merchant merchant = merchantRepository.findOne(roleDTO.getMerchantId());
        role.setMerchant(merchant);
        role.setName(roleDTO.getName());
        roleRepository.save(role);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setResponseCode("200");
        return apiResponse;
    }

    public ApiResponse deleteRole(RoleDTO roleDTO) {

        Merchant merchant = merchantRepository.findOne(roleDTO.getMerchantId());
        Role role = roleRepository.findOne(roleDTO.getRoleId());
        List<UserRole> userRoles = userRoleRepository.findByRole(role);
        userRoles.stream()
                .forEach(ur-> {
                    userRoleRepository.delete(ur);
                });
        List<MerchantScreen> merchantScreens = merchantScreenRepository.findByRoleAndMerchant(role, merchant);
        merchantScreens.stream()
                .forEach(ms -> {
                    merchantScreenRepository.delete(ms);
                });

        roleRepository.delete(role);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setResponseCode("200");
        return apiResponse;
    }

    public ApiResponse getAccountAccess(String uuid, String branchUuid){
        List<String> access = new ArrayList<>();
        User user = userRepository.findOneByUuid(uuid);
        if (user != null) {
            List<UserRole> userRoles = userRoleRepository.findByUser(user);
            userRoles.stream()
                    .forEach(ur-> {
                        List<MerchantScreen> merchantScreens = merchantScreenRepository.findByRole(ur.getRole());
                        merchantScreens.stream()
                                .forEach(ms -> {
                                    access.add(ms.getScreen().toString());
                                });
                    });
        }
        boolean withVk = false;
        Branch branch = branchRepository.findOneByUuid(branchUuid);
        if (branch != null) {
            withVk = branch.isWithVk();
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("access", access);
        jsonObject.put("withVk", withVk);

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setResponseCode("200");
        apiResponse.setData(jsonObject);
        return apiResponse;
    }


    public ApiResponse getBranches(Long merchantId) {

        ApiResponse apiResponse = new ApiResponse();
        try {
            Merchant merchant = merchantRepository.findOne(merchantId);

            if (merchant != null) {
                String token = tokenService.getRushtoken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                String endpoint;
                if (merchant.getMerchantClassification().equals(MerchantClassification.GLOBE_SG)) {
                    endpoint = sgBranchesEndpoint;
                } else {
                    endpoint = branchesEndpoint;
                }
                //GET Employees
                List<BranchDTO> branchDTOs = new ArrayList<>();
                String url = rushHost + endpoint.replace(":merchant_type", merchant.getMerchantType().toString().toLowerCase());
                JSONObject jsonObj  = apiService.call(url, null, "GET", token);
                if (jsonObj == null) {
                    tokenService.refreshToken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                    return getBranches(merchantId);
                }

                List<JSONObject> branches = (ArrayList) jsonObj.get("data");
                for (JSONObject branch : branches) {
                    BranchDTO branchDTO = new BranchDTO();
                    branchDTO.setBranchName((String) branch.get("name"));
                    String uuid = (String) branch.get("id");
                    branchDTO.setUuid(uuid);
                    Branch b = branchRepository.findOneByUuid(uuid);
                    if (b != null) {
                        branchDTO.setBranchId(b.getId());
                        branchDTO.setWithVk(b.isWithVk());
                    }
                    branchDTOs.add(branchDTO);
                }
                apiResponse.setData(branchDTOs);
                apiResponse.setResponseCode("200");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return apiResponse;
    }

    public ApiResponse updateBranch(BranchDTO branchDTO) {

        ApiResponse apiResponse = new ApiResponse();
        Branch branch;
        if (branchDTO.getBranchId() != null) {
            branch = branchRepository.findOne(branchDTO.getBranchId());
            branch.setWithVk(branchDTO.getWithVk());
            branchRepository.save(branch);
        } else {
            branch = new Branch();
            branch.setUuid(branchDTO.getUuid());
            branch.setWithVk(branchDTO.getWithVk());
            branchRepository.save(branch);
        }

        apiResponse.setResponseCode("200");
        return apiResponse;
    }
}
