package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;

public class Main {

    public static void main(String[] args) {
        try {
            String pid = args[0];
            ; // Replace with the actual process ID
            ProcessBuilder processBuilder = new ProcessBuilder("jstack", pid);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println("jstack exited with code: " + exitCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}