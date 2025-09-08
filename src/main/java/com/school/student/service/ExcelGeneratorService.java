package com.school.student.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Random;

@Service
public class ExcelGeneratorService {
    private static final String[] CLASSES = {"Class1", "Class2", "Class3", "Class4", "Class5"};
    private static final Random RANDOM = new Random();

    public String generateExcel(long count) throws IOException {
        String dir = System.getProperty("os.name").toLowerCase().contains("win") ?
                "C:\\var\\log\\applications\\API\\dataprocessing\\" :
                "/var/log/applications/API/dataprocessing/";

        Files.createDirectories(Paths.get(dir));
        String fileName = "students-" + count + "-" + System.currentTimeMillis() + ".xlsx";
        Path filePath = Paths.get(dir, fileName);

        try (SXSSFWorkbook workbook = new SXSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(filePath.toFile())) {

            Sheet sheet = workbook.createSheet("Students");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("studentId");
            header.createCell(1).setCellValue("firstName");
            header.createCell(2).setCellValue("lastName");
            header.createCell(3).setCellValue("dob");
            header.createCell(4).setCellValue("class");
            header.createCell(5).setCellValue("score");

            for (int i = 1; i <= count; i++) {
                Row row = sheet.createRow(i);
                row.createCell(0).setCellValue(i);
                row.createCell(1).setCellValue(randomString(3, 8));
                row.createCell(2).setCellValue(randomString(3, 8));
                row.createCell(3).setCellValue(randomDob().toString());
                row.createCell(4).setCellValue(CLASSES[RANDOM.nextInt(CLASSES.length)]);
                row.createCell(5).setCellValue(55 + RANDOM.nextInt(21)); // 55–75
            }

            workbook.write(fos);
            workbook.dispose();
        }
        return fileName;
    }

    private String randomString(int min, int max) {
        int len = min + RANDOM.nextInt(max - min + 1);
        return RANDOM.ints('a', 'z' + 1)
                .limit(len)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    private LocalDate randomDob() {
        long start = LocalDate.of(2000, 1, 1).toEpochDay();
        long end = LocalDate.of(2010, 12, 31).toEpochDay();
        return LocalDate.ofEpochDay(start + RANDOM.nextLong(end - start + 1));
    }
}
