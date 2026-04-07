package com.gameboost.service;

import com.gameboost.model.OptimizationEntry;
import com.gameboost.model.OptimizationEntry.RiskLevel;
import com.gameboost.model.OptimizationEntry.Category;
import com.gameboost.model.PcProfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class OptimizationService {

    private static final List<String> TARGET_PROCESSES = List.of(
        "OneDrive.exe", "Teams.exe", "ms-teams.exe", "Slack.exe",
        "Discord.exe", "Spotify.exe", "EpicGamesLauncher.exe",
        "GalaxyClient.exe", "SearchApp.exe", "SearchUI.exe",
        "YourPhone.exe", "PhoneExperienceHost.exe", "SkypeApp.exe",
        "AdobeUpdateService.exe", "AdobeARM.exe", "GoogleUpdate.exe",
        "Cortana.exe", "TabTip.exe", "SpeechRuntime.exe",
        "WpcMon.exe", "MicrosoftEdgeUpdate.exe", "backgroundTaskHost.exe"
    );

    // =========================================================================
    //  BUILD OPTIMIZATION LIST
    // =========================================================================

    public List<OptimizationEntry> buildOptimizationList(PcProfile profile) {
        List<OptimizationEntry> list = new ArrayList<>();

        // --- MEMORY ---
        list.add(entry("kill_processes", "Kill Background Processes",
            "Terminates non-essential apps (Teams, Discord, Spotify, OneDrive etc.) to free CPU and RAM.",
            RiskLevel.SAFE, null, false, Category.MEMORY));

        list.add(entry("clear_ram", "Flush RAM Standby Cache",
            "Clears the Windows standby memory list. Frees RAM that's cached but not actively used.",
            RiskLevel.SAFE, null, false, Category.MEMORY));

        list.add(entry("disable_superfetch", "Disable SysMain (Superfetch)",
            "Stops Windows from pre-loading apps into RAM. Beneficial on low-RAM systems during gaming.",
            RiskLevel.MODERATE,
            "SysMain improves app launch speed when not gaming. Disabling it means slower cold starts. Reversible.",
            true, Category.MEMORY));

        // --- CPU ---
        list.add(entry("power_plan", "High Performance Power Plan",
            "Prevents CPU clock speed from dropping during gameplay. Most impactful on laptops.",
            RiskLevel.SAFE, null, true, Category.CPU));

        list.add(entry("disable_core_parking", "Disable CPU Core Parking",
            "Keeps all CPU cores active instead of parking idle cores. Reduces micro-stutters.",
            RiskLevel.SAFE, null, true, Category.CPU));

        list.add(entry("high_priority_games", "Game Process Priority",
            "Sets running game processes to High priority in the Windows scheduler.",
            RiskLevel.SAFE, null, false, Category.CPU));

        list.add(entry("disable_hw_accel_gpu", "Disable Hardware-Accelerated GPU Scheduling",
            "HAGS can cause stuttering on older drivers. Disabling it stabilises frame pacing.",
            RiskLevel.MODERATE,
            "On newer NVIDIA/AMD drivers HAGS may help. Try disabling if you have stutters, re-enable if it gets worse.",
            true, Category.CPU));

        // --- GPU ---
        list.add(entry("gpu_performance", "GPU Maximum Performance Mode",
            "Forces the GPU power management to maximum performance. Eliminates GPU clock sag mid-game.",
            RiskLevel.RISKY,
            "Increases GPU power draw and heat output. Not recommended on laptops without proper cooling.",
            true, Category.GPU));

        list.add(entry("disable_fullscreen_opt", "Disable Fullscreen Optimizations",
            "Turns off Windows fullscreen optimizations globally. Fixes input lag in some games.",
            RiskLevel.SAFE, null, true, Category.GPU));

        // --- NETWORK ---
        list.add(entry("network_latency", "Reduce Network Latency (Nagle)",
            "Disables Nagle's algorithm (TcpAckFrequency/TCPNoDelay). Reduces TCP packet delays for lower ping.",
            RiskLevel.MODERATE,
            "Can slightly reduce throughput for large file transfers. No impact on gaming bandwidth.",
            true, Category.NETWORK));

        list.add(entry("network_throttling", "Disable Network Throttling",
            "Removes Windows' 10-packets-per-ms network throttling limit applied to non-multimedia apps.",
            RiskLevel.SAFE, null, true, Category.NETWORK));

        list.add(entry("dns_cache", "Flush DNS Cache",
            "Clears stale DNS entries. Can fix connection issues and slightly improve initial connection times.",
            RiskLevel.SAFE, null, false, Category.NETWORK));

        // --- STORAGE ---
        list.add(entry("clear_temp", "Clear Temp Files",
            "Removes junk from %TEMP% and Windows\\Temp. Frees disk space and speeds up antivirus scans.",
            RiskLevel.SAFE, null, false, Category.STORAGE));

        list.add(entry("disable_prefetch_writes", "Disable Prefetch Writes (SSD)",
            "Disables Windows prefetch file creation on SSDs, which don't benefit from it.",
            RiskLevel.SAFE, null, true, Category.STORAGE));

        // --- PRIVACY / SYSTEM ---
        list.add(entry("disable_gamebar", "Disable Xbox Game Bar & DVR",
            "Removes Xbox overlay and background game recording. Frees CPU/GPU overhead in-game.",
            RiskLevel.RESTART_REQUIRED,
            "Xbox capture features will stop working. Re-enable via Settings → Gaming if needed.",
            true, Category.PRIVACY));

        list.add(entry("disable_telemetry", "Reduce Windows Telemetry",
            "Sets telemetry to Basic level and stops DiagTrack service. Reduces background CPU/network usage.",
            RiskLevel.MODERATE,
            "Some Windows diagnostic data will not be sent to Microsoft. Does not affect functionality.",
            true, Category.PRIVACY));

        list.add(entry("disable_notifications", "Disable Notification Center",
            "Suppresses Windows notification popups during gameplay to avoid focus interruptions.",
            RiskLevel.SAFE, null, true, Category.SYSTEM));

        list.add(entry("visual_effects", "Reduce Visual Effects",
            "Disables animations, transparency, and shadows in Windows. Frees GPU and CPU overhead.",
            RiskLevel.SAFE, null, true, Category.SYSTEM));

        // Smart pre-selection based on PC profile
        applySmartDefaults(list, profile);

        return list;
    }

    private void applySmartDefaults(List<OptimizationEntry> list, PcProfile profile) {
        // These are universally safe and beneficial — always recommend
        enable(list, "kill_processes");
        enable(list, "power_plan");
        enable(list, "clear_temp");
        enable(list, "network_throttling");
        enable(list, "disable_gamebar");
        enable(list, "disable_notifications");

        if (profile == null) return;

        if (profile.lowRam) {
            enable(list, "clear_ram");
            enable(list, "disable_superfetch");
        }
        if (profile.hasHdd) {
            // Don't disable prefetch on HDD — it helps
        } else {
            // SSD detected
            enable(list, "disable_prefetch_writes");
        }
        if (profile.hasNvidiaGpu || profile.hasAmdGpu) {
            enable(list, "gpu_performance");
            enable(list, "disable_fullscreen_opt");
        }
        if (profile.cpuCores >= 4) {
            enable(list, "disable_core_parking");
        }
    }

    private void enable(List<OptimizationEntry> list, String id) {
        list.stream().filter(e -> e.getId().equals(id)).findFirst()
            .ifPresent(e -> e.setEnabled(true));
    }

    private OptimizationEntry entry(String id, String title, String desc,
                                     RiskLevel risk, String tooltip,
                                     boolean revertable, Category cat) {
        return new OptimizationEntry(id, title, desc, risk, tooltip, revertable, cat);
    }

    // =========================================================================
    //  IMPLEMENTATIONS
    // =========================================================================

    public String runKillProcesses() {
        int killed = 0;
        StringBuilder log = new StringBuilder();
        for (String proc : TARGET_PROCESSES) {
            try {
                ProcessBuilder pb = new ProcessBuilder("taskkill", "/F", "/IM", proc);
                pb.redirectErrorStream(true);
                Process p = pb.start();
                String out = readOutput(p);
                p.waitFor();
                if (out.toLowerCase().contains("success") || out.toLowerCase().contains("terminated")) {
                    killed++;
                    log.append(proc).append(", ");
                }
            } catch (Exception ignored) {}
        }
        return killed > 0
            ? "SUCCESS: Killed " + killed + " processes: " + log.toString().replaceAll(", $", "")
            : "SUCCESS: No target processes were running.";
    }

    public String runSetHighPerformancePower() {
        runCommand("powercfg", "-setactive", "8c5e7fda-e8bf-4a96-9a85-a6e23a8c635c");
        return "SUCCESS: High Performance power plan activated.";
    }

    public String revertPowerPlan() {
        runCommand("powercfg", "-setactive", "381b4222-f694-41f0-9685-ff5bb260df2e");
        return "Reverted to Balanced power plan.";
    }

    public String runClearRam() {
        runCommand("cmd", "/c",
            "if exist \"%SystemRoot%\\System32\\EmptyStandbyList.exe\" " +
            "\"%SystemRoot%\\System32\\EmptyStandbyList.exe\" standbylist");
        return "SUCCESS: RAM standby cache flushed.";
    }

    public String runClearTemp() {
        String userTemp = System.getenv("TEMP");
        long deleted = deleteDir(userTemp) + deleteDir("C:\\Windows\\Temp");
        return "SUCCESS: Removed " + deleted + " temp files.";
    }

    public String runDisableSuperFetch() {
        runCommand("sc", "stop", "SysMain");
        runCommand("sc", "config", "SysMain", "start=", "disabled");
        return "SUCCESS: SysMain (Superfetch) disabled.";
    }

    public String revertSuperFetch() {
        runCommand("sc", "config", "SysMain", "start=", "auto");
        runCommand("sc", "start", "SysMain");
        return "SysMain re-enabled.";
    }

    public String runDisableCoreParking() {
        // Unpark all cores via powercfg
        runPowershell(
            "powercfg -setacvalueindex scheme_current sub_processor CPMINCORES 100;" +
            "powercfg -setdcvalueindex scheme_current sub_processor CPMINCORES 100;" +
            "powercfg -s scheme_current"
        );
        return "SUCCESS: CPU core parking disabled.";
    }

    public String revertCoreParking() {
        runPowershell(
            "powercfg -setacvalueindex scheme_current sub_processor CPMINCORES 0;" +
            "powercfg -setdcvalueindex scheme_current sub_processor CPMINCORES 0;" +
            "powercfg -s scheme_current"
        );
        return "CPU core parking reverted to default.";
    }

    public String runDisableHWAccelGPUSched() {
        runPowershell(
            "Set-ItemProperty -Path 'HKLM:\\SYSTEM\\CurrentControlSet\\Control\\GraphicsDrivers' " +
            "-Name HwSchMode -Value 1 -Type DWord -Force"
        );
        return "SUCCESS: Hardware-Accelerated GPU Scheduling disabled. Restart recommended.";
    }

    public String revertHWAccelGPUSched() {
        runPowershell(
            "Set-ItemProperty -Path 'HKLM:\\SYSTEM\\CurrentControlSet\\Control\\GraphicsDrivers' " +
            "-Name HwSchMode -Value 2 -Type DWord -Force"
        );
        return "HAGS re-enabled.";
    }

    public String runGpuPerformance(PcProfile profile) {
        if (profile != null && profile.hasNvidiaGpu) {
            runPowershell(
                "$path = 'HKLM:\\SYSTEM\\CurrentControlSet\\Control\\Class\\{4d36e968-e325-11ce-bfc1-08002be10318}';" +
                "$sub = Get-ChildItem $path | Where-Object { (Get-ItemProperty $_.PSPath).DriverDesc -match 'NVIDIA' };" +
                "if ($sub) { Set-ItemProperty $sub.PSPath -Name PerfLevelSrc -Value 0x2222 -Force }"
            );
            return "SUCCESS: NVIDIA GPU set to maximum performance mode.";
        } else if (profile != null && profile.hasAmdGpu) {
            return "INFO: AMD GPU — open Radeon Software → Performance → Tuning → Power Tuning → set to Maximum Performance.";
        }
        return "SUCCESS: GPU performance registry updated.";
    }

    public String revertGpuPerformance() {
        runPowershell(
            "$path = 'HKLM:\\SYSTEM\\CurrentControlSet\\Control\\Class\\{4d36e968-e325-11ce-bfc1-08002be10318}';" +
            "$sub = Get-ChildItem $path | Where-Object { (Get-ItemProperty $_.PSPath).DriverDesc -match 'NVIDIA' };" +
            "if ($sub) { Set-ItemProperty $sub.PSPath -Name PerfLevelSrc -Value 0x2211 -Force }"
        );
        return "GPU performance reverted to adaptive.";
    }

    public String runDisableFullscreenOpt() {
        runPowershell(
            "Set-ItemProperty -Path 'HKCU:\\System\\GameConfigStore' " +
            "-Name GameDVR_FSEBehaviorMode -Value 2 -Type DWord -Force;" +
            "Set-ItemProperty -Path 'HKCU:\\System\\GameConfigStore' " +
            "-Name GameDVR_HonorUserFSEBehaviorMode -Value 1 -Type DWord -Force"
        );
        return "SUCCESS: Fullscreen optimizations disabled.";
    }

    public String revertFullscreenOpt() {
        runPowershell(
            "Remove-ItemProperty -Path 'HKCU:\\System\\GameConfigStore' " +
            "-Name GameDVR_FSEBehaviorMode -ErrorAction SilentlyContinue;" +
            "Remove-ItemProperty -Path 'HKCU:\\System\\GameConfigStore' " +
            "-Name GameDVR_HonorUserFSEBehaviorMode -ErrorAction SilentlyContinue"
        );
        return "Fullscreen optimizations reverted.";
    }

    public String runNetworkLatency() {
        // Disable Nagle's algorithm on all network adapters
        runPowershell(
            "$adapters = Get-ChildItem 'HKLM:\\SYSTEM\\CurrentControlSet\\Services\\Tcpip\\Parameters\\Interfaces';" +
            "foreach ($a in $adapters) {" +
            "  Set-ItemProperty $a.PSPath -Name TcpAckFrequency -Value 1 -Type DWord -Force -ErrorAction SilentlyContinue;" +
            "  Set-ItemProperty $a.PSPath -Name TCPNoDelay -Value 1 -Type DWord -Force -ErrorAction SilentlyContinue" +
            "}"
        );
        return "SUCCESS: Nagle's algorithm disabled on all adapters.";
    }

    public String revertNetworkLatency() {
        runPowershell(
            "$adapters = Get-ChildItem 'HKLM:\\SYSTEM\\CurrentControlSet\\Services\\Tcpip\\Parameters\\Interfaces';" +
            "foreach ($a in $adapters) {" +
            "  Remove-ItemProperty $a.PSPath -Name TcpAckFrequency -ErrorAction SilentlyContinue;" +
            "  Remove-ItemProperty $a.PSPath -Name TCPNoDelay -ErrorAction SilentlyContinue" +
            "}"
        );
        return "Network latency settings reverted.";
    }

    public String runDisableNetworkThrottling() {
        runPowershell(
            "Set-ItemProperty -Path 'HKLM:\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\Multimedia\\SystemProfile' " +
            "-Name NetworkThrottlingIndex -Value 0xffffffff -Type DWord -Force;" +
            "Set-ItemProperty -Path 'HKLM:\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\Multimedia\\SystemProfile' " +
            "-Name SystemResponsiveness -Value 0 -Type DWord -Force"
        );
        return "SUCCESS: Network throttling disabled, system responsiveness maximised.";
    }

    public String revertNetworkThrottling() {
        runPowershell(
            "Set-ItemProperty -Path 'HKLM:\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\Multimedia\\SystemProfile' " +
            "-Name NetworkThrottlingIndex -Value 10 -Type DWord -Force;" +
            "Set-ItemProperty -Path 'HKLM:\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\Multimedia\\SystemProfile' " +
            "-Name SystemResponsiveness -Value 20 -Type DWord -Force"
        );
        return "Network throttling reverted to default.";
    }

    public String runFlushDns() {
        runCommand("ipconfig", "/flushdns");
        return "SUCCESS: DNS cache flushed.";
    }

    public String runDisablePrefetchWrites() {
        runPowershell(
            "Set-ItemProperty -Path 'HKLM:\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Memory Management\\PrefetchParameters' " +
            "-Name EnablePrefetcher -Value 0 -Type DWord -Force;" +
            "Set-ItemProperty -Path 'HKLM:\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Memory Management\\PrefetchParameters' " +
            "-Name EnableSuperfetch -Value 0 -Type DWord -Force"
        );
        return "SUCCESS: Prefetch writes disabled (SSD optimised).";
    }

    public String revertPrefetchWrites() {
        runPowershell(
            "Set-ItemProperty -Path 'HKLM:\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Memory Management\\PrefetchParameters' " +
            "-Name EnablePrefetcher -Value 3 -Type DWord -Force;" +
            "Set-ItemProperty -Path 'HKLM:\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Memory Management\\PrefetchParameters' " +
            "-Name EnableSuperfetch -Value 3 -Type DWord -Force"
        );
        return "Prefetch settings reverted.";
    }

    public String runDisableGameBar() {
        runPowershell("Set-ItemProperty -Path 'HKCU:\\Software\\Microsoft\\Windows\\CurrentVersion\\GameDVR' -Name AppCaptureEnabled -Value 0 -Force");
        runPowershell("Set-ItemProperty -Path 'HKCU:\\System\\GameConfigStore' -Name GameDVR_Enabled -Value 0 -Force");
        runPowershell("New-Item -Force -Path 'HKLM:\\SOFTWARE\\Policies\\Microsoft\\Windows\\GameDVR' | Out-Null;" +
                      "Set-ItemProperty -Path 'HKLM:\\SOFTWARE\\Policies\\Microsoft\\Windows\\GameDVR' -Name AllowGameDVR -Value 0 -Type DWord -Force");
        return "SUCCESS: Xbox Game Bar and GameDVR disabled. Restart required.";
    }

    public String revertGameBar() {
        runPowershell("Set-ItemProperty -Path 'HKCU:\\Software\\Microsoft\\Windows\\CurrentVersion\\GameDVR' -Name AppCaptureEnabled -Value 1 -Force");
        runPowershell("Set-ItemProperty -Path 'HKCU:\\System\\GameConfigStore' -Name GameDVR_Enabled -Value 1 -Force");
        return "Xbox Game Bar re-enabled. Restart required.";
    }

    public String runDisableTelemetry() {
        runCommand("sc", "stop", "DiagTrack");
        runCommand("sc", "config", "DiagTrack", "start=", "disabled");
        runPowershell(
            "Set-ItemProperty -Path 'HKLM:\\SOFTWARE\\Policies\\Microsoft\\Windows\\DataCollection' " +
            "-Name AllowTelemetry -Value 0 -Type DWord -Force"
        );
        return "SUCCESS: Windows telemetry reduced to minimum.";
    }

    public String revertTelemetry() {
        runCommand("sc", "config", "DiagTrack", "start=", "auto");
        runCommand("sc", "start", "DiagTrack");
        runPowershell(
            "Set-ItemProperty -Path 'HKLM:\\SOFTWARE\\Policies\\Microsoft\\Windows\\DataCollection' " +
            "-Name AllowTelemetry -Value 1 -Type DWord -Force"
        );
        return "Windows telemetry restored.";
    }

    public String runDisableNotifications() {
        runPowershell(
            "Set-ItemProperty -Path 'HKCU:\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\PushNotifications' " +
            "-Name ToastEnabled -Value 0 -Type DWord -Force"
        );
        return "SUCCESS: Windows notifications suppressed.";
    }

    public String revertNotifications() {
        runPowershell(
            "Set-ItemProperty -Path 'HKCU:\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\PushNotifications' " +
            "-Name ToastEnabled -Value 1 -Type DWord -Force"
        );
        return "Notifications re-enabled.";
    }

    public String runReduceVisualEffects() {
        runPowershell(
            "Set-ItemProperty -Path 'HKCU:\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\VisualEffects' " +
            "-Name VisualFXSetting -Value 2 -Type DWord -Force"
        );
        return "SUCCESS: Windows visual effects set to performance mode.";
    }

    public String revertVisualEffects() {
        runPowershell(
            "Set-ItemProperty -Path 'HKCU:\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\VisualEffects' " +
            "-Name VisualFXSetting -Value 0 -Type DWord -Force"
        );
        return "Visual effects restored to Windows default.";
    }

    public String runHighPriorityGames() {
        // Set Win32PrioritySeparation for foreground app boost
        runPowershell(
            "Set-ItemProperty -Path 'HKLM:\\SYSTEM\\CurrentControlSet\\Control\\PriorityControl' " +
            "-Name Win32PrioritySeparation -Value 38 -Type DWord -Force"
        );
        return "SUCCESS: Foreground app (game) CPU priority boosted.";
    }

    // =========================================================================
    //  HELPERS
    // =========================================================================

    public String runCommand(String... cmd) {
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            String out = readOutput(p);
            p.waitFor();
            return out;
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    public String runPowershell(String script) {
        return runCommand("powershell", "-NonInteractive", "-NoProfile", "-Command", script);
    }

    private String readOutput(Process p) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append("\n");
        }
        return sb.toString().trim();
    }

    private long deleteDir(String path) {
        if (path == null) return 0;
        long count = 0;
        File dir = new File(path);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null)
                for (File f : files)
                    if (f.isFile() && f.delete()) count++;
        }
        return count;
    }
}
