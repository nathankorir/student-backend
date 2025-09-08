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
@RequestMapping("/api/data")
public class StudentController {
    private final ExcelGeneratorService excelGeneratorService;

    private final ExcelToCsvService excelToCsvService;

    private final CsvUploadService csvUploadService;

    private final StudentService studentService;

    private final String dataDir = System.getProperty("os.name").toLowerCase().contains("win") ?
            "C:\\var\\log\\applications\\API\\dataprocessing\\" :
            "/var/log/applications/API/dataprocessing/";

    public StudentController(ExcelGeneratorService excelGeneratorService, ExcelToCsvService excelToCsvService, CsvUploadService csvUploadService, StudentService studentService) {
        this.excelGeneratorService = excelGeneratorService;
        this.excelToCsvService = excelToCsvService;
        this.csvUploadService = csvUploadService;
        this.studentService = studentService;
    }

//    @PostMapping("/generate")
//    public ResponseEntity<String> generateExcel(@RequestParam long count) {
//        try {
//            excelGeneratorService.generateExcel(count);
//            return ResponseEntity.ok("Excel generated");
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
//        }
//    }

    @PostMapping("/generate")
    public ResponseEntity<String> generateExcel(@RequestParam long count) {
        try {
            // Generate and save Excel on backend
            String filePath = excelGeneratorService.generateExcel(count); // return full path

            // Return path or filename to frontend
            return ResponseEntity.ok("Excel file generated and saved to: " + filePath);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }


//    @PostMapping("/convert")
//    public ResponseEntity<String> convertExcelToCsv(@RequestParam String excelPath, @RequestParam String csvPath) {
//        try {
//            excelToCsvService.convertExcelToCsv(excelPath, csvPath);
//            return ResponseEntity.ok("CSV generated");
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
//        }
//    }

    @PostMapping("/convert")
    public ResponseEntity<String> convertExcelToCsv(
            @RequestParam String excelName,
            @RequestParam String csvName) {
        try {
            excelToCsvService.convertExcelToCsv(excelName, csvName); // reads Excel from server, writes CSV to server
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

    @GetMapping("/all")
    public ResponseEntity<Page<Student>> getAllStudentsByClass(Pageable pageable) {
        Page<Student> students = studentService.getAllStudentsByClass(pageable);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/files")
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

    @GetMapping("/students/export/csv")
    public ResponseEntity<Resource> exportCsv(
            @RequestParam(required = false) String className,
            @RequestParam(required = false) Long studentId) throws IOException {

        Path csvFile = studentService.exportCsv(className, studentId);
        Resource resource = new FileSystemResource(csvFile);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=students.csv")
                .body(resource);
    }

    @GetMapping("/students/export/excel")
    public ResponseEntity<Resource> exportExcel(
            @RequestParam(required = false) String className,
            @RequestParam(required = false) Long studentId) throws IOException {

        Path excelFile = studentService.exportExcel(className, studentId);
        Resource resource = new FileSystemResource(excelFile);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=students.xlsx")
                .body(resource);
    }

    @GetMapping("/students/{studentId}/export/pdf")
    public ResponseEntity<byte[]> exportStudentPdf(@PathVariable Long studentId) throws Exception {
        byte[] pdfBytes = studentService.generateStudentPdf(studentId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=student-" + studentId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

}
