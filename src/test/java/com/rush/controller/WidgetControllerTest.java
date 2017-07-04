package com.rush.controller;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by aomine on 6/27/17.
 */

public class WidgetControllerTest {

    @Test
    public void loginEmployeeTest() throws IOException, ParseException {


        JSONObject json = new JSONObject();
        json.put("employee_login", "211");
        json.put("branch_id", "20b8c504-eea4-11e6-9dd1-066da3107b5d");
        json.put("merchant_type", "loyalty");
        json.put("merchant_key", "XlE2lRtdJ6Tq");

        StringEntity stringEntity = new StringEntity(json.toJSONString());

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost("http://52.74.190.173:8080/rush-pos-sync/api/widget/login/employee");
        httpPost.addHeader("Authorization", "Bearer 2c41e8de-0d6d-45a5-9544-f2a5a6da164d");
        httpPost.addHeader("Content-type", "application/json");
        httpPost.setEntity(stringEntity);

        HttpResponse response = httpClient.execute(httpPost);
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        httpClient.close();

        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(result.toString());
        // Then
        assertEquals(jsonObject.get("error_code"), "0x3");

    }

}
