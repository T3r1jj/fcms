package io.github.t3r1jj.fcms.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.PersistenceConstructor;

import java.util.Arrays;
import java.util.Objects;

public class ExternalService {
    private final String name;
    private final boolean primary;
    private final boolean enabled;
    private final ApiKey[] apiKeys;

    @PersistenceConstructor
    public ExternalService(@JsonProperty("name") String name,
                           @JsonProperty("primary") boolean primary,
                           @JsonProperty("enabled") boolean enabled,
                           @JsonProperty("apiKeys") ApiKey... apiKeys) {
        this.primary = primary;
        this.name = name;
        this.apiKeys = apiKeys;
        this.enabled = enabled;
    }

    /**
     * @return the primary
     */
    public boolean isPrimary() {
        return primary;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the apiKey
     */
    public ApiKey[] getApiKeys() {
        return apiKeys;
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public final boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof ExternalService)) {
            return false;
        }
        ExternalService service = (ExternalService) o;
        return Objects.equals(name, service.name) &&
                Arrays.equals(apiKeys, service.apiKeys) &&
                Objects.equals(primary, service.primary) &&
                Objects.equals(enabled, service.enabled);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(name, primary) + Arrays.hashCode(apiKeys);
    }

    public static class ApiKey {
        private final String label;
        private final String value;

        @PersistenceConstructor
        public ApiKey(@JsonProperty("label") String label, @JsonProperty("value") String value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ApiKey apiKey = (ApiKey) o;
            return Objects.equals(label, apiKey.label) && Objects.equals(value, apiKey.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(label);
        }
    }
}