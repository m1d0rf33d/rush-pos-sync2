package com.rush.service;

import com.rush.model.ApiResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by aomine on 11/14/16.
 */
@Service
public class FileUploadService {


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
}
