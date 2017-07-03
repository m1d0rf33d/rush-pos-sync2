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
    
    private static final String MERCHANT_KEY = "merchant_key";
    private static final String EMPLOYEE_ID = "employee_id";
    private static final String APP_STATE = "app_state";
    private static final String MOBILE_NO = "mobile_no";
    private static final String MERCHANT_TYPE_REF = ":merchant_type";
    private static final String EMPLOYEE_ID_REF = ":employee_id";
    private static final String ERROR_CODE = "error_code";
    private static final String MESSAGE = "message";
    private static final String GENDER = "gender";
    private static final String BIRTHDATE = "birthdate";
    private static final String EMAIL = "email";
    private static final String ACCOUNT_NUMBER = "account_number";
    private static final String POINTS = "points";
    private static final String REDEEM_ID = "redeem_id";
    private static final String QUANTITY = "quantity";
    private static final String TRANSACTIONS = "transactions";
    private static final String CUSTOMER_ID_REF  = ":customer_id";
    private static final String TITLE_ID = "title_id";
    private static final String MERCHANT_TYPE = "merchant_type";
    private static final String EARNING_PESO = "earning_peso";
    private static final String CUSTOMER_ID = "customer_id";
    private static final String OR_NO = "or_no";
    private static final String AMOUNT = "amount";
    private static final String MERCHANT_NOT_FOUND = "Merchant not found";
    private static final String REWARD_ID = "reward_id";
    private static final String ERRORS = "errors";



    public JSONObject loginMember(JSONObject requestBody) {

        String merchantKey  = (String) requestBody.get(MERCHANT_KEY);
        String employeeId   = (String) requestBody.get(EMPLOYEE_ID);
        String appState     = (String) requestBody.get(APP_STATE);
        String mobileNumber = (String) requestBody.get(MOBILE_NO);

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
            String url = rushHost + endpoint.replace(MERCHANT_TYPE_REF, merchant.getMerchantType().getValue().toLowerCase());
            url = url.replace(EMPLOYEE_ID_REF, employeeId);
            String token = tokenService.getRushtoken(merchantKey, RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
            try {
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair(MOBILE_NO, mobileNumber));
                JSONObject jsonObject = apiService.call(url, params, "post", token);
                if (jsonObject != null) {
                    if (jsonObject.get(ERROR_CODE) == null) {
                        tokenService.refreshToken(merchantKey, RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                        return loginMember(requestBody);
                    }

                    payload.put(ERROR_CODE, jsonObject.get(ERROR_CODE));
                    payload.put(MESSAGE, jsonObject.get(MESSAGE));
                    if (jsonObject.get(ERROR_CODE).equals("0x0")) {
                        JSONObject d = (JSONObject) jsonObject.get("data");

                        JSONObject member = new JSONObject();
                        member.put(MOBILE_NO,d.get(MOBILE_NO));
                        member.put(GENDER, d.get(GENDER));
                        member.put("profile_id", d.get("profile_id"));
                        member.put("name", d.get("name"));
                        member.put(BIRTHDATE, d.get(BIRTHDATE));
                        member.put(EMAIL, d.get(EMAIL));
                        member.put("registration_date", d.get("registration_date"));
                        member.put("id", d.get("id"));
                        member.put(ACCOUNT_NUMBER, d.get(ACCOUNT_NUMBER));
                        member.put("account_name", d.get("account_name"));
                        data.put("member", member);

                        if (merchant.getMerchantType().equals(MerchantType.LOYALTY)) {
                            String points = getCurrentPoints((String) member.get("id"),(String) requestBody.get(EMPLOYEE_ID),merchant);
                            member.put(POINTS, points);

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
                                    reward.put(REDEEM_ID,  redeem.get("id"));
                                    reward.put("date", redeem.get("date"));
                                    reward.put(QUANTITY, redeem.get(QUANTITY));
                                    reward.put("claimed", redeem.get("claimed"));
                                    rewards.add(reward);
                                }
                                data.put("rewards", rewards);
                            }


                            if (merchant.getMerchantClassification().equals(MerchantClassification.GLOBE_SG)) {
                                data.put("account_points", getAccountPoints(merchant, (String) member.get(ACCOUNT_NUMBER), employeeId));
                            }
                        } else {
                            JSONObject customerCard = getCustomerCard(merchant, employeeId, (String) member.get("id"));
                            data.put("customer_card", customerCard);
                        }

                        if (appState.equals(AppState.MEMBER_INQUIRY.toString())) {
                            data.put(TRANSACTIONS, getCustomerTransactions(merchant, (String) member.get("id")));
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

            String url = rushHost + endpoint.replace(MERCHANT_TYPE_REF, merchant.getMerchantType().getValue().toLowerCase());
            url = url.replace(CUSTOMER_ID_REF, customerId);
            url = url.replace(EMPLOYEE_ID_REF, employeeId);
            JSONObject jsonObject = apiService.call(url, null, "get", token);
            if (jsonObject != null) {
                if (jsonObject.get(ERROR_CODE) == null) {
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
        params.add(new BasicNameValuePair(EMAIL, (String) requestBody.get(EMAIL)));
        params.add(new BasicNameValuePair(MOBILE_NO, (String) requestBody.get(MOBILE_NO)));
        params.add(new BasicNameValuePair("pin", (String) requestBody.get("pin")));

        String birthDate = (String) requestBody.get(BIRTHDATE);
        if (birthDate != null) {
            params.add(new BasicNameValuePair(BIRTHDATE, birthDate));
        }
        String gender = (String) requestBody.get(GENDER);
        if (gender != null) {
            params.add(new BasicNameValuePair(GENDER, gender));
        }
        if (requestBody.get(ACCOUNT_NUMBER) != null) {
            params.add(new BasicNameValuePair(ACCOUNT_NUMBER, (String) requestBody.get(ACCOUNT_NUMBER)));
        }
        if (requestBody.get(TITLE_ID) != null) {
            params.add(new BasicNameValuePair(TITLE_ID, ((Integer) requestBody.get(TITLE_ID)).toString()));
        }


        String employeeId = (String) requestBody.get(EMPLOYEE_ID);
        String merchantKey = (String) requestBody.get(MERCHANT_KEY);

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
            url = url.replace(EMPLOYEE_ID_REF, employeeId).replace(MERCHANT_TYPE_REF, merchant.getMerchantType().getValue().toLowerCase());

            try {
                JSONObject rushResponse = apiService.call(url, params, "post", token);
                if (rushResponse != null) {
                    if (rushResponse.get(ERROR_CODE) == null) {
                        tokenService.refreshToken(merchantKey, RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                        return registerMember(requestBody);
                    }

                    payload.put(ERROR_CODE, rushResponse.get(ERROR_CODE));
                    payload.put(MESSAGE, rushResponse.get(MESSAGE));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            payload.put(MESSAGE, "Merchant not found.");
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
            url = url.replace(EMPLOYEE_ID_REF, employeeId).replace(MERCHANT_TYPE_REF, merchant.getMerchantType().getValue().toLowerCase()).replace(CUSTOMER_ID_REF, customerId);
            JSONObject jsonObject = apiService.call(url, null, "get", token);
            if (jsonObject != null) {

                if (jsonObject.get(ERROR_CODE) == null) {
                    tokenService.refreshToken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                    return getPointsRule(employeeId, customerId, merchant);
                }

                if (jsonObject.get(ERROR_CODE).equals("0x0")) {
                    JSONObject dataJSON = (JSONObject) jsonObject.get("data");
                    payload.put(EARNING_PESO, dataJSON.get(EARNING_PESO));
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


        Merchant merchant = merchantRepository.findOneByUniqueKeyAndStatus((String) requestBody.get(MERCHANT_KEY), MerchantStatus.ACTIVE);
        if (merchant != null) {
            String endpoint;
            if (merchant.getMerchantClassification().equals(MerchantClassification.GLOBE_SG)) {
                endpoint = sgCustomerEarnEndpoint;
            } else {
                endpoint = earnPointsEndpoint;
            }

            String url = rushHost + endpoint;
            url = url.replace(CUSTOMER_ID_REF, (String) requestBody.get(CUSTOMER_ID));
            url = url.replace(EMPLOYEE_ID_REF, (String) requestBody.get(EMPLOYEE_ID));
            url = url.replace(MERCHANT_TYPE_REF, ((String) requestBody.get(MERCHANT_TYPE)).toLowerCase());
            String merchantKey = (String) requestBody.get(MERCHANT_KEY);
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("uuid", (String) requestBody.get(CUSTOMER_ID)));
            params.add(new BasicNameValuePair(OR_NO, (String) requestBody.get(OR_NO)));
            params.add(new BasicNameValuePair(AMOUNT, (String) requestBody.get(AMOUNT)));

            //validate points
            JSONObject pointsRule = getPointsRule((String) requestBody.get(EMPLOYEE_ID), (String) requestBody.get(CUSTOMER_ID), merchant);
            Long earningPeso = (Long) pointsRule.get(EARNING_PESO);
            String amountStr = (String) requestBody.get(AMOUNT);
            Long amt;
            try {
                amt = Long.parseLong(amountStr);
            } catch (NumberFormatException e) {
                Double d = Double.parseDouble(amountStr);
                amt = d.longValue();
            }
            if (earningPeso > amt) {
                payload.put(ERROR_CODE, "0x1");
                payload.put(MESSAGE, "Sorry! Amount entered is below the minimum purchase of P" + earningPeso);
                return payload;
            }

            String token = tokenService.getRushtoken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
            try {
                JSONObject jsonObject = apiService.call(url, params, "post", token);
                if (jsonObject != null) {

                    if (jsonObject.get(ERROR_CODE) == null) {
                        tokenService.refreshToken(merchantKey, RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                        return earnPoints(requestBody);
                    }

                    String errorCode = (String) jsonObject.get(ERROR_CODE);
                    payload.put(ERROR_CODE, errorCode);

                    if (errorCode.equals("0x0")) {
                        String points = getCurrentPoints((String) requestBody.get(CUSTOMER_ID), (String) requestBody.get(EMPLOYEE_ID), merchant);
                        payload.put(POINTS, points);
                    }
                    payload.put(MESSAGE,jsonObject.get(MESSAGE));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            payload.put("mesage", MERCHANT_NOT_FOUND);
        }

        return payload;
    }

    public JSONObject guestPurchase(JSONObject requestBody) {

        JSONObject payload = new JSONObject();

        String merchantType = (String) requestBody.get(MERCHANT_TYPE);
        String merchantKey = (String) requestBody.get(MERCHANT_KEY);
        String mobileNumber = (String) requestBody.get(MOBILE_NO);
        String orNumber = (String) requestBody.get(OR_NO);
        String employeeId = (String)  requestBody.get(EMPLOYEE_ID);
        String amount = (String) requestBody.get(AMOUNT);

        String url = rushHost + earnGuestEndpoint;
        url = url.replace(EMPLOYEE_ID_REF, employeeId);
        url = url.replace(MERCHANT_TYPE_REF, merchantType.toLowerCase());


        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(MOBILE_NO, mobileNumber));
        params.add(new BasicNameValuePair(OR_NO, orNumber));
        params.add(new BasicNameValuePair(AMOUNT, amount));

        Merchant merchant = merchantRepository.findOneByUniqueKeyAndStatus(merchantKey, MerchantStatus.ACTIVE);
        if (merchant != null) {

            String token = tokenService.getRushtoken(merchantKey, RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
            try {
                JSONObject jsonObject = apiService.call(url, params, "post", token);
                if (jsonObject != null) {

                    if (jsonObject.get(ERROR_CODE) == null) {
                        tokenService.refreshToken(merchantKey, RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                        return guestPurchase(requestBody);
                    }

                    String errorCode = (String) jsonObject.get(ERROR_CODE);
                    String mesage = (String) jsonObject.get(MESSAGE);
                    payload.put(ERROR_CODE, errorCode);
                    payload.put(MESSAGE, mesage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            payload.put(MESSAGE, MERCHANT_NOT_FOUND);
        }

        return payload;
    }

    public List<JSONObject> getMerchantRewards(Merchant merchant) {
        String endpoint = "";
        if (merchant.getMerchantClassification().equals(MerchantClassification.GLOBE_SG)) {
            endpoint = sgMerchantRewardsEndpoint;
        } else {
            endpoint = merchantRewardsEndpoint.replace(MERCHANT_TYPE_REF, merchant.getMerchantType().getValue().toLowerCase());
        }
        String url = rushHost + endpoint;
        try {
            String token = tokenService.getRushtoken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
            JSONObject jsonObject = apiService.call(url, new ArrayList<>(), "get", token);
            if (jsonObject != null) {

                if (jsonObject.get(ERROR_CODE) == null) {
                    tokenService.refreshToken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                    return getMerchantRewards(merchant);
                }

                return (ArrayList) jsonObject.get("data");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public JSONObject redeemReward(JSONObject requestBody) {

        JSONObject payload = new JSONObject();

        String employeeId = (String) requestBody.get(EMPLOYEE_ID);
        String merchantKey = (String) requestBody.get(MERCHANT_KEY);
        String merchantType = (String) requestBody.get(MERCHANT_TYPE);
        String customerId = (String) requestBody.get(CUSTOMER_ID);
        String rewardId = (String) requestBody.get(REWARD_ID);
        String pin = (String) requestBody.get("pin");
        String quantity = (String) requestBody.get(QUANTITY);

        Merchant merchant = merchantRepository.findOneByUniqueKeyAndStatus(merchantKey, MerchantStatus.ACTIVE);
        if (merchant != null) {
            String token = tokenService.getRushtoken(merchantKey, RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("pin", pin));
            params.add(new BasicNameValuePair(QUANTITY, quantity));

            String endpoint = "";
            if (merchant.getMerchantClassification().equals(MerchantClassification.GLOBE_SG)) {
                endpoint = sgRedeemEndpoint;
            } else {
                endpoint = redeemRewardsEndpoint.replace(MERCHANT_TYPE_REF, merchantType.toLowerCase());
            }

            String url = rushHost + endpoint;
            url = url.replace(CUSTOMER_ID_REF, customerId);
            url = url.replace(EMPLOYEE_ID_REF, employeeId);
            url = url.replace(":reward_id", rewardId);
            try {
                JSONObject jsonObject = apiService.call(url, params, "post", token);
                if (jsonObject != null) {
                    String errorCode = (String) jsonObject.get(ERROR_CODE);
                    String message = (String) jsonObject.get(MESSAGE);

                    if (errorCode == null) {
                        tokenService.refreshToken(merchantKey, RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                        return redeemReward(requestBody);
                    }
                    JSONObject data = new JSONObject();
                    if (errorCode.equals("0x0")) {
                        String points = getCurrentPoints(customerId, employeeId,merchant);
                        data.put(POINTS, points);
                        payload.put("data", data);
                    }

                    payload.put(ERROR_CODE, errorCode);
                    payload.put(MESSAGE, message);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            payload.put(MESSAGE, MERCHANT_NOT_FOUND);
        }

        return payload;
    }


    public JSONObject payWithPoints(JSONObject requestBody) {

        JSONObject payload = new JSONObject();

        String employeeId = (String) requestBody.get(EMPLOYEE_ID);
        String merchantType = (String) requestBody.get(MERCHANT_TYPE);
        String merchantKey = (String) requestBody.get(MERCHANT_KEY);
        String customerId = (String) requestBody.get(CUSTOMER_ID);

        String pin = (String) requestBody.get("pin");
        String points = (String) requestBody.get(POINTS);
        String orNumber = (String) requestBody.get(OR_NO);
        String amount = (String) requestBody.get(AMOUNT);

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("employee_uuid", employeeId));
        params.add(new BasicNameValuePair(OR_NO, orNumber));
        params.add(new BasicNameValuePair(AMOUNT, amount));
        params.add(new BasicNameValuePair(POINTS, points));
        params.add(new BasicNameValuePair("pin", pin));

        Merchant merchant = merchantRepository.findOneByUniqueKeyAndStatus(merchantKey, MerchantStatus.ACTIVE);
        if (merchant != null) {
            String url = rushHost + payPointsEndpoint.replace(MERCHANT_TYPE_REF, merchantType);
            url = url.replace(CUSTOMER_ID_REF, customerId);

            String token = tokenService.getRushtoken(merchantKey, RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
            try {
                JSONObject jsonObject = apiService.call(url, params, "post", token);
                if (jsonObject != null) {

                    if (jsonObject.get(ERROR_CODE) == null) {
                        tokenService.refreshToken(merchantKey, RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                        return payWithPoints(requestBody);
                    }

                    String errorCode = (String) jsonObject.get(ERROR_CODE);
                    String message = (String) jsonObject.get(MESSAGE);
                    payload.put(ERROR_CODE, errorCode);
                    payload.put(MESSAGE, message);

                    if (errorCode.equals("0x0")) {
                        String remainingPoints = getCurrentPoints(customerId, employeeId, merchant);
                        JSONObject data = new JSONObject();
                        data.put(POINTS, remainingPoints);
                        payload.put("data", data);
                    }

                    payload.put(ERRORS, jsonObject.get(ERRORS));

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            payload.put(MESSAGE, MERCHANT_NOT_FOUND);
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
        String url = rushHost + endpoint.replace(MERCHANT_TYPE_REF, merchant.getMerchantType().getValue().toLowerCase());
        url = url.replace(EMPLOYEE_ID_REF, employeeId);
        url = url.replace(CUSTOMER_ID_REF, customerId);

        String token = tokenService.getRushtoken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
        try {
            JSONObject jsonObject = apiService.call(url, null, "get", token);
            if (jsonObject != null) {

                if (jsonObject.get(ERROR_CODE) == null) {
                    tokenService.refreshToken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                    return getUnclaimedRewards(employeeId, customerId, merchant);
                }

                if (jsonObject.get(ERROR_CODE).equals("0x0")) {
                    return (ArrayList) jsonObject.get("data");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public JSONObject issueReward(JSONObject requestBody) {

        JSONObject payload = new JSONObject();

        String employeeId = (String) requestBody.get(EMPLOYEE_ID);
        String merchantType = (String) requestBody.get(MERCHANT_TYPE);
        String merchantKey = (String) requestBody.get(MERCHANT_KEY);
        String customerId = (String) requestBody.get(CUSTOMER_ID);
        String rewardId = (String) requestBody.get(REDEEM_ID);
        String quantity = (String) requestBody.get(QUANTITY);

        Merchant merchant = merchantRepository.findOneByUniqueKeyAndStatus(merchantKey, MerchantStatus.ACTIVE);
        if (merchant != null) {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(REDEEM_ID, rewardId));
            params.add(new BasicNameValuePair(QUANTITY, quantity));

            String endpoint = "";
            if (merchant.getMerchantClassification().equals(MerchantClassification.GLOBE_SG)) {
                endpoint = sgIssueEndpoint;
            } else {
                endpoint = claimRewardsEndpoint.replace(MERCHANT_TYPE_REF, merchantType.toLowerCase());
            }

            String url = rushHost + endpoint;
            url = url.replace(CUSTOMER_ID_REF, customerId);
            url = url.replace(EMPLOYEE_ID_REF, employeeId);

            String token = tokenService.getRushtoken(merchantKey, RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
            try {
                JSONObject jsonObject = apiService.call(url, params, "post", token);
                if (jsonObject != null) {

                    if (jsonObject.get(ERROR_CODE) == null) {
                        tokenService.refreshToken(merchantKey, RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                        return issueReward(requestBody);
                    }

                    String errorCode = (String) jsonObject.get(ERROR_CODE);
                    String message = (String) jsonObject.get(MESSAGE);

                    payload.put(MESSAGE, message);
                    payload.put(ERROR_CODE, errorCode);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            payload.put(MESSAGE, MERCHANT_NOT_FOUND);
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
                String errorCode = (String) jsonObject.get(ERROR_CODE);
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
        return new ArrayList<>();
    }


    public JSONObject sendOfflineTransacions(JSONObject requestBody) {

        String merchantKey  = (String) requestBody.get(MERCHANT_KEY);
        String employeeId   = (String) requestBody.get(EMPLOYEE_ID);
        String appState     = (String) requestBody.get(APP_STATE);
        String merchantType = (String)requestBody.get(MERCHANT_TYPE);

        JSONObject reqBody = new JSONObject();
        reqBody.put(MERCHANT_KEY, merchantKey);
        reqBody.put(MERCHANT_TYPE, merchantType);
        reqBody.put(APP_STATE, appState);
        reqBody.put(EMPLOYEE_ID, employeeId);

        List<JSONObject> transactions = (ArrayList) requestBody.get(TRANSACTIONS);
        List<String> result = new ArrayList<>();
        for (HashMap transaction : transactions) {
            StringBuilder sb = new StringBuilder();
            sb.append("mobileNumber=" + transaction.get(MOBILE_NO));
            sb.append(":");
            sb.append("totalAmount=" + transaction.get(AMOUNT));
            sb.append(":");
            sb.append("orNumber=" + transaction.get(OR_NO));
            sb.append(":");
            sb.append("date=" + transaction.get("date"));
            sb.append(":");

            reqBody.put(MOBILE_NO, transaction.get(MOBILE_NO));

            JSONObject payload = loginMember(reqBody);

            String errorCode = (String) payload.get(ERROR_CODE);

            if (errorCode.equals("0x0")) {
                JSONObject data = (JSONObject) payload.get("data");
                JSONObject member = (JSONObject) data.get("member");

                reqBody.put(OR_NO, transaction.get(OR_NO));
                reqBody.put(AMOUNT, transaction.get(AMOUNT));
                reqBody.put(CUSTOMER_ID, member.get("id"));

                payload = earnPoints(reqBody);
                errorCode = (String) payload.get(ERROR_CODE);

                if (errorCode.equals("0x0")) {
                    sb.append("status=" + "Submitted");
                    sb.append(":");
                    sb.append("message=Points earned.");
                } else {
                    JSONObject error = (JSONObject) payload.get(ERRORS);
                    String errorMessage = "";
                    if (error != null) {
                        if (error.get(OR_NO) != null) {
                            List<String> l = (ArrayList<String>) error.get(OR_NO);
                            errorMessage = l.get(0);
                        }
                        if (error.get(AMOUNT) != null) {
                            List<String> l = (ArrayList<String>) error.get(AMOUNT);
                            errorMessage = l.get(0);
                        }
                    }
                    if (payload.get(MESSAGE) != null) {
                        errorMessage = (String) payload.get(MESSAGE);
                    }
                    sb.append("status=" + "Failed");
                    sb.append(":");
                    sb.append("message=" + errorMessage);
                }


            } else {
                sb.append("status=" + "Failed");
                sb.append(":");
                sb.append("message=" + payload.get(MESSAGE));
            }
            result.add(sb.toString());
        }

        JSONObject payload = new JSONObject();
        payload.put(ERROR_CODE, "0x0");
        payload.put(MESSAGE, "Submit offline transaction successful.");
        JSONObject data = new JSONObject();
        data.put(TRANSACTIONS, result);
        payload.put("data", data);

        return payload;
    }

    private String getAccountPoints(Merchant merchant, String accountNumber, String employeeId) {
        String endpoint = sgAccountSummaryEndpoint;
        String url = rushHost + endpoint.replace(EMPLOYEE_ID_REF, employeeId);
        String token = tokenService.getRushtoken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());

        try {
            JSONObject jsonObject = apiService.call(url, null, "get", token);
            if (jsonObject != null) {
                if (jsonObject.get(ERROR_CODE) == null) {
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
        url = url.replace(EMPLOYEE_ID_REF, employeeId);
        url = url.replace(CUSTOMER_ID_REF, customerId);

        try {
            JSONObject jsonObject = apiService.call(url, null, "get", token);
            if (jsonObject != null) {
                if (jsonObject.get(ERROR_CODE) == null) {
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

        String merchantKey = (String) requestBody.get(MERCHANT_KEY);
        String employeeId = (String) requestBody.get(EMPLOYEE_ID);
        String customerId = (String) requestBody.get(CUSTOMER_ID);
        String amount = (String) requestBody.get(AMOUNT);

        Merchant merchant = merchantRepository.findOneByUniqueKeyAndStatus(merchantKey, MerchantStatus.ACTIVE);
        if (merchant != null) {

            String url = rushHost + earnStampsEndpoint;
            url = url.replace(EMPLOYEE_ID_REF, employeeId);
            url = url.replace(CUSTOMER_ID_REF, customerId);

            String token = tokenService.getRushtoken(merchantKey, RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
            try {
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair(AMOUNT, amount));
                JSONObject jsonObject = apiService.call(url, params, "post", token);
                if (jsonObject.get(ERROR_CODE) == null) {
                    tokenService.refreshToken(merchantKey, RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                    return earnStamp(requestBody);
                }

                String errorCode = (String) jsonObject.get(ERROR_CODE);
                String message = (String) jsonObject.get(MESSAGE);
                if (errorCode.equals("0x0")) {
                    Long stampCount = getStampsCount(merchant, employeeId, customerId);
                    JSONObject data = new JSONObject();
                    data.put("stamp_count", stampCount);
                    payload.put("data", data);
                }
                payload.put(MESSAGE, message);
                payload.put(ERROR_CODE, errorCode);

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            payload.put(MESSAGE, MERCHANT_NOT_FOUND);
        }

        return payload;
    }


    public Long getStampsCount(Merchant merchant, String employeeId, String customerId) {
        try {
            String url = rushHost + stampsCountEndpoint;
            url = url.replace(EMPLOYEE_ID_REF, employeeId);
            url = url.replace(CUSTOMER_ID_REF, customerId);

            String token = tokenService.getRushtoken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
            JSONObject jsonObject = apiService.call(url, null, "get", token);
            if (jsonObject.get(ERROR_CODE) == null) {
                tokenService.refreshToken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                return  getStampsCount(merchant, employeeId, customerId);
            }

            if (jsonObject.get(ERROR_CODE).equals("0x0")) {
                return (Long) jsonObject.get("data");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject redeemStamp(JSONObject requestBody) {
        JSONObject payload = new JSONObject();

        String employeeId = (String) requestBody.get(EMPLOYEE_ID);
        String customerId = (String) requestBody.get(CUSTOMER_ID);
        String rewardId = (String) requestBody.get(REWARD_ID);
        String pin = (String) requestBody.get("pin");
        String merchantKey = (String) requestBody.get(MERCHANT_KEY);

        Merchant merchant = merchantRepository.findOneByUniqueKeyAndStatus(merchantKey, MerchantStatus.ACTIVE);
        if (merchant != null) {
            String url = rushHost + redeemStampEndpoint;
            url = url.replace(EMPLOYEE_ID_REF, employeeId);
            url = url.replace(CUSTOMER_ID_REF, customerId);
            url = url.replace(":reward_id", rewardId);

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("pin", pin));
            String token = tokenService.getRushtoken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
            try {
                JSONObject jsonObject = apiService.call(url, params, "post", token);
                if (jsonObject.get(ERROR_CODE) == null) {
                    tokenService.refreshToken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                    return redeemStamp(requestBody);
                }
                String message = (String) jsonObject.get(MESSAGE);
                String errorCode = (String) jsonObject.get(ERROR_CODE);
                if (errorCode.equals("0x0")) {
                    JSONObject customerCard = getCustomerCard(merchant, employeeId, customerId);
                    JSONObject data = new JSONObject();
                    data.put("customer_card", customerCard);
                    payload.put("data", data);
                }
                payload.put(MESSAGE, message);
                payload.put(ERROR_CODE, errorCode);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return payload;
    }

    public JSONObject issueStampReward(JSONObject requestBody) {

        JSONObject payload = new JSONObject();

        String merchantKey = (String) requestBody.get(MERCHANT_KEY);
        String employeeId = (String) requestBody.get(EMPLOYEE_ID);
        String customerId = (String) requestBody.get(CUSTOMER_ID);
        String rewardId = (String) requestBody.get(REWARD_ID);
        String pin = (String) requestBody.get("pin");

        Merchant merchant = merchantRepository.findOneByUniqueKeyAndStatus(merchantKey, MerchantStatus.ACTIVE);
        if (merchant != null) {

            String token = tokenService.getRushtoken(merchant.getUniqueKey(), RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("pin", pin));

            String url = rushHost + issueStampRewardEndpoint;
            url = url.replace(EMPLOYEE_ID_REF, employeeId);
            url = url.replace(CUSTOMER_ID_REF, customerId);
            url = url.replace(":rewards_id", rewardId);

            try {
                JSONObject jsonObject = apiService.call(url, params, "post", token);
                if (jsonObject.get(ERROR_CODE) == null) {
                    tokenService.refreshToken(merchantKey, RushTokenType.MERCHANT_APP, merchant.getMerchantClassification());
                    return issueStampReward(requestBody);
                }

                String errorCode = (String) jsonObject.get(ERROR_CODE);
                String message = (String) jsonObject.get(MESSAGE);

                payload.put(ERROR_CODE, errorCode);
                payload.put(MESSAGE, message);

            } catch (IOException e) {
                e.printStackTrace();
            }


        } else {
            payload.put(MESSAGE, MERCHANT_NOT_FOUND);
        }

        return payload;
    }

}
