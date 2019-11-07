package io.kamax.mxisd.auth;

public class OpenIdToken {

    private String accessToken;

    private String tokenType;

    private String matrixServerName;

    private long expiredIn;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getMatrixServerName() {
        return matrixServerName;
    }

    public void setMatrixServerName(String matrixServerName) {
        this.matrixServerName = matrixServerName;
    }

    public long getExpiredIn() {
        return expiredIn;
    }

    public void setExpiredIn(long expiredIn) {
        this.expiredIn = expiredIn;
    }
}
