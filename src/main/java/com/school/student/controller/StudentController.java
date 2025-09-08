package com.school.student.controller;

import com.school.student.model.Student;
import com.school.student.service.CsvUploadService;
import com.school.student.service.ExcelGeneratorService;
import com.school.student.service.ExcelToCsvService;
import com.school.student.service.StudentService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.io.IOException;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/students")
public class StudentController {
    private final ExcelGeneratorService excelGeneratorService;

    private final ExcelToCsvService excelToCsvService;

    private final CsvUploadService csvUploadService;

    private final StudentService studentService;

    public StudentController(ExcelGeneratorService excelGeneratorService, ExcelToCsvService excelToCsvService, CsvUploadService csvUploadService, StudentService studentService) {
        this.excelGeneratorService = excelGeneratorService;
        this.excelToCsvService = excelToCsvService;
        this.csvUploadService = csvUploadService;
        this.studentService = studentService;
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<Student> getStudentById(@PathVariable Long studentId) {
        return studentService.getStudentById(studentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/class/{className}")
    public ResponseEntity<Page<Student>> getStudentsByClass(@PathVariable String className, Pageable pageable) {
        Page<Student> students = studentService.getStudentsByClass(className, pageable);
        return ResponseEntity.ok(students);
    }

    @GetMapping
    public ResponseEntity<Page<Student>> getAllStudentsByClass(Pageable pageable) {
        Page<Student> students = studentService.getAllStudentsByClass(pageable);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/{studentId}/export/pdf")
    public ResponseEntity<byte[]> exportStudentPdf(@PathVariable Long studentId) throws Exception {
        byte[] pdfBytes = studentService.generateStudentPdf(studentId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=student-" + studentId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
