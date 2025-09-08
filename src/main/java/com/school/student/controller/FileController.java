package com.school.student.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private final String dataDir = System.getProperty("os.name").toLowerCase().contains("win") ?
            "C:\\var\\log\\applications\\API\\dataprocessing\\" :
            "/var/log/applications/API/dataprocessing/";

    @GetMapping
    public ResponseEntity<List<String>> listFiles(@RequestParam(defaultValue = "excel") String type) {
        try {
            File folder = new File(dataDir);
            if (!folder.exists()) folder.mkdirs();

            String[] files = folder.list((dir, name) -> {
                if ("excel".equalsIgnoreCase(type)) return name.endsWith(".xlsx");
                else if ("csv".equalsIgnoreCase(type)) return name.endsWith(".csv");
                return false;
            });

            return ResponseEntity.ok(files != null ? Arrays.asList(files) : Collections.emptyList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }
}
