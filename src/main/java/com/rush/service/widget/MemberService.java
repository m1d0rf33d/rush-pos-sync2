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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
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
    @Value("${points.conversion.endpoint}")
    private String pointsConversionEndpoint;
    @Value("${earn.points.endpoint}")
    private String earnPointsEndpoint;
    @Value("${earn.guest.endpoint}")
    private String earnGuestEndpoint;
    @Value("${merchant.rewards.endpoint}")
    private String merchantRewardsEndpoint;
    @Value("${redeem.rewards.endpoint}")
    private String redeemRewardsEndpoint;
    @Value("${pay.points.endpoint}")
    private String payPointsEndpoint;
    @Value("${unclaimed.rewards.endpoint}")
    private String unclaimedRewardsEndpoint;
    @Value("${claim.rewards.endpoint}")
    private String claimRewardsEndpoint;
    @Value(("${customer.transactions.endpoint}"))
    private String customerTransactionsEndpoint;

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
                    member.put("profile_id", d.get("profile_id"));
                    member.put("name", d.get("name"));
                    member.put("birthdate", d.get("birthdate"));
                    member.put("email", d.get("email"));
                    member.put("registration_date", d.get("registration_date"));
                    member.put("id", d.get("id"));

                    String points = getCurrentPoints((String) member.get("id"), token);
                    member.put("points", points);
                    data.put("member", member);

                    if (appState.equals(AppState.EARN_POINTS.toString()) ||
                            appState.equals(AppState.PAY_WITH_POINTS.toString())) {
                        JSONObject pointsRuleJSON = getPointsRule(employeeId,(String) member.get("id"), merchantType, token);
                        data.put("pointsRule", pointsRuleJSON);
                    }
                    if (appState.equals(AppState.REDEEM_REWARDS.toString())) {
                        List<JSONObject> merchantRewards = getMerchantRewards(merchantKey, merchantType, token);
                        data.put("merchantRewards", merchantRewards);
                    }

                    if (appState.equals(AppState.ISSUE_REWARDS.toString()) ||
                            appState.equals(AppState.MEMBER_INQUIRY.toString())) {
                        List<JSONObject> rewards = new ArrayList<>();
                        List<JSONObject> unclaimedRewards = getUnclaimedRewards(employeeId, (String) member.get("id"), merchantKey, merchantType);
                        for (JSONObject redeem :unclaimedRewards) {
                            JSONObject reward = (JSONObject)  redeem.get("reward");
                            reward.put("redeem_id",  redeem.get("id"));
                            reward.put("date", redeem.get("date"));
                            rewards.add(reward);
                        }
                        data.put("rewards", rewards);
                    }
                    if (appState.equals(AppState.MEMBER_INQUIRY.toString())) {
                        data.put("transactions", getCustomerTransactions(merchantKey, (String) member.get("id")));
                    }

                    payload.put("data", data);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return payload;
    }

    public List<JSONObject> getActiveVouchers(String merchantKey, String merchantType, String customerId) {
         try {
             String token = tokenService.getRushtoken(merchantKey, RushTokenType.CUSTOMER_APP);
             String url = rushHost + memberRewardsEndpoint.replace(":merchant_type", merchantType.toLowerCase());
             url = url.replace(":id", customerId);
             JSONObject jsonObject = apiService.call(url, new ArrayList<>(), "get", token);
             if (jsonObject != null) {
                 if (jsonObject.get("error_code").equals("0x0")) {
                     return (ArrayList) jsonObject.get("data");
                 }
             }
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

    public JSONObject getPointsRule(String employeeId, String customerId, String merchantType, String token) {
        JSONObject payload = new JSONObject();
        try {
            String url = rushHost + pointsConversionEndpoint;
            url = url.replace(":employee_id", employeeId).replace(":merchant_type", merchantType).replace(":customer_id", customerId);
            JSONObject jsonObject = apiService.call(url, null, "get", token);
            if (jsonObject != null) {
                if (jsonObject.get("error_code").equals("0x0")) {
                    JSONObject dataJSON = (JSONObject) jsonObject.get("data");
                    payload.put("earning_peso",(Long) dataJSON.get("earning_peso"));
                    payload.put("redemption_peso",(Long) dataJSON.get("redemption_peso"));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return payload;
    }

    public JSONObject earnPoints(JSONObject requestBody) {
        JSONObject payload = new JSONObject();

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("uuid", (String) requestBody.get("customer_id")));
        params.add(new BasicNameValuePair("or_no", (String) requestBody.get("or_no")));
        params.add(new BasicNameValuePair("amount", (String) requestBody.get("amount")));

        String url = rushHost + earnPointsEndpoint;
        url = url.replace(":customer_uuid", (String) requestBody.get("customer_id"));
        url = url.replace(":employee_id", (String) requestBody.get("employee_id"));
        url = url.replace(":merchant_type", (String) requestBody.get("merchant_type"));
        String merchantKey = (String) requestBody.get("merchant_key");
        String token = tokenService.getRushtoken(merchantKey, RushTokenType.MERCHANT_APP);
        try {
            JSONObject jsonObject = apiService.call(url, params, "post", token);
            if (jsonObject != null) {
                String errorCode = (String) jsonObject.get("error_code");
                payload.put("error_code", errorCode);

                if (errorCode.equals("0x0")) {
                    String points = getCurrentPoints((String) requestBody.get("customer_id"), token);
                    payload.put("points", points);
                }
                payload.put("message", (String) jsonObject.get("message"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return payload;
    }

    public JSONObject guestPurchase(JSONObject requestBody) {

        JSONObject payload = new JSONObject();

        String merchantType = (String) requestBody.get("merchant_type");
        String merchantKey = (String) requestBody.get("merchant_key");
        String mobileNumber = (String) requestBody.get("mobile_no");
        String orNumber = (String) requestBody.get("or_no");
        String employeeId = (String)  requestBody.get("employee_id");
        String amount = (String) requestBody.get("amount");

        String url = rushHost + earnGuestEndpoint;
        url = url.replace(":employee_id", employeeId);
        url = url.replace(":merchant_type", merchantType);

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("mobile_no", mobileNumber));
        params.add(new BasicNameValuePair("or_no", orNumber));
        params.add(new BasicNameValuePair("amount", amount));

        String token = tokenService.getRushtoken(merchantKey, RushTokenType.MERCHANT_APP);
        try {
            JSONObject jsonObject = apiService.call(url, params, "post", token);
            if (jsonObject != null) {
                String errorCode = (String) jsonObject.get("error_code");
                String mesage = (String) jsonObject.get("message");
                payload.put("error_code", errorCode);
                payload.put("message", mesage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return payload;
    }

    public List<JSONObject> getMerchantRewards(String merchantKey, String merchantType, String token) {

        String url = rushHost + merchantRewardsEndpoint.replace(":merchant_type", merchantType);
        try {
            JSONObject jsonObject = apiService.call(url, new ArrayList<>(), "get", token);
            if (jsonObject != null) {

                if (jsonObject.get("error_code") == null) {
                    String newToken = tokenService.refreshToken(merchantKey, RushTokenType.MERCHANT_APP);
                    return getMerchantRewards(merchantKey, merchantType, newToken);
                }

                return (ArrayList) jsonObject.get("data");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject redeemReward(JSONObject requestBody) {

        JSONObject payload = new JSONObject();

        String employeeId = (String) requestBody.get("employee_id");
        String merchantKey = (String) requestBody.get("merchant_key");
        String merchantType = (String) requestBody.get("merchant_type");
        String customerId = (String) requestBody.get("customer_id");
        String rewardId = (String) requestBody.get("reward_id");
        String pin = (String) requestBody.get("pin");

        String token = tokenService.getRushtoken(merchantKey, RushTokenType.MERCHANT_APP);

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("pin", pin));

        String url = rushHost + redeemRewardsEndpoint.replace(":merchant_type", merchantType);
        url = url.replace(":customer_id", customerId);
        url = url.replace(":employee_id", employeeId);
        url = url.replace(":reward_id", rewardId);
        try {
            JSONObject jsonObject = apiService.call(url, params, "post", token);
            if (jsonObject != null) {
                String errorCode = (String) jsonObject.get("error_code");
                String message = (String) jsonObject.get("message");

                if (errorCode == null) {
                    tokenService.refreshToken(merchantKey, RushTokenType.MERCHANT_APP);
                    return redeemReward(requestBody);
                }
                JSONObject data = new JSONObject();
                if (errorCode.equals("0x0")) {
                    String points = getCurrentPoints(customerId, token);
                    data.put("points", points);
                    payload.put("data", data);
                }

                payload.put("error_code", errorCode);
                payload.put("message", message);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return payload;
    }


    public JSONObject payWithPoints(JSONObject requestBody) {

        JSONObject payload = new JSONObject();

        String employeeId = (String) requestBody.get("employee_id");
        String merchantType = (String) requestBody.get("merchant_type");
        String merchantKey = (String) requestBody.get("merchant_key");
        String customerId = (String) requestBody.get("customer_id");

        String pin = (String) requestBody.get("pin");
        String points = (String) requestBody.get("points");
        String orNumber = (String) requestBody.get("or_no");
        String amount = (String) requestBody.get("amount");

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("employee_uuid", employeeId));
        params.add(new BasicNameValuePair("or_no", orNumber));
        params.add(new BasicNameValuePair("amount", amount));
        params.add(new BasicNameValuePair("points", points));
        params.add(new BasicNameValuePair("pin", pin));

        String url = rushHost + payPointsEndpoint.replace(":merchant_type", merchantType);
        url = url.replace(":customer_uuid", customerId);

        String token = tokenService.getRushtoken(merchantKey, RushTokenType.MERCHANT_APP);
        try {
            JSONObject jsonObject = apiService.call(url, params, "post", token);
            if (jsonObject != null) {

                String errorCode = (String) jsonObject.get("error_code");
                String message = (String) jsonObject.get("message");
                payload.put("error_code", errorCode);
                payload.put("message", message);


                if (errorCode == null) {
                    tokenService.refreshToken(merchantKey, RushTokenType.MERCHANT_APP);
                    return payWithPoints(requestBody);
                }

                if (errorCode.equals("0x0")) {
                    String remainingPoints = getCurrentPoints(customerId, token);
                    JSONObject data = new JSONObject();
                    data.put("points", remainingPoints);
                    payload.put("data", data);
                }

                payload.put("errors", jsonObject.get("errors"));

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return payload;
    }


    public List<JSONObject> getUnclaimedRewards(String employeeId, String customerId, String merchantKey, String merchantType) {

        String url = rushHost + unclaimedRewardsEndpoint.replace(":merchant_type", merchantType);
        url = url.replace(":employee_id", employeeId);
        url = url.replace(":customer_id", customerId);

        String token = tokenService.getRushtoken(merchantKey, RushTokenType.MERCHANT_APP);
        try {
            JSONObject jsonObject = apiService.call(url, null, "get", token);
            if (jsonObject != null) {
                if (jsonObject.get("error_code").equals("0x0")) {
                    return (ArrayList) jsonObject.get("data");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject issueReward(JSONObject requestBody) {

        JSONObject payload = new JSONObject();

        String employeeId = (String) requestBody.get("employee_id");
        String merchantType = (String) requestBody.get("merchant_type");
        String merchantKey = (String) requestBody.get("merchant_key");
        String customerId = (String) requestBody.get("customer_id");

        String rewardId = (String) requestBody.get("redeem_id");

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("redeem_id", rewardId));

        String url = rushHost + claimRewardsEndpoint.replace(":merchant_type", merchantType);
        url = url.replace(":customer_id", customerId);
        url = url.replace(":employee_id", employeeId);

        String token = tokenService.getRushtoken(merchantKey, RushTokenType.MERCHANT_APP);
        try {
            JSONObject jsonObject = apiService.call(url, params, "post", token);
            if (jsonObject != null) {
                String errorCode = (String) jsonObject.get("error_code");
                String message = (String) jsonObject.get("message");

                if (errorCode == null) {
                    tokenService.refreshToken(merchantKey, RushTokenType.MERCHANT_APP);
                    return issueReward(requestBody);
                }

                payload.put("message", message);
                payload.put("error_code", errorCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return payload;
    }

    public List<JSONObject> getCustomerTransactions(String merchantKey, String customerId) {

        String url = rushHost + customerTransactionsEndpoint;
        url = url.replace(":customer_uuid", customerId);
        String token = tokenService.getRushtoken(merchantKey, RushTokenType.CUSTOMER_APP);

        try {
            JSONObject jsonObject = apiService.call(url, null, "get", token);
            if (jsonObject != null) {
                String errorCode = (String) jsonObject.get("error_code");
                String message = (String) jsonObject.get("message");
                if (errorCode.equals("0x0")) {
                    return (ArrayList) jsonObject.get("data");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public JSONObject sendOfflineTransacions(JSONObject requestBody) {

        String merchantKey  = (String) requestBody.get("merchant_key");
        String employeeId   = (String) requestBody.get("employee_id");
        String appState     = (String) requestBody.get("app_state");
        String merchantType = (String)requestBody.get("merchant_type");

        JSONObject reqBody = new JSONObject();
        reqBody.put("merchant_key", merchantKey);
        reqBody.put("merchant_type", merchantType);
        reqBody.put("app_state", appState);
        reqBody.put("employee_id", employeeId);

        List<JSONObject> transactions = (ArrayList) requestBody.get("transactions");
        List<String> result = new ArrayList<>();
        for (HashMap transaction : transactions) {
            StringBuilder sb = new StringBuilder();
            sb.append("mobileNumber=" + transaction.get("mobile_no"));
            sb.append(":");
            sb.append("totalAmount=" + transaction.get("amount"));
            sb.append(":");
            sb.append("orNumber=" + transaction.get("or_no"));
            sb.append(":");
            sb.append("date=" + transaction.get("date"));
            sb.append(":");

            reqBody.put("mobile_no", transaction.get("mobile_no"));

            JSONObject payload = loginMember(reqBody);
            String errorCode = (String) payload.get("error_code");
            String message = (String) payload.get("message");


            if (errorCode.equals("0x0")) {
                JSONObject data = (JSONObject) payload.get("data");
                JSONObject member = (JSONObject) data.get("member");

                reqBody.put("or_no", transaction.get("or_no"));
                reqBody.put("amount", transaction.get("amount"));
                reqBody.put("customer_id", member.get("id"));

                payload = earnPoints(reqBody);
                errorCode = (String) payload.get("error_code");
                message = (String) payload.get("message");

                if (errorCode.equals("0x0")) {
                    sb.append("status=" + "Submitted");
                    sb.append(":");
                    sb.append("message=Points earned.");
                } else {
                    JSONObject error = (JSONObject) payload.get("errors");
                    String errorMessage = "";
                    if (error != null) {
                        if (error.get("or_no") != null) {
                            List<String> l = (ArrayList<String>) error.get("or_no");
                            errorMessage = l.get(0);
                        }
                        if (error.get("amount") != null) {
                            List<String> l = (ArrayList<String>) error.get("amount");
                            errorMessage = l.get(0);
                        }
                    }
                    if (payload.get("message") != null) {
                        errorMessage = (String) payload.get("message");
                    }
                    sb.append("status=" + "Failed");
                    sb.append(":");
                    sb.append("message=" + errorMessage);
                }


            } else {
                sb.append("status=" + "Failed");
                sb.append(":");
                sb.append("message=" + payload.get("message"));
            }
            result.add(sb.toString());
        }

        JSONObject payload = new JSONObject();
        payload.put("error_code", "0x0");
        payload.put("message", "Submit offline transaction successful.");
        JSONObject data = new JSONObject();
        data.put("transactions", result);
        payload.put("data", data);

        return payload;
    }
}
