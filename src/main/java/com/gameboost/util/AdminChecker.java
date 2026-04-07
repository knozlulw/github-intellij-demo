package com.gameboost.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class AdminChecker {
    public static boolean isAdmin() {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "powershell", "-NonInteractive", "-Command",
                "([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent())" +
                ".IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)"
            );
            pb.redirectErrorStream(true);
            Process p = pb.start();
            String out = new BufferedReader(new InputStreamReader(p.getInputStream()))
                .readLine();
            p.waitFor();
            return "True".equalsIgnoreCase(out != null ? out.trim() : "");
        } catch (Exception e) {
            return false;
        }
    }
}
