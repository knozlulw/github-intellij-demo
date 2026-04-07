package com.gameboost.service;

import com.gameboost.model.PcProfile;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OperatingSystem;

import java.util.List;

/**
 * Uses OSHI library to read hardware/OS info.
 * Run once on first launch, result cached to disk.
 */
public class PcScanService {

    public PcProfile scan() {
        PcProfile profile = new PcProfile();

        try {
            SystemInfo si = new SystemInfo();
            HardwareAbstractionLayer hal = si.getHardware();
            OperatingSystem os = si.getOperatingSystem();

            // CPU
            CentralProcessor cpu = hal.getProcessor();
            profile.cpuModel = cpu.getProcessorIdentifier().getName().trim();
            profile.cpuCores = cpu.getLogicalProcessorCount();

            // RAM
            GlobalMemory mem = hal.getMemory();
            long totalBytes = mem.getTotal();
            profile.ramTotalGb = totalBytes / (1024L * 1024L * 1024L);
            profile.lowRam = profile.ramTotalGb < 8;

            // GPU — OSHI returns list; grab first discrete if available
            List<GraphicsCard> gpus = hal.getGraphicsCards();
            if (!gpus.isEmpty()) {
                // Prefer non-"Microsoft Basic" adapter
                GraphicsCard primary = gpus.stream()
                    .filter(g -> !g.getName().toLowerCase().contains("microsoft basic"))
                    .findFirst()
                    .orElse(gpus.get(0));
                profile.gpuModel = primary.getName().trim();
                String gpuLower = profile.gpuModel.toLowerCase();
                profile.hasNvidiaGpu = gpuLower.contains("nvidia") || gpuLower.contains("geforce");
                profile.hasAmdGpu = gpuLower.contains("amd") || gpuLower.contains("radeon");
            } else {
                profile.gpuModel = "Unknown";
            }

            // Storage — check if primary disk is SSD or HDD
            List<HWDiskStore> disks = hal.getDiskStores();
            boolean foundSsd = false;
            boolean foundHdd = false;
            for (HWDiskStore disk : disks) {
                String model = disk.getModel().toLowerCase();
                if (model.contains("ssd") || model.contains("nvme") || model.contains("solid")) {
                    foundSsd = true;
                } else {
                    foundHdd = true;
                }
            }
            // If we can't tell from name, default to "SSD" for modern systems
            profile.storageType = foundHdd && !foundSsd ? "HDD" : "SSD";
            profile.hasHdd = foundHdd;

            // OS
            profile.osName = os.getFamily();
            profile.osVersion = os.getVersionInfo().getVersion();

        } catch (Exception e) {
            // Fallback — don't crash if OSHI fails on some configs
            System.err.println("[PcScan] Error scanning hardware: " + e.getMessage());
            profile.cpuModel = "Unknown CPU";
            profile.gpuModel = "Unknown GPU";
            profile.ramTotalGb = 8;
            profile.storageType = "SSD";
            profile.osName = "Windows";
            profile.osVersion = "10";
        }

        profile.scanTimestamp = System.currentTimeMillis();
        return profile;
    }
}
