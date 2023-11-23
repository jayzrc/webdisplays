package net.montoyo.wd.client.js;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.montoyo.wd.entity.ScreenData;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandler;
import org.cef.handler.CefMessageRouterHandlerAdapter;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class WDRouter extends CefMessageRouterHandlerAdapter {
    public static final WDRouter INSTANCE = new WDRouter();

    private static boolean exists = false;

    public WDRouter() {
        if (exists) throw new RuntimeException("Can only have one WD message router.");
        exists = true;
    }

    class QueryData {
        CefBrowser browser;
        String type;
        BiConsumer<String, CefQueryCallback> consumer;

        public QueryData(CefBrowser browser, String type, BiConsumer<String, CefQueryCallback> consumer) {
            this.browser = browser;
            this.type = type;
            this.consumer = consumer;
        }
    }

    ArrayList<QueryData> awaitingQueries = new ArrayList<>();

    @Override
    public boolean onQuery(CefBrowser browser, CefFrame frame, long queryId, String request, boolean persistent, CefQueryCallback callback) {
        if (request.startsWith("WebDisplays_")) {
            request = request.substring("Webdisplays_".length());

            QueryData target = null;
            for (QueryData awaitingQuery : awaitingQueries) {
                if (browser != awaitingQuery.browser) continue;

                if (request.startsWith(awaitingQuery.type)) {
                    String requestData = request.substring(awaitingQuery.type.length());
                    target = awaitingQuery;
                    awaitingQuery.consumer.accept(requestData, callback);
                    break;
                }
            }

            if (target != null) {
                awaitingQueries.remove(target);
                callback.success("");
            } else {
                callback.failure(-1, "Query " + queryId + " with data " + request + " completed, but there was no active request waiting for the result.");
            }

            return true;
        }
        return false;
    }

    private static final Gson gson = new Gson();

    public class Task<T> {
        QueryData qd;
        CompletableFuture<T> wrapped;

        public Task(QueryData qd, CompletableFuture<T> wrapped) {
            this.qd = qd;
            this.wrapped = wrapped;
        }

        public void cancel() {
            wrapped.cancel(true);
            awaitingQueries.remove(qd);
        }

        public Task<T> thenAccept(Consumer<T> consumer) {
            wrapped.thenAccept(consumer);
            return this;
        }
    }

    public Task<JsonObject> requestJson(CefBrowser screen, String queryType, String script) {
        JsonObject[] obj = new JsonObject[1];

        QueryData qd = new QueryData(
                screen, queryType,
                (data, context) -> {
                    obj[0] = gson.fromJson(data, JsonObject.class);
                }
        );
        awaitingQueries.add(qd);

        screen.executeJavaScript(script, "", 0);

        return new Task<>(
                qd,
                CompletableFuture.supplyAsync(() -> {
                    while (obj[0] == null) {
                        try {
                            Thread.sleep(1);
                        } catch (Throwable ignored) {
                        }
                    }
                    return obj[0];
                })
        );
    }
}
