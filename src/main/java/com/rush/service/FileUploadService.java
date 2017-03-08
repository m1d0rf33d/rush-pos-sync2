package com.rush.service;

import com.rush.model.ApiResponse;
import com.rush.model.AppUpdate;
import com.rush.model.Merchant;
import com.rush.model.enums.MerchantStatus;
import com.rush.repository.AppUpdateRepository;
import com.rush.repository.MerchantRepository;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by aomine on 11/14/16.
 */
@Service
public class FileUploadService {

    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private AppUpdateRepository appUpdateRepository;


    public ApiResponse uploadFile(MultipartFile file) {
        boolean successUpload =false;
        if (file != null) {
          try {
              byte[] bytes = file.getBytes();

              // Creating the directory to store file
              String rootPath = System.getProperty("catalina.home");
              File dir = new File(rootPath + File.separator + "/updates");

              if (!dir.exists())
                  dir.mkdirs();

              // Create the file on server
              File serverFile = new File(dir.getAbsolutePath()
                      + File.separator + file.getOriginalFilename());
              BufferedOutputStream stream = new BufferedOutputStream(
                      new FileOutputStream(serverFile));
              stream.write(bytes);
              stream.close();
              successUpload = true;
          } catch (IOException e) {
              e.printStackTrace();
          }
        }
        ApiResponse apiResponse = new ApiResponse();
        if (successUpload) {
            apiResponse.setResponseCode("200");
        } else {
            apiResponse.setResponseCode("500");
        }

        return apiResponse;
    }

    public ApiResponse checkForUpdates(String merchantKey, String version) {
        boolean forUpdate = false;
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setResponseCode("200");
        JSONObject dataJSON = new JSONObject();

        Merchant merchant = merchantRepository.findOneByUniqueKeyAndStatus(merchantKey, MerchantStatus.ACTIVE);
        AppUpdate appUpdate = appUpdateRepository.findLatestVersionEntry(merchant.getId());
        if (appUpdate != null) {
            if (!appUpdate.getVersion().equals(version)) {
                forUpdate = true;
            }

            if (forUpdate) {
                File file = new File(appUpdate.getFilePath());
                dataJSON.put("fileSize", file.length());
                dataJSON.put("version", appUpdate.getVersion());
            }
        }

        apiResponse.setData(dataJSON);
        return apiResponse;
    }

    public String getUpdatePath(String merchantKey) {
        Merchant merchant = merchantRepository.findOneByUniqueKeyAndStatus(merchantKey, MerchantStatus.ACTIVE);
        AppUpdate appUpdate = appUpdateRepository.findLatestVersionEntry(merchant.getId());
        return appUpdate.getFilePath();
    }
}
