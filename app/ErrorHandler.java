/**
 * Created by win7 on 2016/6/10.
 */

import play.Logger;
import play.http.HttpErrorHandler;
import play.mvc.Http.RequestHeader;
import play.mvc.Result;
import play.mvc.Results;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ErrorHandler implements HttpErrorHandler {
    Logger.ALogger logger = Logger.of(ErrorHandler.class);

    public CompletionStage<Result> onClientError(RequestHeader request, int statusCode, String message) {
        return CompletableFuture.completedFuture(
                Results.badRequest("client error occurred: " + statusCode + message)
        );
    }

    public CompletionStage<Result> onServerError(RequestHeader request, Throwable exception) {
        logger.error("onServerError:" + exception.getMessage());
        return CompletableFuture.completedFuture(
                Results.internalServerError("server error occurred: " + exception.getMessage())
        );
    }
}
