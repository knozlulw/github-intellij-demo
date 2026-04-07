package com.gameboost.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameboost.model.HistoryEntry;
import com.gameboost.model.LicenseState;
import com.gameboost.model.PcProfile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * All disk I/O lives here.
 * Data stored at: %USERPROFILE%\.gameboost\
 */
public class StorageService {

    private static final String APP_DIR =
        System.getProperty("user.home") + File.separator + ".gameboost";

    private static final String PROFILE_FILE  = APP_DIR + File.separator + "pc-profile.json";
    private static final String HISTORY_FILE  = APP_DIR + File.separator + "history.json";
    private static final String LICENSE_FILE  = APP_DIR + File.separator + "license.json";

    private final ObjectMapper mapper = new ObjectMapper();

    public StorageService() {
        ensureAppDir();
    }

    private void ensureAppDir() {
        try {
            Files.createDirectories(Path.of(APP_DIR));
        } catch (IOException e) {
            System.err.println("[Storage] Could not create app dir: " + e.getMessage());
        }
    }

    // --- PcProfile ---

    public boolean hasProfile() {
        return new File(PROFILE_FILE).exists();
    }

    public void saveProfile(PcProfile profile) {
        writeJson(PROFILE_FILE, profile);
    }

    public PcProfile loadProfile() {
        return readJson(PROFILE_FILE, PcProfile.class);
    }

    // --- History ---

    public List<HistoryEntry> loadHistory() {
        HistoryEntry[] arr = readJson(HISTORY_FILE, HistoryEntry[].class);
        return arr != null ? new ArrayList<>(Arrays.asList(arr)) : new ArrayList<>();
    }

    public void saveHistory(List<HistoryEntry> history) {
        writeJson(HISTORY_FILE, history);
    }

    public void appendHistory(HistoryEntry entry) {
        List<HistoryEntry> history = loadHistory();
        history.add(0, entry);  // newest first
        if (history.size() > 200) history = history.subList(0, 200);  // cap size
        saveHistory(history);
    }

    // --- License ---

    public LicenseState loadLicense() {
        LicenseState state = readJson(LICENSE_FILE, LicenseState.class);
        return state != null ? state : new LicenseState();
    }

    public void saveLicense(LicenseState state) {
        writeJson(LICENSE_FILE, state);
    }

    // --- Helpers ---

    private void writeJson(String path, Object obj) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(path), obj);
        } catch (IOException e) {
            System.err.println("[Storage] Write failed for " + path + ": " + e.getMessage());
        }
    }

    private <T> T readJson(String path, Class<T> clazz) {
        File f = new File(path);
        if (!f.exists()) return null;
        try {
            return mapper.readValue(f, clazz);
        } catch (IOException e) {
            System.err.println("[Storage] Read failed for " + path + ": " + e.getMessage());
            return null;
        }
    }
}
