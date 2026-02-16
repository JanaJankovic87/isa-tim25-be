package net.javaguides.springboot_jutjubic.scheduler;

import net.javaguides.springboot_jutjubic.service.impl.ThumbnailCompressionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ThumbnailCompressionScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ThumbnailCompressionScheduler.class);

    @Autowired
    private ThumbnailCompressionService thumbnailCompressionService;

    @Scheduled(cron = "${app.compression.cron:0 0 2 * * ?}")
    public void runDailyThumbnailCompression() {
        logger.info(" Dnevna kompresija thumbnail slika pokrenuta ");

        try {
            long start = System.currentTimeMillis();
            int count = thumbnailCompressionService.compressOldThumbnails();
            long duration = System.currentTimeMillis() - start;

            logger.info(" Zavrseno za {} ms , Kompresovano: {} slika ", duration, count);
        } catch (Exception e) {
            logger.error("Greska tokom kompresije: {}", e.getMessage(), e);
        }
    }
}