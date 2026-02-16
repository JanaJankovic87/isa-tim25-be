package net.javaguides.springboot_jutjubic.scheduler;

import net.javaguides.springboot_jutjubic.config.MetricsConfig;
import net.javaguides.springboot_jutjubic.metrics.ActiveUsersMetricsService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    private final ActiveUsersMetricsService activeUsersMetrics;

    public ScheduledTasks(ActiveUsersMetricsService activeUsersMetrics) {
        this.activeUsersMetrics = activeUsersMetrics;
        System.out.println("ScheduledTasks inicijalizovan");
        System.out.println("  - Cleanup interval: " + MetricsConfig.CLEANUP_INTERVAL_MS + " ms (" + (MetricsConfig.CLEANUP_INTERVAL_MS / 1000) + " sekundi)");
        System.out.println("  - Inactivity timeout: " + MetricsConfig.INACTIVITY_TIMEOUT_MINUTES + " minuta");
    }

    // Periodično ukloni neaktivne korisnike
    @Scheduled(fixedRate = 60000) // TODO: Ne može da se stavi konstanta ovde - mora hard-coded
    public void cleanupInactiveUsers() {
        System.out.println("\n========================================");
        System.out.println("⏰ Scheduler: cleanupInactiveUsers() pozvan");
        System.out.println("🕐 Vreme: " + java.time.LocalDateTime.now());
        System.out.println("========================================");

        activeUsersMetrics.removeInactiveUsers();

    }
}