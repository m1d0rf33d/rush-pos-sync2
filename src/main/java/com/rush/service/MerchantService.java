package com.rush.service;

import com.rush.model.*;
import com.rush.model.enums.MerchantStatus;
import com.rush.model.enums.Screen;
import com.rush.repository.MerchantRepository;
import com.rush.repository.MerchantScreenRepository;
import com.rush.repository.RoleRepository;
import com.rush.repository.UserRepository;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
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

    private String baseUrl;
    private String merchantEmployeesEndpoint;
    private String authorizationEndpoint;
    private String branchesEndpoint;


    public MerchantService() {
        try {
            Properties prop = new Properties();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("api.properties");
            if (inputStream != null) {
                prop.load(inputStream);
                inputStream.close();
            }

            baseUrl = prop.getProperty("base_url");
            merchantEmployeesEndpoint = prop.getProperty("merchant_employees_endpoint");
            authorizationEndpoint = prop.getProperty("authorization_endpoint");
            branchesEndpoint = prop.getProperty("get_branches_endpoint");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


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

            String url = baseUrl + authorizationEndpoint;
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("app_key", merchant.getMerchantApiKey()));
            params.add(new BasicNameValuePair("app_secret", merchant.getMerchantApiSecret()));

            //GET Token
            String jsonResponse = apiService.call(url, params, "POST", null);
            JSONParser parser = new JSONParser();
            JSONObject tokenJSON = (JSONObject) parser.parse(jsonResponse);
            String token = (String) tokenJSON.get("token");

            //GET Employees
            url = baseUrl + merchantEmployeesEndpoint;
            params = new ArrayList<>();
            jsonResponse = apiService.call(url, params, "GET", token);
            JSONObject jsonObj = (JSONObject) parser.parse(jsonResponse);
            List<JSONObject> accounts = (ArrayList) jsonObj.get("data");
            for (JSONObject account : accounts) {
                String uuid = (String) account.get("uuid");
                User user = userRepository.findOneByUuid(uuid);
                if (user != null) {
                    List<Role> roles = user.getRoles();
                    if (!roles.isEmpty()) {
                        account.put("role", roles.get(0).getName());
                    }
                }
            }
            apiResponse.setData(accounts);
            apiResponse.setResponseCode("200");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return apiResponse;
    }

    public ApiResponse updateMerchantAccounts(UserDTO userDTO) {
        User user = userRepository.findOneByUuid(userDTO.getUuid());
        if (user == null) {
            user = new User();
        }
        user.setName(userDTO.getName());
        user.setUuid(userDTO.getUuid());

        Role role = roleRepository.findOneByName(userDTO.getRole());
        List<Role> roles = new ArrayList<>();
        roles.add(role);
        user.setRoles(roles);
        userRepository.save(user);

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setResponseCode("200");
        return apiResponse;
    }

    public ApiResponse getScreens(Long merchantId) {

        List<RoleDTO> roleDTOs = new ArrayList<>();
        Merchant merchant = merchantRepository.findOne(merchantId);
        Iterable<Role> roles = roleRepository.findAll();
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
}
