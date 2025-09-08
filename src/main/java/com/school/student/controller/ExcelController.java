package com.school.student.controller;

import com.school.student.service.ExcelGeneratorService;
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
@RequestMapping("/api/excel")
public class ExcelController {
    private final ExcelGeneratorService excelGeneratorService;
    private final StudentService studentService;

    public ExcelController(ExcelGeneratorService excelGeneratorService, StudentService studentService) {
        this.excelGeneratorService = excelGeneratorService;
        this.studentService = studentService;
    }

    @PostMapping("/generate")
    public ResponseEntity<String> generateExcel(@RequestParam long count) {
        try {
            // Generate and save Excel on backend
            String fileName = excelGeneratorService.generateExcel(count);

            // Return path or filename to frontend
            return ResponseEntity.ok("Excel file generated and saved as: " + fileName);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/export")
    public ResponseEntity<Resource> exportExcel(
            @RequestParam(required = false) String className,
            @RequestParam(required = false) Long studentId) throws IOException {

        Path excelFile = studentService.exportExcel(className, studentId);
        Resource resource = new FileSystemResource(excelFile);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=students.xlsx")
                .body(resource);
    }
}
