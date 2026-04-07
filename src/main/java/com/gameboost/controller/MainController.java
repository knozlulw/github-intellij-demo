package com.gameboost.controller;

import com.gameboost.model.*;
import com.gameboost.service.*;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.net.URL;
import java.util.*;

public class MainController implements Initializable {

    @FXML private Button navHome, navOptimize, navHistory, navLicense;
    @FXML private VBox homePanel, optimizePanel, historyPanel, licensePanel;

    // Home
    @FXML private Label cpuLabel, ramLabel, diskLabel, tempLabel;
    @FXML private Label pcProfileLabel, pcSummaryLabel;
    @FXML private ProgressBar cpuBar, ramBar, diskBar;
    @FXML private Label selectedCountLabel, recommendedLabel;

    // Optimize
    @FXML private VBox optimizationList;
    @FXML private Button optimizeNowBtn;
    @FXML private Label filterAll, filterMemory, filterCpu, filterNetwork, filterStorage, filterGpu, filterSystem, filterPrivacy;

    // History
    @FXML private VBox historyList;

    // License
    @FXML private TextField licenseKeyField;
    @FXML private Label licenseResultLabel, usageLabel, licenseStatusLabel, licenseStatusLabel2;

    private final StorageService       storage     = new StorageService();
    private final PcScanService        scanner     = new PcScanService();
    private final OptimizationService  optService  = new OptimizationService();
    private final SystemStatsService   stats       = new SystemStatsService();

    private PcProfile profile;
    private LicenseState license;
    private List<OptimizationEntry> optimizations;
    private String activeFilter = "ALL";
    private Timeline statsTimer;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        license = storage.loadLicense();

        if (storage.hasProfile()) {
            profile = storage.loadProfile();
        } else {
            scanPcAsync();
        }

