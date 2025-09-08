package com.school.student.service;

import com.school.student.model.Student;
import com.school.student.repository.StudentRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.sql.ResultSet;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;

@Service
public class StudentService {
    private final StudentRepository studentRepository;
    private final JdbcTemplate jdbcTemplate;

    public StudentService(StudentRepository studentRepository, JdbcTemplate jdbcTemplate) {
        this.studentRepository = studentRepository;
        this.jdbcTemplate = jdbcTemplate;
    }
    public Optional<Student> getStudentById(Long studentId) {
        return studentRepository.findById(studentId);
    }

    public Page<Student> getStudentsByClass(String className, Pageable pageable) {
        return studentRepository.findByClassName(className, pageable);
    }

    public Page<Student> getAllStudentsByClass(Pageable pageable) {
        return studentRepository.findAll(pageable);
    }

    public Path exportCsv(String className, Long studentId) throws IOException {

        Path tempFile = Files.createTempFile("students-", ".csv");

        try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
            // CSV header
            writer.write("studentId,firstName,lastName,dob,className,score\n");

            // Build SQL
            String sql = "SELECT student_id, first_name, last_name, dob, class_name, score FROM student WHERE 1=1";
            if (className != null) sql += " AND class_name = ?";
            if (studentId != null) sql += " AND student_id = ?";

            Object[] params;
            if (className != null && studentId != null) {
                params = new Object[]{className, studentId};
            } else if (className != null) {
                params = new Object[]{className};
            } else if (studentId != null) {
                params = new Object[]{studentId};
            } else {
                params = new Object[]{};
            }

            jdbcTemplate.query(sql, params, (ResultSetExtractor<Void>) rs -> {
                while (rs.next()) {
                    try {
                        writer.write(String.format("%d,%s,%s,%s,%s,%d\n",
                                rs.getLong("student_id"),
                                rs.getString("first_name"),
                                rs.getString("last_name"),
                                rs.getDate("dob"),
                                rs.getString("class_name"),
                                rs.getInt("score")));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                return null;
            });
        }

        return tempFile;
    }

    public Path exportExcel(String className, Long studentId) throws IOException {
        Path tempFile = Files.createTempFile("students-", ".xlsx");

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(); FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {
            Sheet sheet = workbook.createSheet("Students");

            // Header
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("studentId");
            header.createCell(1).setCellValue("firstName");
            header.createCell(2).setCellValue("lastName");
            header.createCell(3).setCellValue("dob");
            header.createCell(4).setCellValue("className");
            header.createCell(5).setCellValue("score");

            // SQL
            String sql = "SELECT student_id, first_name, last_name, dob, class_name, score FROM student WHERE 1=1";
            if (className != null) sql += " AND class_name = ?";
            if (studentId != null) sql += " AND student_id = ?";

            Object[] params;
            if (className != null && studentId != null) {
                params = new Object[]{className, studentId};
            } else if (className != null) {
                params = new Object[]{className};
            } else if (studentId != null) {
                params = new Object[]{studentId};
            } else {
                params = new Object[]{};
            }

            // Use ResultSetExtractor to write Excel
            jdbcTemplate.query(sql, params, (ResultSet rs) -> {
                int rowNum = 1;
                while (rs.next()) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(rs.getLong("student_id"));
                    row.createCell(1).setCellValue(rs.getString("first_name"));
                    row.createCell(2).setCellValue(rs.getString("last_name"));
                    row.createCell(3).setCellValue(rs.getDate("dob").toString());
                    row.createCell(4).setCellValue(rs.getString("class_name"));
                    row.createCell(5).setCellValue(rs.getInt("score"));
                }
                return null;
            });

            workbook.write(fos);
            workbook.dispose();
        }

        return tempFile;
    }

    public byte[] generateStudentPdf(Long studentId) {
        Student student = studentRepository.findById(studentId).orElseThrow(() -> new NoSuchElementException("Student not found"));
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

        document.open();

        // Title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Student Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Table with 2 columns
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(80);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        addRow(table, "Student ID", String.valueOf(student.getStudentId()));
        addRow(table, "First Name", student.getFirstName());
        addRow(table, "Last Name", student.getLastName());
        addRow(table, "Date of Birth", student.getDob().toString());
        addRow(table, "Class", student.getClassName());
        addRow(table, "Score", String.valueOf(student.getScore()));

        document.add(table);
        document.close();

        return out.toByteArray();
    }

    private void addRow(PdfPTable table, String field, String value) {
        PdfPCell cell1 = new PdfPCell(new Phrase(field));
        PdfPCell cell2 = new PdfPCell(new Phrase(value));
        cell1.setPadding(8);
        cell2.setPadding(8);
        table.addCell(cell1);
        table.addCell(cell2);
    }
}
