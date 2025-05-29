package org.zapply.product.global.scheduler.task;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.support.TransactionTemplate;

@RequiredArgsConstructor
public class SchedulingTask implements Runnable {
    private final Runnable target;
    private final TransactionTemplate txTemplate;

    @Override
    public void run() {
        // 실행 시점에 트랜잭션 적용
        txTemplate.execute(status -> {
            target.run();
            return null;
        });
    }
}

