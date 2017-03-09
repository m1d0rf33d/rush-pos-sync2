package com.rush.service.widget;

import com.rush.model.ApiResponse;
import com.rush.model.Merchant;
import com.rush.model.dto.EmployeeDTO;
import com.rush.model.dto.LoginDTO;
import com.rush.model.dto.MerchantDTO;
import com.rush.model.enums.MerchantStatus;
import com.rush.model.enums.RushTokenType;
import com.rush.model.enums.WidgetCode;
import com.rush.repository.MerchantRepository;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aomine on 3/9/17.
 */
@Service
@PropertySource("classpath:api.properties")
public class WidgetService {

    @Value("${widget.auth.endpoint}")
    private String widgetAuthEndpoint;
    @Value("${widget.host}")
    private String widgetHost;
    @Value("${login.employee.endpoint}")
    private String loginEmployeeEndpoint;

    @Autowired
    private TokenService tokenService;
    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private MerchantApiService merchantApiService;
    @Autowired
    private EmployeeService employeeService;

    public ApiResponse initializeWidget(String merchantKey) {
        Merchant merchant = merchantRepository.findOneByUniqueKeyAndStatus(merchantKey, MerchantStatus.ACTIVE);

        ApiResponse apiResponse = new ApiResponse();
        if (merchant == null) {
            apiResponse.setResponseCode(WidgetCode.X5);
        }
        String widgetToken = tokenService.getWidgetToken(merchant);
        String rushtoken = tokenService.getRushtoken(merchant, RushTokenType.MERCHANT_APP);

        JSONObject payload = new JSONObject();
        MerchantDTO merchantDTO = new MerchantDTO();
        merchantDTO.setName(merchant.getName());
        merchantDTO.setCustomerApiKey(merchant.getCustomerApiKey());
        merchantDTO.setCustomerApiSecret(merchant.getCustomerApiSecret());
        merchantDTO.setMerchantApiSecret(merchant.getMerchantApiSecret());
        merchantDTO.setMerchantApiKey(merchant.getMerchantApiKey());
        merchantDTO.setToken(widgetToken);
        payload.put("merchant", merchantDTO);

        ApiResponse branchResp = merchantApiService.getBranches(merchant, rushtoken);
        if (branchResp.getResponseCode().equals(WidgetCode.X2)) {
            payload.put("branches", branchResp.getData());
        } else {
            apiResponse.setMessage("Failed to retrieve merchant branches");
        }
        return apiResponse;
    }

    public ApiResponse loginEmployee(LoginDTO loginDTO) {
        ApiResponse apiResponse = new ApiResponse();
        Merchant merchant = merchantRepository.findOneByUniqueKeyAndStatus(loginDTO.getMerchantKey(), MerchantStatus.ACTIVE);

        String token = tokenService.getRushtoken(merchant, RushTokenType.MERCHANT_APP);
        ApiResponse<EmployeeDTO> loginResp = employeeService.login(loginDTO, token);

        JSONObject payload = new JSONObject();
        apiResponse.setErrorCode(loginResp.getErrorCode());
        if (loginResp.getResponseCode().equals(WidgetCode.X2)) {
            EmployeeDTO employeeDTO = loginResp.getData();
            payload.put("employee", employeeDTO);
            ApiResponse<MerchantDTO> merchantDesignResp = merchantApiService.getMerchantDesign(token);
            if (merchantDesignResp.getResponseCode().equals(WidgetCode.X2)) {
                MerchantDTO merchantDTO = merchantDesignResp.getData();
                payload.put("merchant", merchantDTO);
            }

            ApiResponse<List<String>> accessResp = employeeService.getEmployeeAccess(loginDTO.getEmployeeId());
            List<String> screenAccess = accessResp.getData();
            payload.put("access", screenAccess);
            apiResponse.setData(payload);
        }
        return loginResp;
    }

}
