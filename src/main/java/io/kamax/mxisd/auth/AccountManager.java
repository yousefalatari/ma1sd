package io.kamax.mxisd.auth;

import com.google.gson.JsonObject;
import io.kamax.matrix.json.GsonUtil;
import io.kamax.mxisd.exception.InvalidCredentialsException;
import io.kamax.mxisd.exception.NotFoundException;
import io.kamax.mxisd.matrix.HomeserverFederationResolver;
import io.kamax.mxisd.storage.IStorage;
import io.kamax.mxisd.storage.ormlite.dao.AccountDao;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class AccountManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountManager.class);

    private final IStorage storage;
    private final HomeserverFederationResolver resolver;
    private final CloseableHttpClient httpClient;

    public AccountManager(IStorage storage, HomeserverFederationResolver resolver,
                          CloseableHttpClient httpClient) {
        this.storage = storage;
        this.resolver = resolver;
        this.httpClient = httpClient;
    }

    public String register(OpenIdToken openIdToken) {
        Objects.requireNonNull(openIdToken.getAccessToken(), "Missing required access_token");
        Objects.requireNonNull(openIdToken.getTokenType(), "Missing required token type");
        Objects.requireNonNull(openIdToken.getMatrixServerName(), "Missing required matrix domain");

        String homeserverURL = resolver.resolve(openIdToken.getMatrixServerName()).toString();
        HttpGet getUserInfo = new HttpGet(
            "https://" + homeserverURL + "/_matrix/federation/v1/openid/userinfo?access_token=" + openIdToken.getAccessToken());
        String userId;
        try (CloseableHttpResponse response = httpClient.execute(getUserInfo)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                JsonObject body = GsonUtil.parseObj(EntityUtils.toString(response.getEntity()));
                userId = GsonUtil.getStringOrThrow(body, "sub");
            } else {
                LOGGER.error("Wrong response status: {}", statusCode);
                throw new InvalidCredentialsException();
            }
        } catch (IOException e) {
            LOGGER.error("Unable to get user info.");
            throw new InvalidCredentialsException();
        }

        String token = UUID.randomUUID().toString();
        AccountDao account = new AccountDao(openIdToken.getAccessToken(), openIdToken.getTokenType(),
            openIdToken.getMatrixServerName(), openIdToken.getExpiredIn(),
            Instant.now().getEpochSecond(), userId, token);
        storage.insertToken(account);
        return token;
    }

    public String getUserId(String token) {
        return storage.findUserId(token).orElseThrow(NotFoundException::new);
    }

    public void logout(String token) {
        String userId = storage.findUserId(token).orElseThrow(InvalidCredentialsException::new);
        LOGGER.info("Logout: {}", userId);
        storage.deleteToken(token);
    }
}
