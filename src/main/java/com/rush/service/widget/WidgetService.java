package com.rush.service.widget;

import com.rush.model.ApiResponse;
import com.rush.model.Merchant;
import com.rush.model.WidgetResponse;
import com.rush.model.dto.*;
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

    @Autowired
    private TokenService tokenService;
    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private MerchantApiService merchantApiService;
    @Autowired
    private EmployeeService employeeService;

    public WidgetResponse<WidgetInitDTO> initializeWidget(String merchantKey) {
        Merchant merchant = merchantRepository.findOneByUniqueKeyAndStatus(merchantKey, MerchantStatus.ACTIVE);

        WidgetResponse widgetResponse = new WidgetResponse();
        if (merchant == null) {

        }
        String widgetToken = tokenService.getWidgetToken(merchant);
        String rushtoken = tokenService.getRushtoken(merchant, RushTokenType.MERCHANT_APP);

        WidgetInitDTO widgetInitDTO = new WidgetInitDTO();
        MerchantDTO merchantDTO = new MerchantDTO();
        merchantDTO.setName(merchant.getName());
        merchantDTO.setCustomerApiKey(merchant.getCustomerApiKey());
        merchantDTO.setCustomerApiSecret(merchant.getCustomerApiSecret());
        merchantDTO.setMerchantApiSecret(merchant.getMerchantApiSecret());
        merchantDTO.setMerchantApiKey(merchant.getMerchantApiKey());
        merchantDTO.setToken(widgetToken);
        merchantDTO.setWithVk(merchant.getWithVk());
        merchantDTO.setMerchantType(merchant.getMerchantType().toString());
        widgetInitDTO.setMerchantDTO(merchantDTO);

        ApiResponse<List<BranchDTO>> branchResp = merchantApiService.getBranches(merchant, rushtoken);
        if (branchResp.getErrorCode().equals("0x0")) {
            widgetInitDTO.setBranchDTOs(branchResp.getData());
        } else {
            widgetResponse.setMessage("Failed to retrieve merchant branches");
        }
        widgetResponse.setData(widgetInitDTO);
        return widgetResponse;
    }

    public WidgetResponse<LoginResponseDTO> loginEmployee(LoginDTO loginDTO) {
        WidgetResponse widgetResponse = new WidgetResponse();
        Merchant merchant = merchantRepository.findOneByUniqueKeyAndStatus(loginDTO.getMerchantKey(), MerchantStatus.ACTIVE);

        String token = tokenService.getRushtoken(merchant, RushTokenType.MERCHANT_APP);
        ApiResponse<EmployeeDTO> loginResp = employeeService.login(loginDTO, token, merchant);
        widgetResponse.setErrorCode(loginResp.getErrorCode());
        widgetResponse.setMessage(loginResp.getMessage());

        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();

        if (loginResp.getErrorCode().equals("0x0")) {
            EmployeeDTO employeeDTO = loginResp.getData();
            loginResponseDTO.setEmployeeDTO(employeeDTO);

            ApiResponse<MerchantDTO> merchantDesignResp = merchantApiService.getMerchantDesign(token, merchant);
            if (merchantDesignResp.getErrorCode().equals("0x0")) {
                MerchantDTO merchantDTO = merchantDesignResp.getData();
                loginResponseDTO.setMerchantDTO(merchantDTO);
            }

            ApiResponse<List<String>> accessResp = employeeService.getEmployeeAccess(loginDTO.getEmployeeId());
            List<String> screenAccess = accessResp.getData();
            loginResponseDTO.setScreenAccess(screenAccess);
            widgetResponse.setData(loginResponseDTO);
        }
        return widgetResponse;
    }

}
