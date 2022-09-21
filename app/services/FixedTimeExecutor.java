package services;

import actor.ActorProtocol;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import play.Logger;
import play.inject.ApplicationLifecycle;
import scala.concurrent.duration.Duration;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 定时脚本处理器
 */
@Singleton
public class FixedTimeExecutor {
    private final ActorSystem system;
    private final ApplicationLifecycle appLifecycle;
    Logger.ALogger logger = Logger.of(FixedTimeExecutor.class);

    @Inject
    public FixedTimeExecutor(ActorSystem system, ApplicationLifecycle appLifecycle) {
        this.system = system;
        this.appLifecycle = appLifecycle;
        Executor executor = Executors.newCachedThreadPool();
        CompletableFuture.runAsync(() -> schedule(), executor);
        appLifecycle.addStopHook(() -> {
            system.terminate();
            return CompletableFuture.completedFuture(null);
        });
    }

    public void schedule() {
        logger.info("schedule");
    }


}
