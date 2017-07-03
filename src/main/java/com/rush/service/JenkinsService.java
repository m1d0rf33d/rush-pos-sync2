package com.rush.service;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jdom.Content;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

/**
 * Created by m1d0rf33d on 6/30/17.
 */
@Service
@PropertySource("classpath:api.properties")
public class JenkinsService {


    @Value("${jenkins.host}")
    private String jenkinsHost;

    public boolean triggerJenkins() {
       try {
           CloseableHttpClient httpClient = HttpClientBuilder.create().build();
           String url = jenkinsHost + "/crumbIssuer/api/xml";
           HttpGet request = new HttpGet(url);
           request.addHeader("Authorization", "Basic YWRtaW46MTIzNDU2Nzg=");
           HttpResponse response = httpClient.execute(request);

           // use httpClient (no need to close it explicitly)
           BufferedReader rd = new BufferedReader(
                   new InputStreamReader(response.getEntity().getContent()));

           StringBuilder result = new StringBuilder();
           String line = "";
           while ((line = rd.readLine()) != null) {
               result.append(line);
           }


           org.jdom.input.SAXBuilder saxBuilder = new SAXBuilder();
           org.jdom.Document doc = saxBuilder.build(new StringReader(result.toString()));
           Content content = doc.getRootElement().getContent(0);
           String crumb = content.getValue();

           HttpPost httpPost = new HttpPost(jenkinsHost + "/job/rush-pos-sync/build");
           httpPost.addHeader("Authorization", "Basic YWRtaW46NWMwNTczOGI1NDM1NTBhNGE0N2FjODJkYjY2MTIzMzE=");
           httpPost.addHeader("Jenkins-Crumb", crumb);
           httpClient.execute(httpPost);


           httpClient.close();
           return true;
       } catch (ClientProtocolException e) {
           e.printStackTrace();
       } catch (IOException e) {
           e.printStackTrace();
       } catch (JDOMException e) {
           e.printStackTrace();
       }

        return false;
    }

}
