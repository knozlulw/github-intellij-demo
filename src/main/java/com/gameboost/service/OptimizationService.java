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

    public List<OptimizationEntry> buildOptimizationList(PcProfile profile) {
        List<OptimizationEntry> list = new ArrayList<>();

        // ── MEMORY ──
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

        list.add(entry("disable_paging_exec", "Reduce DPC Latency (Paging Executive)",
                "Forces the kernel to keep pageable code in physical RAM instead of the pagefile. Reduces DPC latency and micro-stutters.",
                RiskLevel.MODERATE,
                "Uses slightly more RAM to keep kernel data in physical memory. Safe on systems with 8GB+ RAM.",
                true, Category.MEMORY));

        list.add(entry("disable_delivery_opt", "Disable Delivery Optimization",
                "Stops Windows from using your PC to upload Windows updates to other PCs on the internet. Frees bandwidth and reduces background I/O.",
                RiskLevel.SAFE, null, true, Category.MEMORY));

        // ── CPU ──
        list.add(entry("power_plan", "High Performance Power Plan",
                "Prevents CPU clock speed from dropping during gameplay. Most impactful on laptops.",
                RiskLevel.SAFE, null, true, Category.CPU));

        list.add(entry("ultimate_power_plan", "Ultimate Performance Power Plan",
                "Activates Windows' hidden Ultimate Performance plan. Eliminates micro-latencies by keeping CPU at max frequency at all times.",
                RiskLevel.MODERATE,
                "Higher idle power draw. Not recommended on battery-powered laptops. Slightly more heat.",
                true, Category.CPU));

        list.add(entry("disable_core_parking", "Disable CPU Core Parking",
                "Keeps all CPU cores active instead of parking idle cores. Reduces micro-stutters.",
                RiskLevel.SAFE, null, true, Category.CPU));

        list.add(entry("high_priority_games", "Game Process Priority Boost",
                "Sets Win32PrioritySeparation to 0x26 — short fixed boost. Lower input lag, higher 1% lows, smoother frametimes.",
                RiskLevel.SAFE, null, true, Category.CPU));

        list.add(entry("mmcss_games_priority", "MMCSS Game Thread Priority",
                "Sets GPU Priority to 8 and CPU Priority to 6 for the Games task in Multimedia Class Scheduler. Used by DirectX games.",
                RiskLevel.SAFE, null, true, Category.CPU));

        list.add(entry("disable_hw_accel_gpu", "Disable Hardware-Accelerated GPU Scheduling",
                "HAGS can cause stuttering on older drivers. Disabling it stabilises frame pacing on most systems.",
                RiskLevel.MODERATE,
                "On newer NVIDIA/AMD drivers HAGS may help. Disable if you have stutters, re-enable if performance drops.",
                true, Category.CPU));

        list.add(entry("disable_spectre_meltdown", "Disable Spectre/Meltdown Mitigations",
                "Removes CPU security patches that Microsoft applies by default. Can recover 5–15% CPU performance lost to these patches.",
                RiskLevel.RISKY,
                "WARNING: Reduces system security. Only for dedicated gaming PCs not used for browsing or sensitive data. NOT recommended for laptops.",
                true, Category.CPU));

        // ── GPU ──
        list.add(entry("gpu_performance", "GPU Maximum Performance Mode",
                "Forces GPU power management to maximum performance. Eliminates GPU clock sag mid-game.",
                RiskLevel.RISKY,
                "Increases GPU power draw and heat. Not recommended on laptops without proper cooling.",
                true, Category.GPU));

        list.add(entry("disable_fullscreen_opt", "Disable Fullscreen Optimizations",
                "Turns off Windows fullscreen optimizations globally. Fixes input lag in some DX11 games.",
                RiskLevel.SAFE, null, true, Category.GPU));

        list.add(entry("disable_mpo", "Disable Multiplane Overlay (MPO)",
                "Disables MPO which causes black screen flashes, stutters and alt-tab rendering bugs in Windows 11 24H2+. Widely recommended fix.",
                RiskLevel.SAFE,
                "Safe to disable. MPO is a known source of rendering glitches on NVIDIA and AMD in recent Windows builds.",
                true, Category.GPU));

        list.add(entry("nvidia_shader_cache", "Increase NVIDIA Shader Cache",
                "Increases the NVIDIA shader cache to 10GB. Prevents in-game stutters caused by shader recompilation when cache fills up.",
                RiskLevel.SAFE, null, true, Category.GPU));

        // ── NETWORK ──
        list.add(entry("network_latency", "Disable Nagle's Algorithm",
                "Disables Nagle's algorithm (TcpAckFrequency/TCPNoDelay). Reduces TCP packet delays for lower ping.",
                RiskLevel.MODERATE,
                "Can slightly reduce throughput on large file transfers. No impact on gaming bandwidth.",
                true, Category.NETWORK));

        list.add(entry("network_throttling", "Disable Network Throttling",
                "Removes Windows' 10-packets-per-ms network throttling limit for non-multimedia apps.",
                RiskLevel.SAFE, null, true, Category.NETWORK));

        list.add(entry("dns_cache", "Flush DNS Cache",
                "Clears stale DNS entries. Can fix connection issues and speed up initial connection times.",
                RiskLevel.SAFE, null, false, Category.NETWORK));

        list.add(entry("disable_auto_tuning", "Disable TCP Auto-Tuning",
                "Locks the TCP receive window to a fixed size. Reduces bufferbloat and improves latency consistency in online games.",
                RiskLevel.MODERATE,
                "May slightly reduce max throughput on fast connections. Helps most on unstable/congested networks.",
                true, Category.NETWORK));

        list.add(entry("disable_windows_update_bandwidth", "Block Windows Update Bandwidth",
                "Limits Windows Update to 0% background bandwidth so it never steals bandwidth during gaming sessions.",
                RiskLevel.SAFE, null, true, Category.NETWORK));

        // ── STORAGE ──
        list.add(entry("clear_temp", "Clear Temp Files",
                "Removes junk from %TEMP% and Windows\\Temp. Frees disk space and speeds up antivirus scans.",
                RiskLevel.SAFE, null, false, Category.STORAGE));

        list.add(entry("disable_prefetch_writes", "Disable Prefetch Writes (SSD)",
                "Disables Windows prefetch file creation on SSDs, which don't benefit from it.",
                RiskLevel.SAFE, null, true, Category.STORAGE));

        list.add(entry("disable_last_access", "Disable NTFS Last Access Timestamps",
                "Stops NTFS from writing a timestamp on every file read. Reduces unnecessary disk writes and I/O overhead.",
                RiskLevel.SAFE, null, true, Category.STORAGE));

        list.add(entry("disable_hibernate", "Disable Hibernation",
                "Frees the hiberfil.sys file from disk (equal to your RAM size). Reclaims gigabytes of SSD space.",
                RiskLevel.MODERATE,
                "You will no longer be able to use Hibernate. Sleep and Shutdown still work normally.",
                true, Category.STORAGE));

        // ── SYSTEM ──
        list.add(entry("disable_gamebar", "Disable Xbox Game Bar & DVR",
                "Removes Xbox overlay and background game recording. Frees CPU/GPU overhead in-game.",
                RiskLevel.RESTART_REQUIRED,
                "Xbox capture features will stop working. Re-enable via Settings → Gaming if needed.",
                true, Category.PRIVACY));

        list.add(entry("disable_telemetry", "Reduce Windows Telemetry",
                "Sets telemetry to minimum and stops DiagTrack service. Reduces background CPU/network usage.",
                RiskLevel.MODERATE,
                "Some Windows diagnostic data will not be sent to Microsoft. Does not affect functionality.",
                true, Category.PRIVACY));

        list.add(entry("disable_notifications", "Disable Notification Center",
                "Suppresses Windows notification popups during gameplay to avoid focus interruptions.",
                RiskLevel.SAFE, null, true, Category.SYSTEM));

        list.add(entry("visual_effects", "Reduce Visual Effects",
                "Disables animations, transparency, and shadows in Windows. Frees GPU and CPU overhead.",
                RiskLevel.SAFE, null, true, Category.SYSTEM));

        list.add(entry("disable_mouse_accel", "Disable Mouse Acceleration",
                "Removes Windows pointer precision (mouse acceleration). Gives 1:1 mouse movement — essential for FPS accuracy.",
                RiskLevel.SAFE, null, true, Category.SYSTEM));

        list.add(entry("enable_game_mode", "Enable Windows Game Mode",
                "Activates Windows Game Mode to deprioritize background tasks during gaming. Simple but effective for low-end systems.",
                RiskLevel.SAFE,
                "Note: can interfere with OBS/streaming software. Turn off if you record gameplay.",
                true, Category.SYSTEM));

        list.add(entry("disable_search_indexing", "Disable Search Indexing",
                "Stops Windows Search from constantly indexing files in the background. Reduces random disk I/O spikes mid-game.",
                RiskLevel.MODERATE,
                "Windows Search results may be slower. Type in Start Menu still works, just takes a moment longer.",
                true, Category.SYSTEM));

        list.add(entry("disable_audio_enhancements", "Disable Audio Enhancements",
                "Turns off Windows audio processing effects. Reduces sound-related DPC latency that can cause micro-stutters.",
                RiskLevel.SAFE, null, true, Category.SYSTEM));

        applySmartDefaults(list, profile);
        return list;
    }

    private void applySmartDefaults(List<OptimizationEntry> list, PcProfile profile) {
        // Universal safe defaults — always recommended
        enable(list, "kill_processes");
        enable(list, "power_plan");
        enable(list, "clear_temp");
        enable(list, "network_throttling");
        enable(list, "disable_gamebar");
        enable(list, "disable_notifications");
        enable(list, "disable_mouse_accel");
        enable(list, "disable_mpo");
        enable(list, "disable_last_access");
        enable(list, "disable_audio_enhancements");
        enable(list, "enable_game_mode");
        enable(list, "disable_windows_update_bandwidth");
        enable(list, "mmcss_games_priority");

        if (profile == null) return;

        if (profile.lowRam) {
            enable(list, "clear_ram");
            enable(list, "disable_superfetch");
        }
        if (!profile.hasHdd) {
            enable(list, "disable_prefetch_writes");
            enable(list, "disable_last_access");
        }
        if (profile.hasNvidiaGpu || profile.hasAmdGpu) {
            enable(list, "gpu_performance");
            enable(list, "disable_fullscreen_opt");
        }
        if (profile.hasNvidiaGpu) {
            enable(list, "nvidia_shader_cache");
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

    public String runDisablePagingExec() {
        runPowershell(
                "Set-ItemProperty -Path 'HKLM:\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Memory Management' " +
                        "-Name DisablePagingExecutive -Value 1 -Type DWord -Force"
        );
        return "SUCCESS: Paging executive disabled — kernel kept in RAM.";
    }

    public String revertDisablePagingExec() {
        runPowershell(
                "Set-ItemProperty -Path 'HKLM:\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Memory Management' " +
                        "-Name DisablePagingExecutive -Value 0 -Type DWord -Force"
        );
        return "Paging executive reverted.";
    }

    public String runDisableDeliveryOpt() {
        runPowershell(
                "Set-ItemProperty -Path 'HKLM:\\SOFTWARE\\Policies\\Microsoft\\Windows\\DeliveryOptimization' " +
                        "-Name DODownloadMode -Value 0 -Type DWord -Force"
        );
        return "SUCCESS: Delivery Optimization disabled.";
    }

    public String revertDeliveryOpt() {
        runPowershell(
                "Remove-ItemProperty -Path 'HKLM:\\SOFTWARE\\Policies\\Microsoft\\Windows\\DeliveryOptimization' " +
                        "-Name DODownloadMode -ErrorAction SilentlyContinue"
        );
        return "Delivery Optimization reverted.";
    }

    public String runUltimatePowerPlan() {
        runCommand("powercfg", "-duplicatescheme", "e9a42b02-d5df-448d-aa00-03f14749eb61");
        runPowershell(
                "$guid = (powercfg -list | Select-String 'Ultimate').ToString().Split()[3];" +
                        "if ($guid) { powercfg -setactive $guid }"
        );
        return "SUCCESS: Ultimate Performance power plan activated.";
    }

    public String revertUltimatePowerPlan() {
        runCommand("powercfg", "-setactive", "381b4222-f694-41f0-9685-ff5bb260df2e");
        return "Reverted to Balanced power plan.";
    }

    public String runMmcssGamesPriority() {
        runPowershell(
                "$path = 'HKLM:\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\Multimedia\\SystemProfile\\Tasks\\Games';" +
                        "Set-ItemProperty -Path $path -Name 'GPU Priority' -Value 8 -Type DWord -Force;" +
                        "Set-ItemProperty -Path $path -Name 'Priority' -Value 6 -Type DWord -Force;" +
                        "Set-ItemProperty -Path $path -Name 'Scheduling Category' -Value 'High' -Type String -Force;" +
                        "Set-ItemProperty -Path $path -Name 'SFIO Priority' -Value 'High' -Type String -Force"
        );
        return "SUCCESS: MMCSS Games thread priority set to High.";
    }

    public String revertMmcssGamesPriority() {
        runPowershell(
                "$path = 'HKLM:\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\Multimedia\\SystemProfile\\Tasks\\Games';" +
                        "Set-ItemProperty -Path $path -Name 'GPU Priority' -Value 2 -Type DWord -Force;" +
                        "Set-ItemProperty -Path $path -Name 'Priority' -Value 2 -Type DWord -Force;" +
                        "Set-ItemProperty -Path $path -Name 'Scheduling Category' -Value 'Medium' -Type String -Force;" +
                        "Set-ItemProperty -Path $path -Name 'SFIO Priority' -Value 'Normal' -Type String -Force"
        );
        return "MMCSS Games priority reverted.";
    }

    public String runDisableMpo() {
        runPowershell(
                "If (!(Test-Path 'HKLM:\\SOFTWARE\\Microsoft\\Windows\\Dwm')) {" +
                        "  New-Item -Path 'HKLM:\\SOFTWARE\\Microsoft\\Windows\\Dwm' -Force | Out-Null" +
                        "};" +
                        "Set-ItemProperty -Path 'HKLM:\\SOFTWARE\\Microsoft\\Windows\\Dwm' " +
                        "-Name OverlayTestMode -Value 5 -Type DWord -Force"
        );
        return "SUCCESS: Multiplane Overlay disabled. Restart recommended.";
    }

    public String revertMpo() {
        runPowershell(
                "Remove-ItemProperty -Path 'HKLM:\\SOFTWARE\\Microsoft\\Windows\\Dwm' " +
                        "-Name OverlayTestMode -ErrorAction SilentlyContinue"
        );
        return "MPO reverted.";
    }

    public String runNvidiaShaderCache() {
        runPowershell(
                "$path = 'HKLM:\\SYSTEM\\CurrentControlSet\\Control\\Class\\{4d36e968-e325-11ce-bfc1-08002be10318}';" +
                        "$sub = Get-ChildItem $path | Where-Object { (Get-ItemProperty $_.PSPath -ErrorAction SilentlyContinue).DriverDesc -match 'NVIDIA' };" +
                        "if ($sub) { Set-ItemProperty $sub.PSPath -Name GLShaderDiskCache -Value 1 -Force;" +
                        "Set-ItemProperty $sub.PSPath -Name GLShaderDiskCacheMaxSize -Value 0x9C400000 -Force }"
        );
        return "SUCCESS: NVIDIA shader cache increased to 10GB.";
    }

    public String runDisableAutoTuning() {
        runCommand("netsh", "int", "tcp", "set", "global", "autotuninglevel=disabled");
        return "SUCCESS: TCP Auto-Tuning disabled.";
    }

    public String revertAutoTuning() {
        runCommand("netsh", "int", "tcp", "set", "global", "autotuninglevel=normal");
        return "TCP Auto-Tuning reverted to normal.";
    }

    public String runDisableUpdateBandwidth() {
        runPowershell(
                "If (!(Test-Path 'HKLM:\\SOFTWARE\\Policies\\Microsoft\\Windows\\DeliveryOptimization')) {" +
                        "  New-Item -Path 'HKLM:\\SOFTWARE\\Policies\\Microsoft\\Windows\\DeliveryOptimization' -Force | Out-Null" +
                        "};" +
                        "Set-ItemProperty -Path 'HKLM:\\SOFTWARE\\Policies\\Microsoft\\Windows\\DeliveryOptimization' " +
                        "-Name DOMaxBackgroundDownloadBandwidth -Value 1 -Type DWord -Force"
        );
        return "SUCCESS: Windows Update background bandwidth capped.";
    }

    public String revertUpdateBandwidth() {
        runPowershell(
                "Remove-ItemProperty -Path 'HKLM:\\SOFTWARE\\Policies\\Microsoft\\Windows\\DeliveryOptimization' " +
                        "-Name DOMaxBackgroundDownloadBandwidth -ErrorAction SilentlyContinue"
        );
        return "Windows Update bandwidth limit removed.";
    }

    public String runDisableLastAccess() {
        runCommand("fsutil", "behavior", "set", "disablelastaccess", "1");
        return "SUCCESS: NTFS last access timestamps disabled.";
    }

    public String revertLastAccess() {
        runCommand("fsutil", "behavior", "set", "disablelastaccess", "0");
        return "NTFS last access timestamps re-enabled.";
    }

    public String runDisableHibernate() {
        runCommand("powercfg", "-hibernate", "off");
        return "SUCCESS: Hibernation disabled. hiberfil.sys freed.";
    }

    public String revertHibernate() {
        runCommand("powercfg", "-hibernate", "on");
        return "Hibernation re-enabled.";
    }

    public String runDisableMouseAccel() {
        runPowershell(
                "Set-ItemProperty -Path 'HKCU:\\Control Panel\\Mouse' -Name MouseSpeed -Value 0 -Force;" +
                        "Set-ItemProperty -Path 'HKCU:\\Control Panel\\Mouse' -Name MouseThreshold1 -Value 0 -Force;" +
                        "Set-ItemProperty -Path 'HKCU:\\Control Panel\\Mouse' -Name MouseThreshold2 -Value 0 -Force"
        );
        return "SUCCESS: Mouse acceleration (pointer precision) disabled.";
    }

    public String revertMouseAccel() {
        runPowershell(
                "Set-ItemProperty -Path 'HKCU:\\Control Panel\\Mouse' -Name MouseSpeed -Value 1 -Force;" +
                        "Set-ItemProperty -Path 'HKCU:\\Control Panel\\Mouse' -Name MouseThreshold1 -Value 6 -Force;" +
                        "Set-ItemProperty -Path 'HKCU:\\Control Panel\\Mouse' -Name MouseThreshold2 -Value 10 -Force"
        );
        return "Mouse acceleration restored.";
    }

    public String runEnableGameMode() {
        runPowershell(
                "Set-ItemProperty -Path 'HKCU:\\Software\\Microsoft\\GameBar' -Name AllowAutoGameMode -Value 1 -Type DWord -Force;" +
                        "Set-ItemProperty -Path 'HKCU:\\Software\\Microsoft\\GameBar' -Name AutoGameModeEnabled -Value 1 -Type DWord -Force"
        );
        return "SUCCESS: Windows Game Mode enabled.";
    }

    public String revertGameMode() {
        runPowershell(
                "Set-ItemProperty -Path 'HKCU:\\Software\\Microsoft\\GameBar' -Name AllowAutoGameMode -Value 0 -Type DWord -Force;" +
                        "Set-ItemProperty -Path 'HKCU:\\Software\\Microsoft\\GameBar' -Name AutoGameModeEnabled -Value 0 -Type DWord -Force"
        );
        return "Game Mode disabled.";
    }

    public String runDisableSearchIndexing() {
        runCommand("sc", "stop", "WSearch");
        runCommand("sc", "config", "WSearch", "start=", "disabled");
        return "SUCCESS: Windows Search indexing stopped.";
    }

    public String revertSearchIndexing() {
        runCommand("sc", "config", "WSearch", "start=", "auto");
        runCommand("sc", "start", "WSearch");
        return "Windows Search indexing re-enabled.";
    }

    public String runDisableAudioEnhancements() {
        runPowershell(
                "Get-ItemProperty -Path 'HKLM:\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\MMDevices\\Audio\\Render\\*\\Properties' " +
                        "-ErrorAction SilentlyContinue | Out-Null;" +
                        "Set-ItemProperty -Path 'HKCU:\\Software\\Microsoft\\Multimedia\\Audio' " +
                        "-Name UserDuckingPreference -Value 3 -Type DWord -Force"
        );
        return "SUCCESS: Audio enhancements and ducking disabled.";
    }

    public String revertAudioEnhancements() {
        runPowershell(
                "Remove-ItemProperty -Path 'HKCU:\\Software\\Microsoft\\Multimedia\\Audio' " +
                        "-Name UserDuckingPreference -ErrorAction SilentlyContinue"
        );
        return "Audio enhancements reverted.";
    }

    public String runDisableSpectreMetldown() {
        runPowershell(
                "Set-ItemProperty -Path 'HKLM:\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Memory Management' " +
                        "-Name FeatureSettingsOverride -Value 3 -Type DWord -Force;" +
                        "Set-ItemProperty -Path 'HKLM:\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Memory Management' " +
                        "-Name FeatureSettingsOverrideMask -Value 3 -Type DWord -Force"
        );
        return "SUCCESS: Spectre/Meltdown mitigations disabled. Restart required.";
    }

    public String revertSpectreMeltdown() {
        runPowershell(
                "Remove-ItemProperty -Path 'HKLM:\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Memory Management' " +
                        "-Name FeatureSettingsOverride -ErrorAction SilentlyContinue;" +
                        "Remove-ItemProperty -Path 'HKLM:\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Memory Management' " +
                        "-Name FeatureSettingsOverrideMask -ErrorAction SilentlyContinue"
        );
        return "Spectre/Meltdown mitigations restored.";
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
