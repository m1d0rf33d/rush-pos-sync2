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

    private static String fileName = "Rush.log";


    public List<String> getLogs() {
        List<String> logs = new ArrayList<>();

        String catalinaHome = System.getProperty("catalina.home");
        String folder = "/logs/";

        try (Stream<String> stream = Files.lines(Paths.get(catalinaHome + folder + fileName))) {
            stream.forEach(s -> {
                logs.add(s);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return logs;
    }

}
