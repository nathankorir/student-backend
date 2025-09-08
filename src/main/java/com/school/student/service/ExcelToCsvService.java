package com.school.student.service;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.springframework.stereotype.Service;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class ExcelToCsvService {
    public void convertExcelToCsv(String excelName, String csvName) throws Exception {
        String dir = System.getProperty("os.name").toLowerCase().contains("win") ?
                "C:\\var\\log\\applications\\API\\dataprocessing\\" :
                "/var/log/applications/API/dataprocessing/";
        try (OPCPackage pkg = OPCPackage.open(new File(dir + excelName));
             BufferedWriter writer = Files.newBufferedWriter(Paths.get(dir + csvName))) {

            XSSFReader reader = new XSSFReader(pkg);
            SharedStringsTable sst = (SharedStringsTable) reader.getSharedStringsTable();

            XMLReader parser = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            parser.setContentHandler(new SheetHandler(sst, writer));

            try (InputStream sheet = reader.getSheetsData().next()) {
                parser.parse(new InputSource(sheet));
            }
        }
    }

    private static class SheetHandler extends DefaultHandler {
        private final SharedStringsTable sst;
        private final BufferedWriter writer;
        private final DataFormatter formatter = new DataFormatter();

        private final StringBuilder lastContents = new StringBuilder();
        private String currentCellRef;

        private String[] currentRowValues;
        private int rowIndex = -1;
        private int maxColumns = 0;

        SheetHandler(SharedStringsTable sst, BufferedWriter writer) {
            this.sst = sst;
            this.writer = writer;
        }

        private String cellType;

        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes) {
            if ("c".equals(name)) {
                currentCellRef = attributes.getValue("r");
                cellType = attributes.getValue("t");
                attributes.getValue("s");
                lastContents.setLength(0);
            } else if ("row".equals(name)) {
                rowIndex++;
                currentRowValues = new String[100];
            }
        }


        @Override
        public void characters(char[] ch, int start, int length) {
            lastContents.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String name) {
            if ("v".equals(name) || "t".equals(name)) {
                String value = lastContents.toString();

                if ("s".equals(cellType)) {
                    int idx = Integer.parseInt(value);
                    value = sst.getItemAt(idx).getString();
                } else {
                    value = value.trim();
                }

                int actualColIndex = getColumnIndex(currentCellRef);
                if (actualColIndex >= currentRowValues.length) {
                    String[] bigger = new String[actualColIndex + 10];
                    System.arraycopy(currentRowValues, 0, bigger, 0, currentRowValues.length);
                    currentRowValues = bigger;
                }
                currentRowValues[actualColIndex] = value;
                maxColumns = Math.max(maxColumns, actualColIndex + 1);
            }

            else if ("row".equals(name)) {
                writeRow();
            }
            lastContents.setLength(0);
        }

        private void writeRow() {
            try {
                StringBuilder rowBuilder = new StringBuilder();
                for (int i = 0; i < maxColumns; i++) {
                    if (i > 0) rowBuilder.append(",");
                    String val = (i < currentRowValues.length && currentRowValues[i] != null)
                            ? currentRowValues[i] : "";

                    // add score (+10) in column 5
                    if (i == 5 && rowIndex > 0 && !val.isEmpty()) {
                        int score = (int) Math.round(Double.parseDouble(val));
                        val = String.valueOf(score + 10);
                    }
                    rowBuilder.append(escapeCsv(val));
                }
                writer.write(rowBuilder.toString());
                writer.newLine();
            } catch (Exception e) {
                throw new RuntimeException("Error writing row " + rowIndex, e);
            }
        }

        private int getColumnIndex(String cellRef) {
            if (cellRef == null) return 0;
            StringBuilder colRef = new StringBuilder();
            for (char ch : cellRef.toCharArray()) {
                if (Character.isLetter(ch)) colRef.append(ch);
                else break;
            }
            int index = 0;
            for (int i = 0; i < colRef.length(); i++) {
                index = index * 26 + (colRef.charAt(i) - 'A' + 1);
            }
            return index - 1;
        }

        private String escapeCsv(String value) {
            if (value.contains(",") || value.contains("\"")) {
                return "\"" + value.replace("\"", "\"\"") + "\"";
            }
            return value;
        }
    }
}
