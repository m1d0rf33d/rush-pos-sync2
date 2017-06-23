package com.rush.service.widget;

import com.rush.model.Merchant;
import com.rush.model.enums.*;
import com.rush.repository.MerchantRepository;
import com.rush.service.APIService;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.LoadTimeWeavingConfiguration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
    @Value("${sg.register.endpoint}")
    private String sgRegisterEndpoint;
    @Value("${sg.customer.login.endpoint}")
    private String customerLoginEndpoint;
    @Value("${sg.unclaimed.endpoint}")
    private String sgUnclaimedEndpoint;
    @Value("${sg.customer.points.endpoint}")
    private String sgCustomerPointsEndpoint;
    @Value("${sg.customer.transactions.endpoint}")
    private String sgCustomerTransactionsEndpoint;
    @Value("${sg.customer.earn.endpoint}")
    private String sgCustomerEarnEndpoint;
    @Value("${sg.points.conversion.endpoint}")
    private String sgPointsConversionEndpoint;
    @Value(("${sg.account.summary.endpoint}"))
    private String sgAccountSummaryEndpoint;
    @Value("${customer.card.endpoint}")
    private String customerCardEndpoint;
    @Value("${earn.stamps.endpoint}")
    private String earnStampsEndpoint;
    @Value("${stamps.count.endpoint}")
    private String stampsCountEndpoint;
    @Value("${redeem.stamp.endpoint}")
    private String redeemStampEndpoint;
    @Value("${issue.stamp.reward.endpoint}")
    private String issueStampRewardEndpoint;
    @Value("${sg.merchant.rewards.endpoint}")
    private String sgMerchantRewardsEndpoint;
    @Value("${sg.redeem.endpoint}")
    private String sgRedeemEndpoint;
    @Value("${sg.issue.endpoint}")
    private String sgIssueEndpoint;

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

        JSONObject payload = new JSONObject();
        Merchant merchant = merchantRepository.findOneByUniqueKeyAndStatus(merchantKey, MerchantStatus.ACTIVE);
        if (merchant != null) {
            JSONObject data = new JSONObject();
            String endpoint;
            if (merchant.getMerchantClassification().equals(MerchantClassification.GLOBE_SG)) {
                endpoint = customerLoginEndpoint;
            } else {
                endpoint = loginMemberEndpoint;
            }
            String url = rushHost + endpoint.replace(":merchant_type", merchant.getMerchantType().getValue().toLowerCase());
            url = url.replace(":employee_id", employeeId);
            String token = tokenService.getRushtoken(merchantKey, RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
            try {
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("mobile_no", mobileNumber));
                JSONObject jsonObject = apiService.call(url, params, "post", token);
                if (jsonObject != null) {
                    if (jsonObject.get("error_code") == null) {
                        tokenService.refreshToken(merchantKey, RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                        return loginMember(requestBody);
                    }

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
                        member.put("account_number", d.get("account_number"));
                        member.put("account_name", d.get("account_name"));
                        data.put("member", member);

                        if (merchant.getMerchantType().equals(MerchantType.LOYALTY)) {
                            String points = getCurrentPoints((String) member.get("id"),(String) requestBody.get("employee_id"),merchant);
                            member.put("points", points);

                            if (appState.equals(AppState.EARN_POINTS.toString()) ||
                                    appState.equals(AppState.PAY_WITH_POINTS.toString())) {
                                JSONObject pointsRuleJSON = getPointsRule(employeeId,(String) member.get("id"), merchant);
                                data.put("pointsRule", pointsRuleJSON);
                            }
                            if (appState.equals(AppState.REDEEM_REWARDS.toString())) {
                                List<JSONObject> merchantRewards = getMerchantRewards(merchant);
                                data.put("merchantRewards", merchantRewards);
                            }

                            if (appState.equals(AppState.ISSUE_REWARDS.toString()) ||
                                    appState.equals(AppState.MEMBER_INQUIRY.toString())) {
                                List<JSONObject> rewards = new ArrayList<>();
                                List<JSONObject> unclaimedRewards = getUnclaimedRewards(employeeId, (String) member.get("id"), merchant);
                                for (JSONObject redeem :unclaimedRewards) {
                                    JSONObject reward = (JSONObject)  redeem.get("reward");
                                    reward.put("redeem_id",  redeem.get("id"));
                                    reward.put("date", redeem.get("date"));
                                    reward.put("quantity", redeem.get("quantity"));
                                    reward.put("claimed", redeem.get("claimed"));
                                    rewards.add(reward);
                                }
                                data.put("rewards", rewards);
                            }


                            if (merchant.getMerchantClassification().equals(MerchantClassification.GLOBE_SG)) {
                                data.put("account_points", getAccountPoints(merchant, (String) member.get("account_number"), employeeId));
                            }
                        } else {
                            JSONObject customerCard = getCustomerCard(merchant, employeeId, (String) member.get("id"));
                            data.put("customer_card", customerCard);
                        }

                        if (appState.equals(AppState.MEMBER_INQUIRY.toString())) {
                            data.put("transactions", getCustomerTransactions(merchant, (String) member.get("id")));
                        }

                        payload.put("data", data);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return payload;
    }


    public String getCurrentPoints(String customerId, String employeeId, Merchant merchant) {

        try {
            String endpoint;
            if (merchant.getMerchantClassification().equals(MerchantClassification.GLOBE_SG)) {
                endpoint = sgCustomerPointsEndpoint;
            } else {
                endpoint = memberPointsEndpoint;
            }
            String merchantKey = merchant.getUniqueKey();
            String token = tokenService.getRushtoken(merchantKey, RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());

            String url = rushHost + endpoint.replace(":merchant_type", merchant.getMerchantType().getValue().toLowerCase());
            url = url.replace(":customer_id", customerId);
            url = url.replace(":employee_id", employeeId);
            JSONObject jsonObject = apiService.call(url, null, "get", token);
            if (jsonObject != null) {
                if (jsonObject.get("error_code") == null) {
                    tokenService.refreshToken(merchantKey, RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                    return getCurrentPoints(customerId, employeeId, merchant);
                }
                String points = (String) jsonObject.get("data");
                return points;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject registerMember(JSONObject requestBody) {
        JSONObject payload = new JSONObject();
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("name", (String) requestBody.get("name")));
        params.add(new BasicNameValuePair("email", (String) requestBody.get("email")));
        params.add(new BasicNameValuePair("mobile_no", (String) requestBody.get("mobile_no")));
        params.add(new BasicNameValuePair("pin", (String) requestBody.get("pin")));

        String birthDate = (String) requestBody.get("birthdate");
        if (birthDate != null) {
            params.add(new BasicNameValuePair("birthdate", birthDate));
        }
        String gender = (String) requestBody.get("gender");
        if (gender != null) {
            params.add(new BasicNameValuePair("gender", gender));
        }
        if (requestBody.get("account_number") != null) {
            params.add(new BasicNameValuePair("account_number", (String) requestBody.get("account_number")));
        }
        if (requestBody.get("title_id") != null) {
            params.add(new BasicNameValuePair("title_id", ((Integer) requestBody.get("title_id")).toString()));
        }


        String employeeId = (String) requestBody.get("employee_id");
        String merchantKey = (String) requestBody.get("merchant_key");
        String merchantType = (String) requestBody.get("merchant_type");

        Merchant merchant = merchantRepository.findOneByUniqueKeyAndStatus(merchantKey, MerchantStatus.ACTIVE);
        if (merchant != null) {
            String token = tokenService.getRushtoken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
            String endpoint;
            if (merchant.getMerchantClassification().equals(MerchantClassification.GLOBE_SG)) {
                endpoint = sgRegisterEndpoint;
            } else {
                endpoint = registerMemberEndpoint;
            }
            String url = rushHost + endpoint;
            url = url.replace(":employee_id", employeeId).replace(":merchant_type", merchant.getMerchantType().getValue().toLowerCase());

            try {
                JSONObject rushResponse = apiService.call(url, params, "post", token);
                if (rushResponse != null) {
                    if (rushResponse.get("error_code") == null) {
                        tokenService.refreshToken(merchantKey, RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                        return registerMember(requestBody);
                    }

                    payload.put("error_code", rushResponse.get("error_code"));
                    payload.put("message", rushResponse.get("message"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            payload.put("message", "Merchant not found.");
        }
        return payload;
    }

    public JSONObject getPointsRule(String employeeId, String customerId, Merchant merchant) {
        JSONObject payload = new JSONObject();
        try {
            String token = tokenService.getRushtoken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
            String endpoint;
            if (merchant.getMerchantClassification().equals(MerchantClassification.GLOBE_SG)) {
                endpoint = sgPointsConversionEndpoint;
            } else {
                endpoint = pointsConversionEndpoint;
            }
            String url = rushHost + endpoint;
            url = url.replace(":employee_id", employeeId).replace(":merchant_type", merchant.getMerchantType().getValue().toLowerCase()).replace(":customer_id", customerId);
            JSONObject jsonObject = apiService.call(url, null, "get", token);
            if (jsonObject != null) {

                if (jsonObject.get("error_code") == null) {
                    tokenService.refreshToken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                    return getPointsRule(employeeId, customerId, merchant);
                }

                if (jsonObject.get("error_code").equals("0x0")) {
                    JSONObject dataJSON = (JSONObject) jsonObject.get("data");
                    payload.put("earning_peso", dataJSON.get("earning_peso"));
                    payload.put("redemption_peso", dataJSON.get("redemption_peso"));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return payload;
    }

    public JSONObject earnPoints(JSONObject requestBody) {
        JSONObject payload = new JSONObject();


        Merchant merchant = merchantRepository.findOneByUniqueKeyAndStatus((String) requestBody.get("merchant_key"), MerchantStatus.ACTIVE);
        if (merchant != null) {
            String endpoint;
            if (merchant.getMerchantClassification().equals(MerchantClassification.GLOBE_SG)) {
                endpoint = sgCustomerEarnEndpoint;
            } else {
                endpoint = earnPointsEndpoint;
            }

            String url = rushHost + endpoint;
            url = url.replace(":customer_id", (String) requestBody.get("customer_id"));
            url = url.replace(":employee_id", (String) requestBody.get("employee_id"));
            url = url.replace(":merchant_type", ((String) requestBody.get("merchant_type")).toLowerCase());
            String merchantKey = (String) requestBody.get("merchant_key");
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("uuid", (String) requestBody.get("customer_id")));
            params.add(new BasicNameValuePair("or_no", (String) requestBody.get("or_no")));
            params.add(new BasicNameValuePair("amount", (String) requestBody.get("amount")));

            //validate points
            JSONObject pointsRule = getPointsRule((String) requestBody.get("employee_id"), (String) requestBody.get("customer_id"), merchant);
            Long earningPeso = (Long) pointsRule.get("earning_peso");
            String amountStr = (String) requestBody.get("amount");
            Long amt;
            try {
                amt = Long.parseLong(amountStr);
            } catch (NumberFormatException e) {
                Double d = Double.parseDouble(amountStr);
                amt = d.longValue();
            }
            if (earningPeso > amt) {
                payload.put("error_code", "0x1");
                payload.put("message", "Sorry! Amount entered is below the minimum purchase of P" + earningPeso);
                return payload;
            }

            String token = tokenService.getRushtoken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
            try {
                JSONObject jsonObject = apiService.call(url, params, "post", token);
                if (jsonObject != null) {

                    if (jsonObject.get("error_code") == null) {
                        tokenService.refreshToken(merchantKey, RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                        return earnPoints(requestBody);
                    }

                    String errorCode = (String) jsonObject.get("error_code");
                    payload.put("error_code", errorCode);

                    if (errorCode.equals("0x0")) {
                        String points = getCurrentPoints((String) requestBody.get("customer_id"), (String) requestBody.get("employee_id"), merchant);
                        payload.put("points", points);
                    }
                    payload.put("message",jsonObject.get("message"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            payload.put("mesage", "Merchant not found");
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
        url = url.replace(":merchant_type", merchantType.toLowerCase());


        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("mobile_no", mobileNumber));
        params.add(new BasicNameValuePair("or_no", orNumber));
        params.add(new BasicNameValuePair("amount", amount));

        Merchant merchant = merchantRepository.findOneByUniqueKeyAndStatus(merchantKey, MerchantStatus.ACTIVE);
        if (merchant != null) {

            String token = tokenService.getRushtoken(merchantKey, RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
            try {
                JSONObject jsonObject = apiService.call(url, params, "post", token);
                if (jsonObject != null) {

                    if (jsonObject.get("error_code") == null) {
                        tokenService.refreshToken(merchantKey, RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                        return guestPurchase(requestBody);
                    }

                    String errorCode = (String) jsonObject.get("error_code");
                    String mesage = (String) jsonObject.get("message");
                    payload.put("error_code", errorCode);
                    payload.put("message", mesage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            payload.put("message", "Merchant not found");
        }

        return payload;
    }

    public List<JSONObject> getMerchantRewards(Merchant merchant) {
        String endpoint = "";
        if (merchant.getMerchantClassification().equals(MerchantClassification.GLOBE_SG)) {
            endpoint = sgMerchantRewardsEndpoint;
        } else {
            endpoint = merchantRewardsEndpoint.replace(":merchant_type", merchant.getMerchantType().getValue().toLowerCase());
        }
        String url = rushHost + endpoint;
        try {
            String token = tokenService.getRushtoken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
            JSONObject jsonObject = apiService.call(url, new ArrayList<>(), "get", token);
            if (jsonObject != null) {

                if (jsonObject.get("error_code") == null) {
                    tokenService.refreshToken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                    return getMerchantRewards(merchant);
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
        String quantity = (String) requestBody.get("quantity");

        Merchant merchant = merchantRepository.findOneByUniqueKeyAndStatus(merchantKey, MerchantStatus.ACTIVE);
        if (merchant != null) {
            String token = tokenService.getRushtoken(merchantKey, RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("pin", pin));
            params.add(new BasicNameValuePair("quantity", quantity));

            String endpoint = "";
            if (merchant.getMerchantClassification().equals(MerchantClassification.GLOBE_SG)) {
                endpoint = sgRedeemEndpoint;
            } else {
                endpoint = redeemRewardsEndpoint.replace(":merchant_type", merchantType);
            }

            String url = rushHost + endpoint;
            url = url.replace(":customer_id", customerId);
            url = url.replace(":employee_id", employeeId);
            url = url.replace(":reward_id", rewardId);
            try {
                JSONObject jsonObject = apiService.call(url, params, "post", token);
                if (jsonObject != null) {
                    String errorCode = (String) jsonObject.get("error_code");
                    String message = (String) jsonObject.get("message");

                    if (errorCode == null) {
                        tokenService.refreshToken(merchantKey, RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                        return redeemReward(requestBody);
                    }
                    JSONObject data = new JSONObject();
                    if (errorCode.equals("0x0")) {
                        String points = getCurrentPoints(customerId, employeeId,merchant);
                        data.put("points", points);
                        payload.put("data", data);
                    }

                    payload.put("error_code", errorCode);
                    payload.put("message", message);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            payload.put("message", "Merchant not found");
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

        Merchant merchant = merchantRepository.findOneByUniqueKeyAndStatus(merchantKey, MerchantStatus.ACTIVE);
        if (merchant != null) {
            String url = rushHost + payPointsEndpoint.replace(":merchant_type", merchantType);
            url = url.replace(":customer_id", customerId);

            String token = tokenService.getRushtoken(merchantKey, RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
            try {
                JSONObject jsonObject = apiService.call(url, params, "post", token);
                if (jsonObject != null) {

                    if (jsonObject.get("error_code") == null) {
                        tokenService.refreshToken(merchantKey, RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                        return payWithPoints(requestBody);
                    }

                    String errorCode = (String) jsonObject.get("error_code");
                    String message = (String) jsonObject.get("message");
                    payload.put("error_code", errorCode);
                    payload.put("message", message);

                    if (errorCode.equals("0x0")) {
                        String remainingPoints = getCurrentPoints(customerId, employeeId, merchant);
                        JSONObject data = new JSONObject();
                        data.put("points", remainingPoints);
                        payload.put("data", data);
                    }

                    payload.put("errors", jsonObject.get("errors"));

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            payload.put("message", "Merchant not found");
        }
        return payload;
    }


    public List<JSONObject> getUnclaimedRewards(String employeeId, String customerId, Merchant merchant) {
        String endpoint;
        if (merchant.getMerchantClassification().equals(MerchantClassification.GLOBE_SG)) {
            endpoint = sgUnclaimedEndpoint;
        } else {
            endpoint = unclaimedRewardsEndpoint;
        }
        String url = rushHost + endpoint.replace(":merchant_type", merchant.getMerchantType().getValue().toLowerCase());
        url = url.replace(":employee_id", employeeId);
        url = url.replace(":customer_id", customerId);

        String token = tokenService.getRushtoken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
        try {
            JSONObject jsonObject = apiService.call(url, null, "get", token);
            if (jsonObject != null) {

                if (jsonObject.get("error_code") == null) {
                    tokenService.refreshToken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                    return getUnclaimedRewards(employeeId, customerId, merchant);
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

    public JSONObject issueReward(JSONObject requestBody) {

        JSONObject payload = new JSONObject();

        String employeeId = (String) requestBody.get("employee_id");
        String merchantType = (String) requestBody.get("merchant_type");
        String merchantKey = (String) requestBody.get("merchant_key");
        String customerId = (String) requestBody.get("customer_id");
        String rewardId = (String) requestBody.get("redeem_id");
        String quantity = (String) requestBody.get("quantity");

        Merchant merchant = merchantRepository.findOneByUniqueKeyAndStatus(merchantKey, MerchantStatus.ACTIVE);
        if (merchant != null) {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("redeem_id", rewardId));
            params.add(new BasicNameValuePair("quantity", quantity));

            String endpoint = "";
            if (merchant.getMerchantClassification().equals(MerchantClassification.GLOBE_SG)) {
                endpoint = sgIssueEndpoint;
            } else {
                endpoint = claimRewardsEndpoint.replace(":merchant_type", merchantType);
            }

            String url = rushHost + endpoint;
            url = url.replace(":customer_id", customerId);
            url = url.replace(":employee_id", employeeId);

            String token = tokenService.getRushtoken(merchantKey, RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
            try {
                JSONObject jsonObject = apiService.call(url, params, "post", token);
                if (jsonObject != null) {

                    if (jsonObject.get("error_code") == null) {
                        tokenService.refreshToken(merchantKey, RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                        return issueReward(requestBody);
                    }

                    String errorCode = (String) jsonObject.get("error_code");
                    String message = (String) jsonObject.get("message");

                    payload.put("message", message);
                    payload.put("error_code", errorCode);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            payload.put("message", "Merchant not found");
        }

        return payload;
    }

    public List<JSONObject> getCustomerTransactions(Merchant merchant, String customerId) {

        String endpoint;
        if (merchant.getMerchantClassification().equals(MerchantClassification.GLOBE_SG)) {
            endpoint = sgCustomerTransactionsEndpoint;
        } else {
            endpoint = customerTransactionsEndpoint;
        }
        String url = rushHost + endpoint;
        url = url.replace(":id", customerId);
        String token = tokenService.getRushtoken(merchant.getUniqueKey(), RushTokenType.CUSTOMER_APP, merchant.getMerchantClassification());

        try {
            JSONObject jsonObject = apiService.call(url, null, "get", token);
            if (jsonObject != null) {
                String errorCode = (String) jsonObject.get("error_code");
                if (errorCode == null) {
                    tokenService.refreshToken(merchant.getUniqueKey(), RushTokenType.CUSTOMER_APP, merchant.getMerchantClassification());
                    return getCustomerTransactions(merchant, customerId);
                }
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

    private String getAccountPoints(Merchant merchant, String accountNumber, String employeeId) {
        String endpoint = sgAccountSummaryEndpoint;
        String url = rushHost + endpoint.replace(":employee_id", employeeId);
        String token = tokenService.getRushtoken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());

        try {
            JSONObject jsonObject = apiService.call(url, null, "get", token);
            if (jsonObject != null) {
                if (jsonObject.get("error_code") == null) {
                    tokenService.refreshToken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                    return getAccountPoints(merchant, accountNumber, employeeId);
                }
                List<JSONObject> data = (ArrayList) jsonObject.get("data");
                for (JSONObject account : data) {
                    if (account.get("armstrong_id").equals(accountNumber)) {
                        Long i = (Long) account.get("points_available");
                        return i.toString();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject getCustomerCard(Merchant merchant, String employeeId, String customerId) {

        String token = tokenService.getRushtoken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());

        String url = rushHost + customerCardEndpoint;
        url = url.replace(":employee_id", employeeId);
        url = url.replace(":customer_id", customerId);

        try {
            JSONObject jsonObject = apiService.call(url, null, "get", token);
            if (jsonObject != null) {
                if (jsonObject.get("error_code") == null) {
                    tokenService.refreshToken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                    return getCustomerCard(merchant, employeeId, customerId);
                }

                return (JSONObject) jsonObject.get("data");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public JSONObject earnStamp(JSONObject requestBody) {

        JSONObject payload = new JSONObject();

        String merchantKey = (String) requestBody.get("merchant_key");
        String employeeId = (String) requestBody.get("employee_id");
        String customerId = (String) requestBody.get("customer_id");
        String amount = (String) requestBody.get("amount");

        Merchant merchant = merchantRepository.findOneByUniqueKeyAndStatus(merchantKey, MerchantStatus.ACTIVE);
        if (merchant != null) {

            String url = rushHost + earnStampsEndpoint;
            url = url.replace(":employee_id", employeeId);
            url = url.replace(":customer_id", customerId);

            String token = tokenService.getRushtoken(merchantKey, RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
            try {
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("amount", amount));
                JSONObject jsonObject = apiService.call(url, params, "post", token);
                if (jsonObject.get("error_code") == null) {
                    tokenService.refreshToken(merchantKey, RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                    return earnStamp(requestBody);
                }

                String errorCode = (String) jsonObject.get("error_code");
                String message = (String) jsonObject.get("message");
                if (errorCode.equals("0x0")) {
                    Long stampCount = getStampsCount(merchant, employeeId, customerId);
                    JSONObject data = new JSONObject();
                    data.put("stamp_count", stampCount);
                    payload.put("data", data);
                }
                payload.put("message", message);
                payload.put("error_code", errorCode);

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            payload.put("message", "Merchant not found");
        }

        return payload;
    }


    public Long getStampsCount(Merchant merchant, String employeeId, String customerId) {
        try {
            String url = rushHost + stampsCountEndpoint;
            url = url.replace(":employee_id", employeeId);
            url = url.replace(":customer_id", customerId);

            String token = tokenService.getRushtoken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
            JSONObject jsonObject = apiService.call(url, null, "get", token);
            if (jsonObject.get("error_code") == null) {
                tokenService.refreshToken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                return  getStampsCount(merchant, employeeId, customerId);
            }

            if (jsonObject.get("error_code").equals("0x0")) {
                return (Long) jsonObject.get("data");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject redeemStamp(JSONObject requestBody) {
        JSONObject payload = new JSONObject();

        String employeeId = (String) requestBody.get("employee_id");
        String customerId = (String) requestBody.get("customer_id");
        String rewardId = (String) requestBody.get("reward_id");
        String pin = (String) requestBody.get("pin");
        String merchantKey = (String) requestBody.get("merchant_key");

        Merchant merchant = merchantRepository.findOneByUniqueKeyAndStatus(merchantKey, MerchantStatus.ACTIVE);
        if (merchant != null) {
            String url = rushHost + redeemStampEndpoint;
            url = url.replace(":employee_id", employeeId);
            url = url.replace(":customer_id", customerId);
            url = url.replace(":reward_id", rewardId);

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("pin", pin));
            String token = tokenService.getRushtoken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
            try {
                JSONObject jsonObject = apiService.call(url, params, "post", token);
                if (jsonObject.get("error_code") == null) {
                    tokenService.refreshToken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                    return redeemStamp(requestBody);
                }
                String message = (String) jsonObject.get("message");
                String errorCode = (String) jsonObject.get("error_code");
                if (errorCode.equals("0x0")) {
                    JSONObject customerCard = getCustomerCard(merchant, employeeId, customerId);
                    JSONObject data = new JSONObject();
                    data.put("customer_card", customerCard);
                    payload.put("data", data);
                }
                payload.put("message", message);
                payload.put("error_code", errorCode);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return payload;
    }

    public JSONObject issueStampReward(JSONObject requestBody) {

        JSONObject payload = new JSONObject();

        String merchantKey = (String) requestBody.get("merchant_key");
        String employeeId = (String) requestBody.get("employee_id");
        String customerId = (String) requestBody.get("customer_id");
        String rewardId = (String) requestBody.get("reward_id");
        String pin = (String) requestBody.get("pin");

        Merchant merchant = merchantRepository.findOneByUniqueKeyAndStatus(merchantKey, MerchantStatus.ACTIVE);
        if (merchant != null) {

            String token = tokenService.getRushtoken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("pin", pin));

            String url = rushHost + issueStampRewardEndpoint;
            url = url.replace(":employee_id", employeeId);
            url = url.replace(":customer_id", customerId);
            url = url.replace(":rewards_id", rewardId);

            try {
                JSONObject jsonObject = apiService.call(url, params, "post", token);
                if (jsonObject.get("error_code") == null) {
                    tokenService.refreshToken(merchantKey, RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                    return issueStampReward(requestBody);
                }

                String errorCode = (String) jsonObject.get("error_code");
                String message = (String) jsonObject.get("message");

                payload.put("error_code", errorCode);
                payload.put("message", message);

            } catch (IOException e) {
                e.printStackTrace();
            }


        } else {
            payload.put("message", "Merchant not found");
        }

        return payload;
    }

}
