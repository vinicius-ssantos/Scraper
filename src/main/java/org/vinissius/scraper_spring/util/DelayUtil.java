package org.vinissius.scraper_spring.util;

import java.util.Random;

public class DelayUtil {

    private static final Random RANDOM = new Random();
    private static final int MIN_DELAY_MS = 1000; // 1 segundo
    private static final int MAX_DELAY_MS = 5000; // 5 segundos

    public static void sleepRandomDelay() {
        int delay = MIN_DELAY_MS + RANDOM.nextInt(MAX_DELAY_MS - MIN_DELAY_MS + 1);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // Opcional: registrar o erro se necessário
        }
    }
}