        optimizations = optService.buildOptimizationList(profile);
        setupNavigation();
        setupFilterButtons();
        buildOptimizationCards("ALL");
        updateLicenseUI();
        startStatsTimer();
        showPanel(homePanel, navHome);
        if (profile != null) updateProfileUI();
        updateSelectedCount();
    }

    // =========================================================================
    //  NAVIGATION
    // =========================================================================

    private void setupNavigation() {
        navHome.setOnAction(e     -> showPanel(homePanel, navHome));
        navOptimize.setOnAction(e -> showPanel(optimizePanel, navOptimize));
        navHistory.setOnAction(e  -> { loadHistoryPanel(); showPanel(historyPanel, navHistory); });
        navLicense.setOnAction(e  -> showPanel(licensePanel, navLicense));
    }

    private void showPanel(VBox panel, Button active) {
        homePanel.setVisible(false); optimizePanel.setVisible(false);
        historyPanel.setVisible(false); licensePanel.setVisible(false);
        panel.setVisible(true);
        for (Button b : List.of(navHome, navOptimize, navHistory, navLicense))
            b.getStyleClass().remove("nav-active");
        active.getStyleClass().add("nav-active");
    }

    // =========================================================================
    //  FILTER TABS
    // =========================================================================

    private void setupFilterButtons() {
        Map<Label, String> filters = new LinkedHashMap<>();
        filters.put(filterAll,     "ALL");
        filters.put(filterMemory,  "MEMORY");
        filters.put(filterCpu,     "CPU");
        filters.put(filterGpu,     "GPU");
        filters.put(filterNetwork, "NETWORK");
        filters.put(filterStorage, "STORAGE");
        filters.put(filterSystem,  "SYSTEM");
        filters.put(filterPrivacy, "PRIVACY");

        for (Map.Entry<Label, String> e : filters.entrySet()) {
            Label lbl = e.getKey();
            String cat = e.getValue();
            if (lbl == null) continue;
            lbl.setOnMouseClicked(ev -> {
                activeFilter = cat;
                filters.keySet().forEach(l -> { if (l != null) l.getStyleClass().remove("filter-active"); });
                lbl.getStyleClass().add("filter-active");
                buildOptimizationCards(cat);
            });
        }
    }

    // =========================================================================
    //  HOME STATS
    // =========================================================================

    private void startStatsTimer() {
        statsTimer = new Timeline(new KeyFrame(Duration.seconds(2), e -> refreshStats()));
        statsTimer.setCycleCount(Animation.INDEFINITE);
        statsTimer.play();
    }

    private void refreshStats() {
        Task<double[]> t = new Task<>() {
            @Override protected double[] call() {
                return new double[]{
                    stats.getCpuUsagePercent(),
                    stats.getUsedRamGb(), stats.getTotalRamGb(),
                    stats.getFreeDiskGb(), stats.getTotalDiskGb(),
                    stats.getCpuTemperature()
                };
            }
        };
        t.setOnSucceeded(e -> {
            double[] d = t.getValue();
            double cpuPct  = d[0];
            double ramUsed = d[1], ramTotal = d[2];
            double diskFree= d[3], diskTotal= d[4];
            double temp    = d[5];

            cpuLabel.setText(String.format("%.0f%%", cpuPct));
            cpuBar.setProgress(cpuPct / 100.0);
            styleBar(cpuBar, cpuPct);

            double ramPct = ramTotal > 0 ? ramUsed / ramTotal * 100 : 0;
            ramLabel.setText(String.format("%.1f / %.0f GB", ramUsed, ramTotal));
            ramBar.setProgress(ramPct / 100.0);
            styleBar(ramBar, ramPct);

            double diskPct = diskTotal > 0 ? (diskTotal - diskFree) / diskTotal * 100 : 0;
            diskLabel.setText(String.format("%.0f GB free", diskFree));
            diskBar.setProgress(diskPct / 100.0);
            styleBar(diskBar, diskPct);

            if (temp > 0) {
                tempLabel.setText(String.format("%.0f°C", temp));
                tempLabel.setStyle(temp > 85 ? "-fx-text-fill:#ff4d4d;" :
                                   temp > 70 ? "-fx-text-fill:#f59e0b;" :
                                               "-fx-text-fill:#10b981;");
            } else {
                tempLabel.setText("N/A");
                tempLabel.setStyle("-fx-text-fill:#6b7280;");
            }
        });
        new Thread(t).start();
    }

    private void styleBar(ProgressBar bar, double pct) {
        String c = pct > 90 ? "#ef4444" : pct > 70 ? "#f59e0b" : "#10b981";
        bar.setStyle("-fx-accent:" + c + ";");
    }

    // =========================================================================
    //  PC PROFILE
    // =========================================================================

    private void scanPcAsync() {
        Task<PcProfile> t = new Task<>() {
            @Override protected PcProfile call() { return scanner.scan(); }
        };
        t.setOnSucceeded(e -> {
            profile = t.getValue();
            storage.saveProfile(profile);
            optimizations = optService.buildOptimizationList(profile);
            Platform.runLater(() -> {
                buildOptimizationCards(activeFilter);
                updateProfileUI();
                updateSelectedCount();
            });
        });
        new Thread(t).start();
    }

    private void updateProfileUI() {
        if (profile == null) return;
        pcProfileLabel.setText(
            profile.cpuModel + "   ·   " + profile.ramTotalGb + " GB RAM   ·   " + profile.gpuModel);
        String summary = profile.getSummary();
        pcSummaryLabel.setText(summary.isEmpty() ? "Your hardware configuration looks good." : summary);

        long recommended = optimizations.stream().filter(OptimizationEntry::isEnabled).count();
        if (recommendedLabel != null)
            recommendedLabel.setText(recommended + " optimizations recommended for your hardware");
    }

    // =========================================================================
    //  OPTIMIZATION CARDS
    // =========================================================================

    private void buildOptimizationCards(String filter) {
        Platform.runLater(() -> {
            optimizationList.getChildren().clear();
            optimizations.stream()
                .filter(o -> filter.equals("ALL") || o.getCategoryLabel().equals(filter))
                .forEach(o -> optimizationList.getChildren().add(buildCard(o)));
        });
    }

    private HBox buildCard(OptimizationEntry opt) {
        HBox card = new HBox(14);
        card.getStyleClass().add("opt-card");
        card.setAlignment(Pos.CENTER_LEFT);

        // Left: custom toggle
        ToggleButton toggle = new ToggleButton();
        toggle.setSelected(opt.isEnabled());
        toggle.getStyleClass().add("toggle-btn");
        toggle.selectedProperty().addListener((obs, o, n) -> {
            opt.setEnabled(n);
            updateSelectedCount();
        });

        // Category pill
        Label catPill = new Label(opt.getCategoryLabel());
        catPill.getStyleClass().addAll("cat-pill", "cat-" + opt.getCategoryLabel().toLowerCase());

        // Text
        VBox text = new VBox(4);
        HBox.setHgrow(text, Priority.ALWAYS);
        Label title = new Label(opt.getTitle());
        title.getStyleClass().add("opt-title");
        Label desc = new Label(opt.getDescription());
        desc.getStyleClass().add("opt-desc");
        desc.setWrapText(true);
        text.getChildren().addAll(title, desc);

        // Risk badge
        Label risk = new Label(opt.getRiskLabel());
        risk.getStyleClass().addAll("risk-pill", "risk-" + opt.getRiskLevel().name().toLowerCase());
        if (opt.getRiskTooltip() != null) {
            Tooltip tt = new Tooltip(opt.getRiskTooltip());
            tt.setWrapText(true);
            tt.setMaxWidth(260);
            Tooltip.install(risk, tt);
        }

        // Revert
        Button revert = new Button("Undo");
        revert.getStyleClass().add("undo-btn");
        revert.setVisible(false);
        revert.setManaged(false);
        revert.setId("revert_" + opt.getId());
        revert.setOnAction(e -> {
            runRevert(opt);
            revert.setVisible(false);
            revert.setManaged(false);
        });
        card.setUserData(revert);

        // Restart badge
        if (opt.getRiskLevel() == OptimizationEntry.RiskLevel.RESTART_REQUIRED) {
            card.getStyleClass().add("card-restart");
        }

        card.getChildren().addAll(toggle, catPill, text, risk);
        if (opt.isRevertable()) card.getChildren().add(revert);
        return card;
    }

    private void updateSelectedCount() {
        long count = optimizations.stream().filter(OptimizationEntry::isEnabled).count();
        if (selectedCountLabel != null)
            selectedCountLabel.setText(count + " selected");
        if (optimizeNowBtn != null)
            optimizeNowBtn.setText(count > 0 ? "Run " + count + " Optimizations" : "Select Optimizations");
    }

    // =========================================================================
    //  OPTIMIZE NOW
    // =========================================================================

    @FXML
    private void onGoToOptimize() { showPanel(optimizePanel, navOptimize); }

    @FXML
    private void onOptimizeNow() {
        if (!license.canOptimize()) { showUpgradeDialog(); return; }

        List<OptimizationEntry> toRun = optimizations.stream()
            .filter(OptimizationEntry::isEnabled).toList();
        if (toRun.isEmpty()) {
            showAlert("Nothing selected", "Enable at least one optimization first.");
            return;
        }

        optimizeNowBtn.setDisable(true);
        optimizeNowBtn.setText("Running...");

        Task<Void> t = new Task<>() {
            @Override protected Void call() {
                for (OptimizationEntry opt : toRun) {
                    if (!license.canOptimize()) break;
                    String result = execute(opt);
                    storage.appendHistory(new HistoryEntry(opt.getId(), opt.getTitle(), result));
                    if (!license.isPro) { license.optimizationsUsed++; storage.saveLicense(license); }
                    Platform.runLater(() -> { opt.setApplied(true); revealUndo(opt); });
                }
                return null;
            }
        };
        t.setOnSucceeded(e -> {
            optimizeNowBtn.setDisable(false);
            updateSelectedCount();
            updateLicenseUI();
        });
        new Thread(t).start();
    }

    private String execute(OptimizationEntry opt) {
        try {
            return switch (opt.getId()) {
                case "kill_processes"      -> optService.runKillProcesses();
                case "clear_ram"           -> optService.runClearRam();
                case "disable_superfetch"  -> optService.runDisableSuperFetch();
                case "power_plan"          -> optService.runSetHighPerformancePower();
                case "disable_core_parking"-> optService.runDisableCoreParking();
                case "high_priority_games" -> optService.runHighPriorityGames();
                case "disable_hw_accel_gpu"-> optService.runDisableHWAccelGPUSched();
                case "gpu_performance"     -> optService.runGpuPerformance(profile);
                case "disable_fullscreen_opt"-> optService.runDisableFullscreenOpt();
                case "network_latency"     -> optService.runNetworkLatency();
                case "network_throttling"  -> optService.runDisableNetworkThrottling();
                case "dns_cache"           -> optService.runFlushDns();
                case "clear_temp"          -> optService.runClearTemp();
                case "disable_prefetch_writes"-> optService.runDisablePrefetchWrites();
                case "disable_gamebar"     -> optService.runDisableGameBar();
                case "disable_telemetry"   -> optService.runDisableTelemetry();
                case "disable_notifications"-> optService.runDisableNotifications();
                case "visual_effects"      -> optService.runReduceVisualEffects();
                default -> "SKIPPED: Unknown ID";
            };
        } catch (Exception e) { return "FAILED: " + e.getMessage(); }
    }

    private void revealUndo(OptimizationEntry opt) {
        optimizationList.getChildren().stream()
            .filter(n -> n instanceof HBox)
            .map(n -> (HBox) n)
            .filter(c -> c.getUserData() instanceof Button btn &&
                         btn.getId() != null && btn.getId().equals("revert_" + opt.getId()))
            .findFirst()
            .ifPresent(c -> {
                Button btn = (Button) c.getUserData();
                btn.setVisible(true); btn.setManaged(true);
            });
    }

    private void runRevert(OptimizationEntry opt) {
        String result = switch (opt.getId()) {
            case "power_plan"            -> optService.revertPowerPlan();
            case "disable_superfetch"    -> optService.revertSuperFetch();
            case "disable_core_parking"  -> optService.revertCoreParking();
            case "disable_hw_accel_gpu"  -> optService.revertHWAccelGPUSched();
            case "gpu_performance"       -> optService.revertGpuPerformance();
            case "disable_fullscreen_opt"-> optService.revertFullscreenOpt();
            case "network_latency"       -> optService.revertNetworkLatency();
            case "network_throttling"    -> optService.revertNetworkThrottling();
            case "disable_prefetch_writes"-> optService.revertPrefetchWrites();
            case "disable_gamebar"       -> optService.revertGameBar();
            case "disable_telemetry"     -> optService.revertTelemetry();
            case "disable_notifications" -> optService.revertNotifications();
            case "visual_effects"        -> optService.revertVisualEffects();
            default -> "No revert available.";
        };
        storage.appendHistory(new HistoryEntry(opt.getId(), opt.getTitle() + " [REVERTED]", result));
        opt.setApplied(false);
    }

    // =========================================================================
    //  HISTORY
    // =========================================================================

    private void loadHistoryPanel() {
        historyList.getChildren().clear();
        List<HistoryEntry> history = storage.loadHistory();
        if (history.isEmpty()) {
            Label e = new Label("No optimizations have been applied yet.");
            e.getStyleClass().add("empty-label");
            historyList.getChildren().add(e);
            return;
        }
        for (HistoryEntry entry : history)
            historyList.getChildren().add(buildHistoryRow(entry));
    }

    private HBox buildHistoryRow(HistoryEntry entry) {
        HBox row = new HBox(12);
        row.getStyleClass().add("history-row");
        row.setAlignment(Pos.CENTER_LEFT);

        boolean ok = entry.result != null && entry.result.startsWith("SUCCESS");
        Label dot = new Label(ok ? "●" : "●");
        dot.setStyle("-fx-text-fill:" + (ok ? "#10b981" : "#ef4444") + ";-fx-font-size:10px;");

        Label time = new Label(entry.getFormattedTime());
        time.getStyleClass().add("history-time");
        time.setMinWidth(155);

        Label title = new Label(entry.optimizationTitle);
        title.getStyleClass().add("history-title");
        HBox.setHgrow(title, Priority.ALWAYS);

        Label status = new Label(ok ? "Success" : "Failed");
        status.getStyleClass().add(ok ? "badge-success" : "badge-fail");

        row.getChildren().addAll(dot, time, title, status);
        return row;
    }

    // =========================================================================
    //  LICENSE
    // =========================================================================

    @FXML
    private void onActivateLicense() {
        String key = licenseKeyField.getText().trim();
        if (LicenseState.validateKey(key)) {
            license.isPro = true;
            license.licenseKey = key;
            storage.saveLicense(license);
            licenseResultLabel.setText("Pro license activated — unlimited optimizations unlocked.");
            licenseResultLabel.setStyle("-fx-text-fill:#10b981;");
            updateLicenseUI();
        } else {
            licenseResultLabel.setText("Invalid key. Expected format: GB-XXXX-XXXX-XXXX");
            licenseResultLabel.setStyle("-fx-text-fill:#ef4444;");
        }
    }

    private void updateLicenseUI() {
        Platform.runLater(() -> {
            if (license.isPro) {
                setLabel(licenseStatusLabel,  "PRO", "-fx-text-fill:#10b981;");
                setLabel(licenseStatusLabel2, "PRO", "-fx-text-fill:#10b981;");
                usageLabel.setText("Unlimited optimizations active.");
            } else {
                int left = license.remainingFree();
                String style = "-fx-text-fill:" + (left < 5 ? "#f59e0b" : "#9ca3af") + ";";
                setLabel(licenseStatusLabel,  "FREE  ·  " + left + " left", style);
                setLabel(licenseStatusLabel2, "FREE  ·  " + left + " left", style);
                usageLabel.setText(license.optimizationsUsed + " of " + LicenseState.FREE_TIER_LIMIT + " free runs used.");
            }
        });
    }

    // =========================================================================
    //  UTIL
    // =========================================================================

    private void showUpgradeDialog() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Upgrade to Pro"); a.setHeaderText("Free tier limit reached");
        a.setContentText("You've used all " + LicenseState.FREE_TIER_LIMIT +
            " free optimizations.\nEnter a Pro license key on the License tab.");
        a.getDialogPane().getStylesheets().add(
            getClass().getResource("/com/gameboost/css/dark-theme.css").toExternalForm());
        a.showAndWait();
    }

    private void setLabel(Label lbl, String text, String style) {
        if (lbl != null) { lbl.setText(text); lbl.setStyle(style); }
    }

    private void showAlert(String title, String content) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(content);
        a.getDialogPane().getStylesheets().add(
            getClass().getResource("/com/gameboost/css/dark-theme.css").toExternalForm());
        a.showAndWait();
    }
}
