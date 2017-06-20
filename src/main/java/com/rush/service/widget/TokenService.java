package com.rush.service.widget;

import com.rush.model.Merchant;
import com.rush.model.enums.MerchantClassification;
import com.rush.model.enums.MerchantStatus;
import com.rush.model.enums.RushTokenType;
import com.rush.repository.MerchantRepository;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by aomine on 3/9/17.
 */
@Service
@PropertySource("classpath:api.properties")
public class TokenService {

    private HashMap<String, String> rushTokens = new HashMap<>();
    private HashMap<String, String> widgetTokens = new HashMap<>();

    @Value("${widget.auth.endpoint}")
    private String widgetAuthEndpoint;
    @Value("${widget.host}")
    private String widgetHost;
    @Value("${rush.host}")
    private String rushHost;
    @Value("${rush.auth.endpoint}")
    private String rushAuthEndpoint;
    @Value("${sg.auth.endpoint}")
    private String sgAuthEndpoint;

    @Autowired
    private MerchantRepository merchantRepository;

    public String getWidgetToken(String merchantKey) {

        String token = widgetTokens.get(merchantKey);
        if (token != null) {
            return token;
        }

        try {
            Merchant merchant = merchantRepository.findOneByUniqueKeyAndStatus(merchantKey, MerchantStatus.ACTIVE);

            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            String url = widgetHost + widgetAuthEndpoint;
            //This is so bad hahahahaha
            url = url.replace(":username", "rushadmin").replace(":password", "12345678");
            url = url.replace(":clientId", merchant.getClientId());

            String str = merchant.getClientId() + ":" + merchant.getClientSecret();
            String encodedSecret = Base64.encodeBase64String(str.getBytes());
            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader("Authorization", "Basic " + encodedSecret);
            httpPost.addHeader("Content-Type", "application/json");
            HttpResponse response = httpClient.execute(httpPost);

            // use httpClient (no need to close it explicitly)
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            httpClient.close();
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(result.toString());
            token = (String) jsonObject.get("access_token");
            widgetTokens.put(merchant.getUniqueKey(), token);
            return token;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getRushtoken(String merchantKey, RushTokenType rushTokenType, MerchantClassification classification) {

        String key = merchantKey + ":" + rushTokenType.toString();
        String token = rushTokens.get(key);
        if (token != null) {
            return token;
        }

        try {
            Merchant merchant = merchantRepository.findOneByUniqueKeyAndStatus(merchantKey, MerchantStatus.ACTIVE);
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            String endpoint = "";
            if (classification.equals(MerchantClassification.BASIC)) {
                endpoint = rushAuthEndpoint;
            } else if (classification.equals(MerchantClassification.GLOBE_SG)) {
                endpoint = sgAuthEndpoint;
            }
            String url = rushHost + endpoint;
            String appKey, appSecret;
            if (rushTokenType.equals(RushTokenType.MERCHANT_APP)) {
                appKey = merchant.getMerchantApiKey();
                appSecret = merchant.getMerchantApiSecret();
            } else {
                appKey = merchant.getCustomerApiKey();
                appSecret = merchant.getCustomerApiSecret();
            }

            HttpPost httpPost = new HttpPost(url);
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("app_key", appKey));
            params.add(new BasicNameValuePair("app_secret", appSecret));
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse response = httpClient.execute(httpPost);

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            httpClient.close();
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(result.toString());
            token = (String) jsonObject.get("token");
            rushTokens.put(key, token);
            return token;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String refreshToken(String merchantKey, RushTokenType rushTokenType, MerchantClassification classification) {

        String key = merchantKey + ":" + rushTokenType.toString();
        String token = rushTokens.get(key);
        if (token != null) {
            rushTokens.remove(key);
        }

        try {
            Merchant merchant = merchantRepository.findOneByUniqueKeyAndStatus(merchantKey, MerchantStatus.ACTIVE);
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            String endpoint = "";
            if (classification.equals(MerchantClassification.BASIC)) {
                endpoint = rushAuthEndpoint;
            } else if (classification.equals(MerchantClassification.GLOBE_SG)) {
                endpoint = sgAuthEndpoint;
            }
            String url = rushHost + endpoint;
            String appKey, appSecret;
            if (rushTokenType.equals(RushTokenType.MERCHANT_APP)) {
                appKey = merchant.getMerchantApiKey();
                appSecret = merchant.getMerchantApiSecret();
            } else {
                appKey = merchant.getCustomerApiKey();
                appSecret = merchant.getCustomerApiSecret();
            }

            HttpPost httpPost = new HttpPost(url);
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("app_key", appKey));
            params.add(new BasicNameValuePair("app_secret", appSecret));
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse response = httpClient.execute(httpPost);

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            httpClient.close();
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(result.toString());
            token = (String) jsonObject.get("token");
            rushTokens.put(key, token);
            return token;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

}
