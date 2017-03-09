package com.rush.service.widget;

import com.rush.model.Merchant;
import com.rush.model.enums.RushTokenType;
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

    private HashMap<String, String> rushTokens;
    private HashMap<String, String> widgetTokens;

    @Value("${widget.auth.endpoint}")
    private String widgetAuthEndpoint;
    @Value("${widget.host}")
    private String widgetHost;
    @Value("${rush.host}")
    private String rushHost;
    @Value("${rush.auth.endpoint}")
    private String rushAuthEndpoint;

    public String getWidgetToken(Merchant merchant) {

        String token = widgetTokens.get(merchant.getUniqueKey());
        if (token != null) {
            return token;
        }


        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            String url = widgetHost + widgetAuthEndpoint;
            url = url.replace(":username", "admin").replace(":password", "admin");
            url = url.replace(":clientId", merchant.getClientId());

            String encodedSecret = merchant.getClientId() + ":" + merchant.getClientSecret();
            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader("Authorization", encodedSecret);
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
            token = result.toString();
            widgetTokens.put(merchant.getUniqueKey(), token);

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getRushtoken(Merchant merchant, RushTokenType rushTokenType) {

        String key = merchant.getUniqueKey() + ":" + rushTokenType.toString();
        String token = rushTokens.get(key);
        if (token != null) {
            return token;
        }

        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            String url = rushHost + rushAuthEndpoint;
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
