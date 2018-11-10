package io.github.t3r1jj.fcms.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.PersistenceConstructor;

import java.util.Arrays;
import java.util.Objects;

public class ExternalService {
    private String name;
    private boolean primary;
    private boolean enabled;
    private ApiKey[] apiKeys;

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
    public boolean equals(Object o) {
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
    public String toString() {
        return "ExternalService{" +
                "name='" + name + '\'' +
                ", primary=" + primary +
                ", enabled=" + enabled +
                ", apiKeys=" + Arrays.toString(apiKeys) +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name) + Arrays.hashCode(apiKeys);
    }

    public static class ApiKey {
        private final String label;
        private final String key;

        @PersistenceConstructor
        public ApiKey(@JsonProperty("label") String label, @JsonProperty("key") String key) {
            this.label = label;
            this.key = key;
        }

        public String getLabel() {
            return label;
        }

        public String getKey() {
            return key;
        }

        @Override
        public String toString() {
            return "ApiKey{" +
                    "label='" + label + '\'' +
                    ", key='" + (key != null && !key.isEmpty()) + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ApiKey apiKey = (ApiKey) o;
            return Objects.equals(label, apiKey.label) && Objects.equals(key, apiKey.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(label);
        }
    }
}