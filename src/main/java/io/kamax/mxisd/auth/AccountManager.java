package io.kamax.mxisd.auth;

import com.google.gson.JsonObject;
import io.kamax.matrix.MatrixID;
import io.kamax.matrix.json.GsonUtil;
import io.kamax.mxisd.config.AccountConfig;
import io.kamax.mxisd.config.MatrixConfig;
import io.kamax.mxisd.config.PolicyConfig;
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
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

public class AccountManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountManager.class);

    private final IStorage storage;
    private final HomeserverFederationResolver resolver;
    private final AccountConfig accountConfig;
    private final MatrixConfig matrixConfig;

    public AccountManager(IStorage storage, HomeserverFederationResolver resolver, AccountConfig accountConfig, MatrixConfig matrixConfig) {
        this.storage = storage;
        this.resolver = resolver;
        this.accountConfig = accountConfig;
        this.matrixConfig = matrixConfig;
    }

    public String register(OpenIdToken openIdToken) {
        Objects.requireNonNull(openIdToken.getAccessToken(), "Missing required access_token");
        Objects.requireNonNull(openIdToken.getTokenType(), "Missing required token type");
        Objects.requireNonNull(openIdToken.getMatrixServerName(), "Missing required matrix domain");

        LOGGER.info("Registration from the server: {}", openIdToken.getMatrixServerName());
        String userId = getUserId(openIdToken);
        LOGGER.info("UserId: {}", userId);

        String token = UUID.randomUUID().toString();
        AccountDao account = new AccountDao(openIdToken.getAccessToken(), openIdToken.getTokenType(),
            openIdToken.getMatrixServerName(), openIdToken.getExpiredIn(),
            Instant.now().getEpochSecond(), userId, token);
        storage.insertToken(account);

        LOGGER.info("User {} registered", userId);

        return token;
    }

    private String getUserId(OpenIdToken openIdToken) {
        String matrixServerName = openIdToken.getMatrixServerName();
        HomeserverFederationResolver.HomeserverTarget homeserverTarget = resolver.resolve(matrixServerName);
        String homeserverURL = homeserverTarget.getUrl().toString();
        LOGGER.info("Domain resolved: {} => {}", matrixServerName, homeserverURL);
        HttpGet getUserInfo = new HttpGet(
            homeserverURL + "/_matrix/federation/v1/openid/userinfo?access_token=" + openIdToken.getAccessToken());
        String userId;
        try (CloseableHttpClient httpClient = HttpClients.custom()
            .setSSLHostnameVerifier(new MatrixHostnameVerifier(homeserverTarget.getDomain())).build()) {
            try (CloseableHttpResponse response = httpClient.execute(getUserInfo)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    String content = EntityUtils.toString(response.getEntity());
                    LOGGER.trace("Response: {}", content);
                    JsonObject body = GsonUtil.parseObj(content);
                    userId = GsonUtil.getStringOrThrow(body, "sub");
                } else {
                    LOGGER.error("Wrong response status: {}", statusCode);
                    throw new InvalidCredentialsException();
                }
            } catch (IOException e) {
                LOGGER.error("Unable to get user info.", e);
                throw new InvalidCredentialsException();
            }
        } catch (IOException e) {
            LOGGER.error("Unable to create a connection to host: " + homeserverURL, e);
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
        return storage.findAccount(token).orElseThrow(NotFoundException::new).getUserId();
    }

    public AccountDao findAccount(String token) {
        AccountDao accountDao = storage.findAccount(token).orElse(null);

        if (LOGGER.isInfoEnabled()) {
            if (accountDao != null) {
                LOGGER.info("Found account for user: {}", accountDao.getUserId());
            } else {
                LOGGER.warn("Account not found.");
            }
        }
        return accountDao;
    }

    public void logout(String token) {
        String userId = storage.findAccount(token).orElseThrow(InvalidCredentialsException::new).getUserId();
        LOGGER.info("Logout: {}", userId);
        deleteAccount(token);
    }

    public void deleteAccount(String token) {
        storage.deleteAccepts(token);
        storage.deleteToken(token);
    }

    public void acceptTerm(String token, String url) {
        storage.acceptTerm(token, url);
    }

    public boolean isTermAccepted(String token, List<PolicyConfig.PolicyObject> policies) {
        return policies.isEmpty() || storage.isTermAccepted(token, policies);
    }

    public AccountConfig getAccountConfig() {
        return accountConfig;
    }

    public MatrixConfig getMatrixConfig() {
        return matrixConfig;
    }

    public static class MatrixHostnameVerifier implements HostnameVerifier {

        private static final String ALT_DNS_NAME_TYPE = "2";
        private static final String ALT_IP_ADDRESS_TYPE = "7";

        private final String matrixHostname;

        public MatrixHostnameVerifier(String matrixHostname) {
            this.matrixHostname = matrixHostname;
        }

        @Override
        public boolean verify(String hostname, SSLSession session) {
            try {
                Certificate peerCertificate = session.getPeerCertificates()[0];
                if (peerCertificate instanceof X509Certificate) {
                    X509Certificate x509Certificate = (X509Certificate) peerCertificate;
                    if (x509Certificate.getSubjectAlternativeNames() == null) {
                        return false;
                    }
                    for (String altSubjectName : getAltSubjectNames(x509Certificate)) {
                        if (match(altSubjectName)) {
                            return true;
                        }
                    }
                }
            } catch (SSLPeerUnverifiedException | CertificateParsingException e) {
                LOGGER.error("Unable to check remote host", e);
                return false;
            }

            return false;
        }

        private List<String> getAltSubjectNames(X509Certificate x509Certificate) {
            List<String> subjectNames = new ArrayList<>();
            try {
                for (List<?> subjectAlternativeNames : x509Certificate.getSubjectAlternativeNames()) {
                    if (subjectAlternativeNames == null
                        || subjectAlternativeNames.size() < 2
                        || subjectAlternativeNames.get(0) == null
                        || subjectAlternativeNames.get(1) == null) {
                        continue;
                    }
                    String subjectType = subjectAlternativeNames.get(0).toString();
                    switch (subjectType) {
                        case ALT_DNS_NAME_TYPE:
                        case ALT_IP_ADDRESS_TYPE:
                            subjectNames.add(subjectAlternativeNames.get(1).toString());
                            break;
                        default:
                            LOGGER.trace("Unusable subject type: " + subjectType);
                    }
                }
            } catch (CertificateParsingException e) {
                LOGGER.error("Unable to parse the certificate", e);
                return Collections.emptyList();
            }
            return subjectNames;
        }

        private boolean match(String altSubjectName) {
            if (altSubjectName.startsWith("*.")) {
                return altSubjectName.toLowerCase().endsWith(matrixHostname.toLowerCase());
            } else {
                return matrixHostname.equalsIgnoreCase(altSubjectName);
            }
        }
    }
}
