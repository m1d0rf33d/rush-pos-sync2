package com.rush.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by aomine on 6/19/17.
 */
@Service
public class FileReaderService {

    private  String rushLog = "Rush.log";
    private  String rushErrorLog = "RushError.log";


    public List<String> getLogs(String type) {
        List<String> logs = new ArrayList<>();

        String fileName = type.equals("error") ? rushErrorLog : rushLog;

        String catalinaHome = System.getProperty("catalina.home");
        String folder = "/logs/";

        try (Stream<String> stream = Files.lines(Paths.get(catalinaHome + folder + fileName))) {
            stream.forEach(s -> {
                logs.add(s);
            });
        } catch (IOException e) {
            e.printStackTrace();
            ErrorLogger.LOG.error(e.getMessage());
        }
        return logs;
    }

}
