package com.rush.controller;

import com.rush.model.ApiResponse;
import com.rush.service.ErrorLogger;
import com.rush.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private FileUploadService fileUploadService;

    @RequestMapping(value = "/download/{merchantKey}", method = RequestMethod.GET)
    public void getMerchantUpdates(@PathVariable("merchantKey") String merchantKey,
                                   HttpServletResponse response) throws IOException, NullPointerException {

        File file = new File(fileUploadService.getUpdatePath(merchantKey));

        if (file.exists()) {
            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(file);
                OutputStream outputStream = response.getOutputStream();
                int length;
                byte[] buffer = new byte[5242880];
                while((length = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.flush();


                response.addHeader("Content-type", "octet-stream");
            } catch (FileNotFoundException e) {
                ErrorLogger.LOG.error(e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                ErrorLogger.LOG.error(e.getMessage());
                e.printStackTrace();
            } finally {
                inputStream.close();
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

    @RequestMapping(value = "/{merchantKey}/{version}", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse fetchUpdates(@PathVariable("merchantKey") String merchantKey,
                                    @PathVariable("version") String version) {
        return fileUploadService.checkForUpdates(merchantKey, version);
    }

}
