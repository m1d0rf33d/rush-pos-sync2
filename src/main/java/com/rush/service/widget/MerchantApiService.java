package com.rush.service.widget;

import com.rush.model.ApiResponse;
import com.rush.model.dto.BranchDTO;
import com.rush.model.Merchant;
import com.rush.model.dto.MerchantDTO;
import com.rush.model.enums.MerchantClassification;
import com.rush.model.enums.RushTokenType;
import com.rush.model.enums.WidgetCode;
import com.rush.service.APIService;
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
public class MerchantApiService {

    @Value("${rush.host}")
    private String rushHost;
    @Value("${branches.endpoint}")
    private String branchesEndpoint;
    @Value("${merchant.design.endpoint}")
    private String merchantDesignEndpoint;
    @Value("${sg.branches.endpoint}")
    private String sgBranchesEndpoint;
    @Value("${sg.merchant.design.endpoint}")
    private String sgMerchantDesignEndpoint;

    @Autowired
    private APIService apiService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private GlobeSgService globeSgService;

    public List<JSONObject> getBranches(Merchant merchant) {

        if (merchant.getMerchantClassification().equals(MerchantClassification.GLOBE_SG)) {
            return globeSgService.getBranches(merchant);
        } else {
            String url   = rushHost + branchesEndpoint.replace(":merchant_type", merchant.getMerchantType());
            String token = tokenService.getRushtoken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());

            try {
                JSONObject jsonObject = apiService.call(url, null, "get", token);
                if (jsonObject != null) {
                    if (jsonObject.get("error_code") == null) {
                        tokenService.refreshToken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                        return getBranches(merchant);
                    }
                    if (jsonObject.get("error_code").equals("0x0")) {
                        return (ArrayList) jsonObject.get("data");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public ApiResponse<MerchantDTO> getMerchantDesign(Merchant merchant) {
        ApiResponse apiResponse = new ApiResponse();
        try {
            String endpoint;
            if (merchant.getMerchantClassification().equals(MerchantClassification.GLOBE_SG)) {
                endpoint = sgMerchantDesignEndpoint;
            } else {
                endpoint = merchantDesignEndpoint;
            }
            String url = rushHost + endpoint.replace(":merchant_type", merchant.getMerchantType());
            String token = tokenService.getRushtoken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
            JSONObject jsonObject = apiService.call(url, new ArrayList<>(), "get", token);
            if (jsonObject != null) {
                JSONObject dataJSON = (JSONObject) jsonObject.get("data");
                JSONObject merchantJSON = (JSONObject) dataJSON.get("merchant");
                MerchantDTO merchantDTO = new MerchantDTO();
                merchantDTO.setBackgroundUrl((String) merchantJSON.get("background_url"));
                merchantDTO.setStampsUrl((String) merchantJSON.get("stamp_url"));
                merchantDTO.setGrayStampsUrl((String) merchantJSON.get("stamp_gray_url"));
                apiResponse.setData(merchantDTO);
                apiResponse.setErrorCode((String) jsonObject.get("error_code"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return apiResponse;
    }


}
