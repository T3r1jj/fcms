package io.github.t3r1jj.fcms.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class Configuration {
    private ExternalService[] apiKeys;

    public Configuration(@JsonProperty("apiKeys") ExternalService[] apiKeys) {
        this.apiKeys = apiKeys;
    }

    public ExternalService[] getApiKeys() {
        return this.apiKeys;
    }

    public void setApiKeys(ExternalService[] apiKeys) {
        this.apiKeys = apiKeys;
    }

    public Configuration apiKeys(ExternalService[] apiKeys) {
        this.apiKeys = apiKeys;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Configuration)) {
            return false;
        }
        Configuration configuration = (Configuration) o;
        return Objects.equals(apiKeys, configuration.apiKeys);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(apiKeys);
    }

    @Override
    public String toString() {
        return "{" + " apiKeys='" + getApiKeys() + "'" + "}";
    }

}