package com.staffs.api.common.rateLimit;

import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {
    private record Bucket(long start, int count) {}
    private final Map<String,Bucket> map = new ConcurrentHashMap<>();
    private final int LIMIT = 60; // 60/min per endpoint
    private final long WINDOW = 60;

    public boolean allow(String key) {
        long now = Instant.now().getEpochSecond();
        var b = map.get(key);
        if (b == null || now - b.start >= WINDOW) { map.put(key, new Bucket(now, 1)); return true; }
        if (b.count + 1 > LIMIT) return false;
        map.put(key, new Bucket(b.start, b.count + 1)); return true;
    }
}

