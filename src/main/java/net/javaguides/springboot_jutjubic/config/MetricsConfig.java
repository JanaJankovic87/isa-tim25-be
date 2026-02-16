package net.javaguides.springboot_jutjubic.config;

import org.springframework.context.annotation.Configuration;


@Configuration
public class MetricsConfig {

    //PRODUKCIJA: 30 minuta
    // TESTIRANJE: 2 minuta
    public static final int INACTIVITY_TIMEOUT_MINUTES = 30;

    // PRODUKCIJA: 60000 (1 minut)
    // TESTIRANJE: 10000 (10 sekundi - za brže testiranje)
    public static final long CLEANUP_INTERVAL_MS = 60000;

    public static final boolean DEBUG_MODE = true;
}

