package org.zapply.product.global.scheduler.repository;

import org.springframework.stereotype.Repository;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Repository
public class SchedulingRepository {
    private final Map<Long, ScheduledFuture<?>> futures = new ConcurrentHashMap<>();

    public void add(Long id, ScheduledFuture<?> future) {
        futures.put(id, future);
    }

    public ScheduledFuture<?> remove(Long id) {
        return futures.remove(id);
    }

    public boolean exists(Long id) {
        return futures.containsKey(id);
    }
}
