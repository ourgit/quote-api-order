package filter;

import akka.stream.Materializer;
import play.Logger;
import play.cache.SyncCacheApi;
import play.mvc.Filter;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static constants.RedisKeyConstant.KEY_SYSTEM_UNDER_MAINTAINCE;
import static play.mvc.Results.redirect;

/**
 * Created by win7 on 2016/7/1.
 */
public class MaintenceFilter extends Filter {

    @Inject
    public MaintenceFilter(Materializer mat) {
        super(mat);
    }

    @Inject
    SyncCacheApi cache;

    @Override
    public CompletionStage<Result> apply(
            Function<Http.RequestHeader, CompletionStage<Result>> nextFilter,
            Http.RequestHeader requestHeader) {
        return nextFilter.apply(requestHeader).thenApply(result -> {
            String underMaintaince = cache.getOrElseUpdate(KEY_SYSTEM_UNDER_MAINTAINCE, () -> "");
            if (null != underMaintaince && underMaintaince.equalsIgnoreCase(KEY_SYSTEM_UNDER_MAINTAINCE)) {
                Logger.info("进入维护模式...");
                return redirect("https://13.125.214.76/maintaince");
            } else return result;
        });
    }
}
