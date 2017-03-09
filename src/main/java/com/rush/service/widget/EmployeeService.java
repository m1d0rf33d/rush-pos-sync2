package com.rush.service.widget;

import com.rush.model.ApiResponse;
import com.rush.model.MerchantScreen;
import com.rush.model.User;
import com.rush.model.UserRole;
import com.rush.model.dto.EmployeeDTO;
import com.rush.model.dto.LoginDTO;
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
    @Value("${employee.access.endpoint}")
    private String employeeAccessEndpoint;
    @Value("${widget.host}")
    private String widgetHost;

    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MerchantScreenRepository merchantScreenRepository;

    @Autowired
    private APIService apiService;

    public ApiResponse<EmployeeDTO> login(LoginDTO loginDTO, String token) {
        ApiResponse apiResponse = new ApiResponse();
         try {
             List<NameValuePair> params = new ArrayList<>();
             params.add(new BasicNameValuePair("employee_id", loginDTO.getEmployeeId()));
             params.add(new BasicNameValuePair("branch_id", loginDTO.getBranchId()));
             if (loginDTO.getPin() != null) {
                 params.add(new BasicNameValuePair("pin", loginDTO.getPin()));
             }
             String url = rushHost + loginEmployeeEndpoint;
             JSONObject jsonObject = apiService.call((url), params, "post", token);
             if (jsonObject != null) {

                 if (jsonObject.get("error_code").equals("0x0")) {
                     JSONObject data = (JSONObject) jsonObject.get("data");
                     EmployeeDTO employee = new EmployeeDTO();
                     employee.setId((String) data.get("id"));
                     employee.setName((String) data.get("name"));
                     apiResponse.setData(employee);
                 } else {
                     apiResponse.setResponseCode(WidgetCode.X6);
                     apiResponse.setMessage((String) jsonObject.get("message"));
                 }
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
