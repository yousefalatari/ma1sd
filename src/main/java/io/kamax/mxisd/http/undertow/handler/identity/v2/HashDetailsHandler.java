package io.kamax.mxisd.http.undertow.handler.identity.v2;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.kamax.mxisd.hash.HashManager;
import io.kamax.mxisd.http.undertow.handler.BasicHttpHandler;
import io.undertow.server.HttpServerExchange;

public class HashDetailsHandler extends BasicHttpHandler {

    public static final String PATH = "/_matrix/identity/v2/hash_details";

    private final HashManager hashManager;
    private volatile JsonObject response = null;

    public HashDetailsHandler(HashManager hashManager) {
        this.hashManager = hashManager;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        respond(exchange, getResponse());
    }

    private JsonObject getResponse() {
        if (response == null) {
            synchronized (this) {
                if (response == null) {
                    response = new JsonObject();
                    response.addProperty("lookup_pepper", hashManager.getHashEngine().getPepper());
                    JsonArray algorithms = new JsonArray();
                    algorithms.add("none");
                    if (hashManager.getConfig().isEnabled()) {
                        algorithms.add("sha256");
                    }
                    response.add("algorithms", algorithms);
                }
            }
        }
        return response;
    }
}
