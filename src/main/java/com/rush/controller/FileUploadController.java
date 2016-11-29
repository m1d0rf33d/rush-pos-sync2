package com.rush.controller;

import com.rush.model.ApiResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Date;

/**
 * Created by aomine on 11/14/16.
 */
@RequestMapping("/api/updates")
@Controller
public class FileUploadController {

    @RequestMapping(value = "/download/{merchantKey}", method = RequestMethod.GET)
    public void getMerchantUpdates(@PathVariable("merchantKey") String merchantKey,
                                   HttpServletResponse response) {

        File file = new File(System.getProperty("catalina.home") + "/" + merchantKey + "/rush-update.jar");

        if (file.exists()) {
            try {
                InputStream inputStream = new FileInputStream(file);
                OutputStream outputStream = response.getOutputStream();
                int length;
                byte[] buffer = new byte[5242880];
                while((length = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.flush();
                inputStream.close();

                response.addHeader("Content-type", "octet-stream");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @RequestMapping(value = "/read", method = RequestMethod.GET)
    public void testing() {
        Date date = new Date();
        File file = new File(System.getProperty("catalina.home") + "/oAx3WZ34Ci8E/rush-update.jar");
        Long m1 = file.lastModified();
        Long m2 = 1479432618112l;
        if (m2 > m1) {
            String x = "";
        }
    }

    @RequestMapping(value = "/{merchantKey}/{dateModified}", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse fetchUpdates(@PathVariable("merchantKey") String merchantKey,
                                    @PathVariable("dateModified") Long dateModified) {
        ApiResponse apiResponse = new ApiResponse();
        File file = new File(System.getProperty("catalina.home") + "/" + merchantKey + "/rush-update.jar");
        if (file.exists()) {
            if (file.lastModified() > dateModified) {
                apiResponse.setData(file.length());
            }
        }
        apiResponse.setResponseCode("200");
        return apiResponse;
    }

}
