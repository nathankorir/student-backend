package com.school.student.controller;

import com.school.student.model.Student;
import com.school.student.service.StudentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
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
    public ResponseEntity<byte[]> exportStudentPdf(@PathVariable Long studentId) {
        byte[] pdfBytes = studentService.generateStudentPdf(studentId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=student-" + studentId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
