package com.rush.service.widget;

import com.rush.model.Merchant;
import com.rush.model.dto.BranchDTO;
import com.rush.model.enums.RushTokenType;
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
 * Created by aomine on 3/15/17.
 */
@Service
@PropertySource("classpath:api.properties")
public class GlobeSgService {

    @Value("${sg.branches.endpoint}")
    private String sgBranchesEndpoint;
    @Value("${rush.host}")
    private String rushHost;

    @Autowired
    private TokenService tokenService;
    @Autowired
    private APIService apiService;

    public List<JSONObject> getBranches(Merchant merchant) {
        try {
            String url = rushHost + sgBranchesEndpoint.replace(":merchant_type", merchant.getMerchantType().getValue().toLowerCase());
            String token = tokenService.getRushtoken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
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
        return null;
    }

}
