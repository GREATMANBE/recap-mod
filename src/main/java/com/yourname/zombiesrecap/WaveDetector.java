package com.yourname.zombiesrecap;

public class WaveDetector {

    private static final long WAVE_GAP_MS = 3500;
    private long lastSpawnTime = 0;

    public boolean onZombieSpawn() {
        long now = System.currentTimeMillis();
        boolean newWave = lastSpawnTime != 0 && now - lastSpawnTime >= WAVE_GAP_MS;
        lastSpawnTime = now;
        return newWave;
    }

    public void reset() {
        lastSpawnTime = 0;
    }
}
