package io.kamax.mxisd.auth;

import com.google.gson.JsonObject;
import io.kamax.matrix.MatrixID;
import io.kamax.matrix.json.GsonUtil;
import io.kamax.mxisd.config.AccountConfig;
import io.kamax.mxisd.config.MatrixConfig;
import io.kamax.mxisd.exception.BadRequestException;
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
    private final AccountConfig accountConfig;
    private final MatrixConfig matrixConfig;

    public AccountManager(IStorage storage, HomeserverFederationResolver resolver,
                          CloseableHttpClient httpClient, AccountConfig accountConfig, MatrixConfig matrixConfig) {
        this.storage = storage;
        this.resolver = resolver;
        this.httpClient = httpClient;
        this.accountConfig = accountConfig;
        this.matrixConfig = matrixConfig;
    }

    public String register(OpenIdToken openIdToken) {
        Objects.requireNonNull(openIdToken.getAccessToken(), "Missing required access_token");
        Objects.requireNonNull(openIdToken.getTokenType(), "Missing required token type");
        Objects.requireNonNull(openIdToken.getMatrixServerName(), "Missing required matrix domain");

        String userId = getUserId(openIdToken);

        String token = UUID.randomUUID().toString();
        AccountDao account = new AccountDao(openIdToken.getAccessToken(), openIdToken.getTokenType(),
            openIdToken.getMatrixServerName(), openIdToken.getExpiredIn(),
            Instant.now().getEpochSecond(), userId, token);
        storage.insertToken(account);

        LOGGER.info("User {} registered", userId);

        return token;
    }

    private String getUserId(OpenIdToken openIdToken) {
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

        checkMXID(userId);
        return userId;
    }

    private void checkMXID(String userId) {
        MatrixID mxid;
        try {
            mxid = MatrixID.asValid(userId);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Wrong MXID: " + userId, e);
            throw new BadRequestException("Wrong MXID");
        }

        if (getAccountConfig().isAllowOnlyTrustDomains()) {
            LOGGER.info("Allow registration only for trust domain.");
            if (getMatrixConfig().getDomain().equals(mxid.getDomain())) {
                LOGGER.info("Allow user {} to registration", userId);
            } else {
                LOGGER.error("Deny user {} to registration", userId);
                throw new InvalidCredentialsException();
            }
        } else {
            LOGGER.info("Allow registration from any server.");
        }
    }

    public String getUserId(String token) {
        return storage.findUserId(token).orElseThrow(NotFoundException::new);
    }

    public void logout(String token) {
        String userId = storage.findUserId(token).orElseThrow(InvalidCredentialsException::new);
        LOGGER.info("Logout: {}", userId);
        storage.deleteToken(token);
    }

    public AccountConfig getAccountConfig() {
        return accountConfig;
    }

    public MatrixConfig getMatrixConfig() {
        return matrixConfig;
    }
}
