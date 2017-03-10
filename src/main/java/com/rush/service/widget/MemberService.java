package com.rush.service.widget;

import com.rush.model.ApiResponse;
import com.rush.model.Merchant;
import com.rush.model.WidgetResponse;
import com.rush.model.dto.LoginMemberDTO;
import com.rush.model.dto.MemberDTO;
import com.rush.model.dto.RewardDTO;
import com.rush.model.enums.AppState;
import com.rush.model.enums.MerchantStatus;
import com.rush.model.enums.RushTokenType;
import com.rush.repository.MerchantRepository;
import com.rush.service.APIService;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aomine on 3/9/17.
 */
@Service
@PropertySource("classpath:api.properties")
public class MemberService {

    @Value("${rush.host}")
    private String rushHost;
    @Value("${login.member.endpoint}")
    private String loginMemberEndpoint;
    @Value("${member.points.endpoint}")
    private String memberPointsEndpoint;
    @Value("${member.rewards.endpoint}")
    private String memberRewardsEndpoint;
    @Value("${register.member.endpoint}")
    private String registerMemberEndpoint;

    @Autowired
    private APIService apiService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private MerchantRepository merchantRepository;

    public JSONObject loginMember(JSONObject requestBody) {

        String merchantKey  = (String) requestBody.get("merchant_key");
        String employeeId   = (String) requestBody.get("employee_id");
        String appState     = (String) requestBody.get("app_state");
        String mobileNumber = (String) requestBody.get("mobile_no");
        String merchantType = (String)requestBody.get("merchant_type");

        JSONObject payload = new JSONObject();
        JSONObject data = new JSONObject();

        String url = rushHost + loginMemberEndpoint.replace(":merchant_type", merchantType);
        url = url.replace(":employee_id", employeeId);

        String token = tokenService.getRushtoken(merchantKey, RushTokenType.MERCHANT_APP);
        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("mobile_no", mobileNumber));
            JSONObject jsonObject = apiService.call(url, params, "post", token);

            if (jsonObject != null) {
                payload.put("error_code", jsonObject.get("error_code"));
                payload.put("message", jsonObject.get("message"));
                if (jsonObject.get("error_code").equals("0x0")) {
                    JSONObject d = (JSONObject) jsonObject.get("data");

                    JSONObject member = new JSONObject();
                    member.put("mobile_no",d.get("mobile_no"));
                    member.put("gender", d.get("gender"));
                    member.put("profile_id", (String) d.get("profile_id"));
                    member.put("name", d.get("name"));
                    member.put("birthdate", d.get("birthdate"));
                    member.put("email", d.get("email"));
                    member.put("registration_date", d.get("registration_date"));
                    member.put("id", d.get("id"));

                    String points = getCurrentPoints((String) member.get("id"), token);
                    member.put("points", points);
                    data.put("member", member);

                    if (appState.equals(AppState.MEMBER_INQUIRY.toString())) {
                        List<RewardDTO> rewardDTOs = getActiveVouchers(merchantKey, merchantType, (String) member.get("id"));
                        data.put("rewards", rewardDTOs);
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return payload;
    }

    public List<RewardDTO> getActiveVouchers(String merchantKey, String merchantType, String customerId) {
         try {
             List<RewardDTO> rewardDTOs = new ArrayList<>();
             String token = tokenService.getRushtoken(merchantKey, RushTokenType.CUSTOMER_APP);

             String url = rushHost + memberRewardsEndpoint.replace(":merchant_type", merchantType.toLowerCase());
             url = url.replace(":id", customerId);
             JSONObject jsonObject = apiService.call(url, new ArrayList<>(), "get", token);
             if (jsonObject != null) {
                 if (jsonObject.get("error_code").equals("0x0")) {
                     List<JSONObject> dataJSON = (ArrayList) jsonObject.get("data");

                     for (JSONObject rewardJSON : dataJSON) {
                         RewardDTO reward = new RewardDTO();
                         reward.setDetails((String) rewardJSON.get("details"));
                         reward.setDate((String) rewardJSON.get("date"));
                         reward.setName((String) rewardJSON.get("name"));
                         reward.setId((rewardJSON.get("id")).toString());
                         reward.setImageUrl((String) rewardJSON.get("image_url"));
                         reward.setPointsRequired(String.valueOf(rewardJSON.get("points")));
                         rewardDTOs.add(reward);
                     }
                 }
             }
             return rewardDTOs;
         } catch (IOException e) {
             e.printStackTrace();
         }
        return null;
    }

    public String getCurrentPoints(String customerId, String token) {

        try {
            String url = rushHost + memberPointsEndpoint;
            url = url.replace(":customer_uuid", customerId);
            JSONObject jsonObject = apiService.call(url, null, "get", token);
            String points = (String) jsonObject.get("data");
            return points;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject registerMember(JSONObject jsonObject) {

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("name", (String) jsonObject.get("name")));
        params.add(new BasicNameValuePair("email", (String) jsonObject.get("email")));
        params.add(new BasicNameValuePair("mobile_no", (String) jsonObject.get("mobile_no")));
        params.add(new BasicNameValuePair("pin", (String) jsonObject.get("pin")));

        String birthDate = (String) jsonObject.get("birthdate");
        if (birthDate != null) {
            params.add(new BasicNameValuePair("birthdate", birthDate));
        }
        String gender = (String) jsonObject.get("gender");
        if (gender != null) {
            params.add(new BasicNameValuePair("gender", gender));
        }

        String employeeId = (String) jsonObject.get("employee_id");
        String merchantKey = (String) jsonObject.get("merchant_key");
        String merchantType = (String) jsonObject.get("merchant_type");

        String token = tokenService.getRushtoken(merchantKey, RushTokenType.MERCHANT_APP);

        String url = rushHost + registerMemberEndpoint;
        url = url.replace(":employee_id", employeeId).replace(":merchant_type", merchantType);
        JSONObject payload = new JSONObject();
        try {
            JSONObject rushResponse = apiService.call(url, params, "post", token);
            if (rushResponse != null) {
                payload.put("error_code", rushResponse.get("error_code"));
                payload.put("message", rushResponse.get("message"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return payload;

    }

}
