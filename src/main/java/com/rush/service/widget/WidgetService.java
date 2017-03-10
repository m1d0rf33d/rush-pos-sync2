package com.rush.service.widget;

import com.rush.model.ApiResponse;
import com.rush.model.Merchant;
import com.rush.model.dto.BranchDTO;
import com.rush.model.dto.EmployeeDTO;
import com.rush.model.dto.MerchantDTO;
import com.rush.model.enums.MerchantStatus;
import com.rush.model.enums.RushTokenType;
import com.rush.repository.MerchantRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

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

    public JSONObject initializeWidget(String merchantKey) {

        String widgetToken = tokenService.getWidgetToken(merchantKey);
        String rushtoken = tokenService.getRushtoken(merchantKey, RushTokenType.MERCHANT_APP);

        Merchant merchant = merchantRepository.findOneByUniqueKeyAndStatus(merchantKey, MerchantStatus.ACTIVE);

        JSONObject payload = new JSONObject();
        JSONObject data = new JSONObject();
        JSONObject merchantJSON = new JSONObject();

        if (merchant != null) {
            merchantJSON.put("name", merchant.getName());
            merchantJSON.put("token", widgetToken);
            merchantJSON.put("with_vk", merchant.getWithVk());
            merchantJSON.put("merchant_key", merchant.getUniqueKey());
            merchantJSON.put("merchant_type", merchant.getMerchantType().toString().toLowerCase());
            data.put("merchant", merchantJSON);
        }

        ApiResponse<List<BranchDTO>> apiResponse = merchantApiService.getBranches(merchant, rushtoken);
        if (apiResponse.getData() != null) {
            JSONArray branchArr = new JSONArray();
            List<BranchDTO> branches = apiResponse.getData();
            for (BranchDTO branch : branches) {
                JSONObject branchJSON = new JSONObject();
                branchJSON.put("name", branch.getBranchName());
                branchJSON.put("logo_url", branch.getLogoUrl());
                branchJSON.put("id", branch.getUuid());
                branchJSON.put("with_vk", branch.getWithVk());
                branchArr.add(branchJSON);
            }
            data.put("branches", branchArr);
        }
        payload.put("data", data);
        return payload;
    }

    public JSONObject loginEmployee(JSONObject requestBody) {

        String merchantType = (String) requestBody.get("merchant_type");
        String merchantKey = (String) requestBody.get("merchant_key");
        String employeeLogin = (String) requestBody.get("employee_login");
        String pin = (String) requestBody.get("pin");
        String branchId = (String) requestBody.get("branch_id");

        String rushToken = tokenService.getRushtoken(merchantKey, RushTokenType.MERCHANT_APP);
        ApiResponse<EmployeeDTO> loginResp = employeeService.login(employeeLogin, branchId, pin, merchantType,rushToken);

        JSONObject payload = new JSONObject();
        JSONObject data = new JSONObject();

        payload.put("message", loginResp.getMessage());
        payload.put("error_code", loginResp.getErrorCode());

        if (loginResp.getErrorCode().equals("0x0")) {
            EmployeeDTO employeeDTO = loginResp.getData();
            JSONObject employee = new JSONObject();
            employee.put("id", employeeDTO.getId());
            employee.put("name", employeeDTO.getName());
            data.put("employee", employee);

            //Merchant design
            ApiResponse<MerchantDTO> merchantDesignResp = merchantApiService.getMerchantDesign(rushToken, merchantType);
            MerchantDTO merchantDTO = merchantDesignResp.getData();
            JSONObject merchant = new JSONObject();
            merchant.put("background_url", merchantDTO.getBackgroundUrl());
            merchant.put("stamps_url", merchantDTO.getStampsUrl());
            merchant.put("gray_stamps_url", merchantDTO.getGrayStampsUrl());
            data.put("merchant", merchant);

            //Screen access
            ApiResponse<List<String>> accessResp = employeeService.getEmployeeAccess(employeeDTO.getId());
            List<String> screenAccess = accessResp.getData();
            data.put("access", screenAccess);
        }
        payload.put("data", data);
        return payload;
    }

}
