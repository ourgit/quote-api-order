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

    @Inject
    @Named("queryRefundActor")
    ActorRef queryRefundActorRef;

    @Inject
    @Named("queryPayResultActor")
    ActorRef queryPayResultActorRef;

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
        system.scheduler().schedule(
                Duration.create(600, TimeUnit.MILLISECONDS),
                Duration.create(5, TimeUnit.MINUTES),
                queryPayResultActorRef,
                new ActorProtocol.CHECK_PAY_RESULT(),
                system.dispatcher(),
                ActorRef.noSender()
        );

        system.scheduler().schedule(
                Duration.create(600, TimeUnit.MILLISECONDS),
                Duration.create(2, TimeUnit.MINUTES),
                queryRefundActorRef,
                new ActorProtocol.QUERY_REFUND_RESULT(),
                system.dispatcher(),
                ActorRef.noSender()
        );
    }


}
