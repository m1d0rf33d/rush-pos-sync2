package com.rush.service.widget;

import com.rush.model.ApiResponse;
import com.rush.model.dto.BranchDTO;
import com.rush.model.Merchant;
import com.rush.model.dto.MerchantDTO;
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
    @Value("${merchantDesignEndpoint}")
    private String merchantDesignEndpoint;

    @Autowired
    private APIService apiService;

    public ApiResponse<List<BranchDTO>> getBranches(Merchant merchant, String token) {

        ApiResponse apiResponse = new ApiResponse();

        String url = rushHost + branchesEndpoint;

        try {
            JSONObject jsonObject = apiService.call(url, null, "get", token);
            List<BranchDTO> branchDTOs = new ArrayList<>();
            if (jsonObject != null) {
                List<JSONObject> data = (ArrayList) jsonObject.get("data");
                for (JSONObject json : data) {
                    BranchDTO branchDTO = new BranchDTO();
                    branchDTO.setUuid((String) json.get("id"));
                    branchDTO.setBranchName((String) json.get("name"));
                    branchDTO.setLogoUrl((String) json.get("logo_url"));
                    branchDTOs.add(branchDTO);
                }
                apiResponse.setData(branchDTOs);
                apiResponse.setResponseCode(WidgetCode.X2);
            }
        } catch (IOException e) {
            e.printStackTrace();
            apiResponse.setResponseCode(WidgetCode.X3);
        }
        return apiResponse;
    }

    public ApiResponse<MerchantDTO> getMerchantDesign(String token) {
        ApiResponse apiResponse = new ApiResponse();
        try {
            String url = rushHost + merchantDesignEndpoint;
            JSONObject jsonObject = apiService.call(url, new ArrayList<>(), "get", token);
            if (jsonObject != null) {
                JSONObject dataJSON = (JSONObject) jsonObject.get("data");
                JSONObject merchantJSON = (JSONObject) dataJSON.get("merchant");
                MerchantDTO merchantDTO = new MerchantDTO();
                merchantDTO.setBackgroundUrl((String) merchantJSON.get("background_url"));
                merchantDTO.setStampsUrl((String) merchantJSON.get("stamp_url"));
                merchantDTO.setGrayStampsUrl((String) merchantJSON.get("stamp_gray_url"));
                apiResponse.setData(merchantDTO);
                apiResponse.setResponseCode(WidgetCode.X2);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return apiResponse;
    }


}
