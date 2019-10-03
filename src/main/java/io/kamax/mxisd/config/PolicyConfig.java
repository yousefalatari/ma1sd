package io.kamax.mxisd.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class PolicyConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyConfig.class);

    public static class PolicyObject {

        private String name;

        private String version;

        private Map<String, String> urls;

        private boolean required = true;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public Map<String, String> getUrls() {
            return urls;
        }

        public void setUrls(Map<String, String> urls) {
            this.urls = urls;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }
    }

    private Map<String, PolicyObject> policies = new HashMap<>();

    public Map<String, PolicyObject> getPolicies() {
        return policies;
    }

    public void setPolicies(Map<String, PolicyObject> policies) {
        this.policies = policies;
    }

    public void build() {
        LOGGER.info("--- Policy Config ---");
        if (getPolicies().isEmpty()) {
            LOGGER.info("Empty");
        } else {
            for (Map.Entry<String, PolicyObject> policyObjectEntry : getPolicies().entrySet()) {
                PolicyObject policyObject = policyObjectEntry.getValue();
                StringBuilder sb = new StringBuilder();
                sb.append("Policy \"").append(policyObjectEntry.getKey()).append("\"\n");
                sb.append("  version: ").append(policyObject.getVersion()).append("\n");
                sb.append("  required: ").append(policyObject.isRequired()).append("\n");
                sb.append("  urls:\n");
                for (Map.Entry<String, String> urlEntry : policyObject.getUrls().entrySet()) {
                    sb.append("    lang: ").append(urlEntry.getKey()).append("\n");
                    sb.append("    url: ").append(urlEntry.getValue());
                }
                LOGGER.info(sb.toString());
            }
        }
    }
}
