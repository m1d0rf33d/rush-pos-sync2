package com.rush.service.widget;

import com.rush.model.ApiResponse;
import com.rush.model.Branch;
import com.rush.model.Merchant;
import com.rush.model.dto.BranchDTO;
import com.rush.model.dto.EmployeeDTO;
import com.rush.model.dto.MerchantDTO;
import com.rush.model.enums.MerchantClassification;
import com.rush.model.enums.MerchantStatus;
import com.rush.model.enums.RushTokenType;
import com.rush.repository.BranchRepository;
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
    @Value("${sg.login.endpoint}")
    private String sgLoginEndpoint;

    @Autowired
    private TokenService tokenService;
    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private MerchantApiService merchantApiService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private BranchRepository branchRepository;

    public JSONObject initializeWidget(String merchantKey) {

        String widgetToken = tokenService.getWidgetToken(merchantKey);
        Merchant merchant  = merchantRepository.findOneByUniqueKeyAndStatus(merchantKey, MerchantStatus.ACTIVE);

        JSONObject payload = new JSONObject();
        JSONObject data = new JSONObject();
        JSONObject merchantJSON = new JSONObject();

        if (merchant != null) {
            merchantJSON.put("name", merchant.getName());
            merchantJSON.put("token", widgetToken);
            merchantJSON.put("with_vk", merchant.getWithVk());
            merchantJSON.put("merchant_key", merchant.getUniqueKey());
            merchantJSON.put("merchant_type", merchant.getMerchantType());
            data.put("merchant", merchantJSON);
        }

        List<JSONObject> branches = merchantApiService.getBranches(merchant);
        for (JSONObject branch : branches) {
            Branch b = branchRepository.findOneByUuid((String) branch.get("id"));
            if (b != null) {
                branch.put("with_vk", b.isWithVk());
            }
        }
        data.put("branches", branches);
        payload.put("data", data);
        return payload;
    }

    public JSONObject loginEmployee(JSONObject requestBody) {
        JSONObject payload = new JSONObject();

        String merchantKey   = (String) requestBody.get("merchant_key");
        String employeeLogin = (String) requestBody.get("employee_login");
        String pin           = (String) requestBody.get("pin");
        String branchId      = (String) requestBody.get("branch_id");

        Merchant merchant = merchantRepository.findOneByUniqueKeyAndStatus(merchantKey, MerchantStatus.ACTIVE);
        if (merchant != null) {
            ApiResponse<EmployeeDTO> loginResp = employeeService.login(employeeLogin, branchId, pin, merchant);
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
                ApiResponse<MerchantDTO> merchantDesignResp = merchantApiService.getMerchantDesign(merchant);
                MerchantDTO merchantDTO = merchantDesignResp.getData();
                JSONObject merchantJSON = new JSONObject();
                merchantJSON.put("background_url", merchantDTO.getBackgroundUrl());
                merchantJSON.put("stamps_url", merchantDTO.getStampsUrl());
                merchantJSON.put("gray_stamps_url", merchantDTO.getGrayStampsUrl());

                JSONObject merch = new JSONObject();
                merch.put("with_vk", merchant.getWithVk());
                data.put("merchant", merch);
                //Screen access
                ApiResponse<List<String>> accessResp = employeeService.getEmployeeAccess(employeeDTO.getId());
                List<String> screenAccess = accessResp.getData();
                data.put("access", screenAccess);
            }
            payload.put("data", data);
        } else {
            payload.put("message", "Merchant not found");
        }

        return payload;
    }

}
