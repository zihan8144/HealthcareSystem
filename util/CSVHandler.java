package com.hms.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CSVHandler {
    public static List<String[]> readCSV(String filePath) {
        List<String[]> records = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) return records;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; } // skip header row
                if (line.trim().isEmpty()) continue;
                
                // use -1 limit to keep trailing empty strings (important for data alignment)
                records.add(line.split(",", -1)); 
            }
        } catch (IOException e) { e.printStackTrace(); }
        return records;
    }

    public static void appendToCSV(String filePath, String content) {
        // 'true' enables append mode
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            bw.write(content);
            bw.newLine();
        } catch (IOException e) { e.printStackTrace(); }
    }
    
    public static void deleteRecord(String filePath, int idIndex, String id) {
        List<String> validLines = new ArrayList<>();
        File file = new File(filePath);
        
        // 1. Read and filter
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { validLines.add(line); first = false; continue; } // keep header
                
                String[] parts = line.split(",", -1);
                // only keep lines that DO NOT match the ID
                if (parts.length > idIndex && !parts[idIndex].equals(id)) {
                    validLines.add(line);
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        
        // 2. Rewrite file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
            for (String l : validLines) {
                bw.write(l);
                bw.newLine();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}