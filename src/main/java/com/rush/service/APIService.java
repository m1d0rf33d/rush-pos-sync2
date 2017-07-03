package com.rush.service;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/** All API calls that will be made going to Rush API should be here / API Module
 *
 *  @author m1d0rf33d
 */
@Service
public class APIService {

    public JSONObject call(String url, List<NameValuePair> params, String method, String token) throws IOException {

       try {
            HttpResponse response = null;
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            //POST request
            if (method.equalsIgnoreCase("post")) {
                HttpPost httpPost = new HttpPost(url);
                if (params != null) {
                    httpPost.setEntity(new UrlEncodedFormEntity(params));
                }

                if (token != null) {
                    httpPost.addHeader("Authorization", "Bearer "+ token);
                }
                response = httpClient.execute(httpPost);
            }
            //GET request
            if (method.equalsIgnoreCase("get")) {
                HttpGet request = new HttpGet(url);
                request.addHeader("content-type", "application/json");
                if (token != null) {
                    request.addHeader("Authorization", "Bearer "+ token);
                }
                response = httpClient.execute(request);
            }

            // use httpClient (no need to close it explicitly)
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuilder result = new StringBuilder();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            httpClient.close();

            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(result.toString());
            return jsonObject;
       } catch (ParseException e) {
           e.printStackTrace();
           ErrorLogger.LOG.error(e.getMessage());
       }

        return null;
    }
}
