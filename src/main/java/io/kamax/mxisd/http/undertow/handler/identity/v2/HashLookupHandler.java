/*
 * mxisd - Matrix Identity Server Daemon
 * Copyright (C) 2018 Kamax Sarl
 *
 * https://www.kamax.io/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.kamax.mxisd.http.undertow.handler.identity.v2;

import io.kamax.mxisd.config.HashingConfig;
import io.kamax.mxisd.exception.InvalidParamException;
import io.kamax.mxisd.exception.InvalidPepperException;
import io.kamax.mxisd.hash.HashManager;
import io.kamax.mxisd.http.IsAPIv2;
import io.kamax.mxisd.http.io.identity.ClientHashLookupAnswer;
import io.kamax.mxisd.http.io.identity.ClientHashLookupRequest;
import io.kamax.mxisd.http.undertow.handler.ApiHandler;
import io.kamax.mxisd.http.undertow.handler.identity.share.LookupHandler;
import io.kamax.mxisd.lookup.BulkLookupRequest;
import io.kamax.mxisd.lookup.HashLookupRequest;
import io.kamax.mxisd.lookup.ThreePidMapping;
import io.kamax.mxisd.lookup.strategy.LookupStrategy;
import io.undertow.server.HttpServerExchange;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class HashLookupHandler extends LookupHandler implements ApiHandler {

    public static final String Path = IsAPIv2.Base + "/lookup";

    private static final Logger log = LoggerFactory.getLogger(HashLookupHandler.class);

    private LookupStrategy strategy;
    private HashManager hashManager;

    public HashLookupHandler(LookupStrategy strategy, HashManager hashManager) {
        this.strategy = strategy;
        this.hashManager = hashManager;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        ClientHashLookupRequest input = parseJsonTo(exchange, ClientHashLookupRequest.class);
        HashLookupRequest lookupRequest = new HashLookupRequest();
        setRequesterInfo(lookupRequest, exchange);
        log.info("Got bulk lookup request from {} with client {} - Is recursive? {}",
            lookupRequest.getRequester(), lookupRequest.getUserAgent(), lookupRequest.isRecursive());

        if (!hashManager.getConfig().isEnabled()) {
            throw new InvalidParamException();
        }

        if (!hashManager.getHashEngine().getPepper().equals(input.getPepper())) {
            throw new InvalidPepperException();
        }

        switch (input.getAlgorithm()) {
            case "none":
                noneAlgorithm(exchange, lookupRequest, input);
                break;
            case "sha256":
                sha256Algorithm(exchange, lookupRequest, input);
                break;
            default:
                throw new InvalidParamException();
        }
    }

    private void noneAlgorithm(HttpServerExchange exchange, HashLookupRequest request, ClientHashLookupRequest input) throws Exception {
        if (!hashManager.getConfig().getAlgorithms().contains(HashingConfig.Algorithm.NONE)) {
            throw new InvalidParamException();
        }

        BulkLookupRequest bulkLookupRequest = new BulkLookupRequest();
        List<ThreePidMapping> mappings = new ArrayList<>();
        for (String address : input.getAddresses()) {
            String[] parts = address.split(" ");
            ThreePidMapping mapping = new ThreePidMapping();
            mapping.setMedium(parts[0]);
            mapping.setValue(parts[1]);
            mappings.add(mapping);
        }
        bulkLookupRequest.setMappings(mappings);

        ClientHashLookupAnswer answer = new ClientHashLookupAnswer();

        for (ThreePidMapping mapping : strategy.find(bulkLookupRequest).get()) {
            answer.getMappings().put(mapping.getMedium() + " " + mapping.getValue(), mapping.getMxid());
        }
        log.info("Finished bulk lookup request from {}", request.getRequester());

        respondJson(exchange, answer);
    }

    private void sha256Algorithm(HttpServerExchange exchange, HashLookupRequest request, ClientHashLookupRequest input) {
        if (!hashManager.getConfig().getAlgorithms().contains(HashingConfig.Algorithm.SHA256)) {
            throw new InvalidParamException();
        }

        ClientHashLookupAnswer answer = new ClientHashLookupAnswer();
        for (Pair<String, ThreePidMapping> pair : hashManager.getHashStorage().find(request.getHashes())) {
            answer.getMappings().put(pair.getKey(), pair.getValue().getMxid());
        }
        log.info("Finished bulk lookup request from {}", request.getRequester());

        respondJson(exchange, answer);
    }

    @Override
    public String getHandlerPath() {
        return "/bulk_lookup";
    }
}
