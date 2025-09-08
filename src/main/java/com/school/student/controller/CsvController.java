package com.school.student.controller;

import com.school.student.service.CsvUploadService;
import com.school.student.service.ExcelToCsvService;
import com.school.student.service.StudentService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/csv")
public class CsvController {
    private final StudentService studentService;
    private final ExcelToCsvService excelToCsvService;
    private final CsvUploadService csvUploadService;

    public CsvController(StudentService studentService, ExcelToCsvService excelToCsvService, CsvUploadService csvUploadService) {
        this.studentService = studentService;
        this.excelToCsvService = excelToCsvService;
        this.csvUploadService = csvUploadService;
    }

    @PostMapping("/convert")
    public ResponseEntity<String> convertExcelToCsv(
            @RequestParam String excelName,
            @RequestParam String csvName) {
        try {
            excelToCsvService.convertExcelToCsv(excelName, csvName);
            return ResponseEntity.ok("CSV file generated and saved as: " + csvName);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadCsv(@RequestParam String csvName) {
        try {
            csvUploadService.uploadCsvToDb(csvName);
            return ResponseEntity.ok("Data uploaded to DB");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/export")
    public ResponseEntity<Resource> exportCsv(
            @RequestParam(required = false) String className,
            @RequestParam(required = false) Long studentId) throws IOException {

        Path csvFile = studentService.exportCsv(className, studentId);
        Resource resource = new FileSystemResource(csvFile);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=students.csv")
                .body(resource);
    }
}
