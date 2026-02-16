package net.javaguides.springboot_jutjubic.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import net.javaguides.springboot_jutjubic.config.MetricsConfig;
import net.javaguides.springboot_jutjubic.security.TokenAuthenticationFilter;
import net.javaguides.springboot_jutjubic.util.TokenUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class ActiveUsersMetricsService {

    private final MeterRegistry meterRegistry;
    private final RedisTemplate<String, Object> monitoringRedis;
    private final Map<String, LocalDateTime> activeUsers = new ConcurrentHashMap<>();
    private final Counter loginCounter;

    private static final String ACTIVE_USERS_KEY = "monitoring:active:users";

    public ActiveUsersMetricsService(
            MeterRegistry meterRegistry,
            @Qualifier("monitoringRedisTemplate") RedisTemplate<String, Object> monitoringRedis) {

        this.meterRegistry = meterRegistry;
        this.monitoringRedis = monitoringRedis;

        // RESET: Očisti Redis pri inicijalizaciji
        try {
            monitoringRedis.delete(ACTIVE_USERS_KEY);
            activeUsers.clear();
            System.out.println("ActiveUsersMetricsService INICIJALIZACIJA - Redis i memory resetovani");
        } catch (Exception e) {
            System.err.println("Greška pri čišćenju Redis-a: " + e.getMessage());
        }

        // Gauge za broj aktivnih korisnika
        Gauge.builder("active.users.count", this, service -> service.getActiveUsersCount())
                .description("Number of currently active users")
                .register(meterRegistry);

        // Counter za ukupan broj logovanja
        this.loginCounter = Counter.builder("user.logins.total")
                .description("Total number of user logins")
                .register(meterRegistry);

        System.out.println("ActiveUsersMetricsService inicijalizovan - početno stanje: memory=" + activeUsers.size());
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter(
            TokenUtils tokenUtils,
            UserDetailsService userDetailsService,
            ActiveUsersMetricsService activeUsersMetrics) {

        return new TokenAuthenticationFilter(
                tokenUtils,
                userDetailsService,
                activeUsersMetrics
        );
    }


    public void recordUserLogin(String userId) {
        activeUsers.put(userId, LocalDateTime.now());
        loginCounter.increment();

        // čuvaj aktivne korisnike i u Redis-u sa timestampom
        monitoringRedis.opsForHash().put(ACTIVE_USERS_KEY, userId, System.currentTimeMillis());
        monitoringRedis.expire(ACTIVE_USERS_KEY, 30, TimeUnit.MINUTES);
    }

    public void recordUserLogout(String userId) {
        activeUsers.remove(userId);
        monitoringRedis.opsForHash().delete(ACTIVE_USERS_KEY, userId);
    }

    public int getActiveUsersCount() {
        // PRVO: Očisti neaktivne korisnike
        removeInactiveUsers();

        // Kombinuj broj iz memorije i Redis-a
        int memoryCount = activeUsers.size();

        try {
            Set<Object> redisUsers = monitoringRedis.opsForHash().keys(ACTIVE_USERS_KEY);
            int redisCount = (redisUsers != null) ? redisUsers.size() : 0;

            // Vrati veći broj (za slučaj desinhronizacije)
            int count = Math.max(memoryCount, redisCount);

            System.out.println("getActiveUsersCount() - memory: " + memoryCount + ", redis: " + redisCount + ", returning: " + count);
            return count;
        } catch (Exception e) {
            System.err.println("Greška pri čitanju iz Redis-a, vraćam memory count: " + e.getMessage());
            return memoryCount;
        }
    }

    public void removeInactiveUsers() {
        System.out.println("removeInactiveUsers() - START");

        LocalDateTime threshold = LocalDateTime.now().minusMinutes(MetricsConfig.INACTIVITY_TIMEOUT_MINUTES);
        System.out.println("Threshold: " + threshold + " (" + MetricsConfig.INACTIVITY_TIMEOUT_MINUTES + " minuta unazad)");

        // obriši neaktivne korisnike iz memorije
        int beforeMemory = activeUsers.size();
        System.out.println("Broj korisnika u memoriji PRE čišćenja: " + beforeMemory);

        activeUsers.entrySet().removeIf(entry -> {
            boolean isInactive = entry.getValue().isBefore(threshold);
            if (isInactive) {
                System.out.println("  ❌ Brisanje iz memorije: " + entry.getKey() + " (last active: " + entry.getValue() + ")");
            } else if (MetricsConfig.DEBUG_MODE) {
                System.out.println("  ✅ Još aktivan: " + entry.getKey() + " (last active: " + entry.getValue() + ")");
            }
            return isInactive;
        });
        int afterMemory = activeUsers.size();
        int removedFromMemory = beforeMemory - afterMemory;

        // obriši neaktivne korisnike iz Redis-a
        long currentTime = System.currentTimeMillis();
        long thresholdTime = currentTime - (MetricsConfig.INACTIVITY_TIMEOUT_MINUTES * 60 * 1000);

        int removedFromRedis = 0;
        try {
            Map<Object, Object> allUsers = monitoringRedis.opsForHash().entries(ACTIVE_USERS_KEY);
            System.out.println("Broj korisnika u Redis-u PRE čišćenja: " + allUsers.size());

            for (Map.Entry<Object, Object> entry : allUsers.entrySet()) {
                long timestamp = (Long) entry.getValue();
                boolean isInactive = timestamp < thresholdTime;

                if (isInactive) {
                    monitoringRedis.opsForHash().delete(ACTIVE_USERS_KEY, entry.getKey());
                    System.out.println("  ❌ Brisanje iz Redis-a: " + entry.getKey() + " (timestamp: " + new java.util.Date(timestamp) + ")");
                    removedFromRedis++;
                } else if (MetricsConfig.DEBUG_MODE) {
                    System.out.println("  ✅ Još aktivan u Redis-u: " + entry.getKey() + " (timestamp: " + new java.util.Date(timestamp) + ")");
                }
            }
        } catch (Exception e) {
            System.err.println("Greška pri brisanju iz Redis-a: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("removeInactiveUsers() - ZAVRŠENO");
        System.out.println("  Obrisano iz memorije: " + removedFromMemory);
        System.out.println("  Obrisano iz Redis-a: " + removedFromRedis);
        System.out.println("  Trenutno aktivnih (memory): " + activeUsers.size());
    }
}