package com.rush.service.widget;

import com.rush.model.*;
import com.rush.model.dto.EmployeeDTO;
import com.rush.model.dto.LoginDTO;
import com.rush.model.enums.MerchantClassification;
import com.rush.model.enums.RushTokenType;
import com.rush.model.enums.WidgetCode;
import com.rush.repository.MerchantScreenRepository;
import com.rush.repository.UserRepository;
import com.rush.repository.UserRoleRepository;
import com.rush.service.APIService;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aomine on 3/9/17.
 */
@Service
@PropertySource("classpath:api.properties")
public class EmployeeService {

    @Value("${rush.host}")
    private String rushHost;
    @Value("${login.employee.endpoint}")
    private String loginEmployeeEndpoint;
    @Value("${widget.host}")
    private String widgetHost;
    @Value("${sg.login.endpoint}")
    private String sgLoginEndpoint;

    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MerchantScreenRepository merchantScreenRepository;

    @Autowired
    private APIService apiService;
    @Autowired
    private TokenService tokenService;

    public ApiResponse<EmployeeDTO> login(String employeeId,
                                          String branchId,
                                          String pin,
                                          Merchant merchant) {
        ApiResponse apiResponse = new ApiResponse();
         try {
             List<NameValuePair> params = new ArrayList<>();
             params.add(new BasicNameValuePair("employee_id", employeeId));
             params.add(new BasicNameValuePair("branch_id", branchId));
             if (pin != null) {
                 params.add(new BasicNameValuePair("pin", pin));
             }
             String token = tokenService.getRushtoken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
             String endpoint;
             if (merchant.getMerchantClassification().equals(MerchantClassification.GLOBE_SG)) {
                 endpoint = sgLoginEndpoint;
             } else {
                 endpoint = loginEmployeeEndpoint;
             }
             String url = rushHost + endpoint.replace(":merchant_type", merchant.getMerchantType());
             JSONObject jsonObject = apiService.call((url), params, "post", token);
             if (jsonObject != null) {
                 String errorCode = (String) jsonObject.get("error_code");
                 if (errorCode == null) {
                    tokenService.refreshToken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                 }
                 if (jsonObject.get("error_code").equals("0x0")) {
                     JSONObject data = (JSONObject) jsonObject.get("data");
                     EmployeeDTO employee = new EmployeeDTO();
                     employee.setId((String) data.get("id"));
                     employee.setName((String) data.get("name"));
                     apiResponse.setData(employee);
                 }
                 apiResponse.setMessage((String) jsonObject.get("message"));
                 apiResponse.setErrorCode((String) jsonObject.get("error_code"));
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
        return apiResponse;
    }

    public ApiResponse<List<String>> getEmployeeAccess(String employeeId) {
        List<String> access = new ArrayList<>();
        User user = userRepository.findOneByUuid(employeeId);
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
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setData(access);
        return apiResponse;
    }
}
