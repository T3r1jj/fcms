package io.github.t3r1jj.fcms.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.PersistenceConstructor;

import java.util.Objects;

public class ExternalService {
    private String name;
    private boolean primary;
    private String apiKey;
    private boolean enabled;

    public ExternalService(String name, boolean primary) {
        this(name, primary, "", false);
    }

    @PersistenceConstructor
    public ExternalService(@JsonProperty("name") String name,
                           @JsonProperty("primary") boolean primary,
                           @JsonProperty("apiKey") String apiKey,
                           @JsonProperty("enabled") boolean enabled) {
        this.primary = primary;
        this.name = name;
        this.apiKey = apiKey;
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
    public String getApiKey() {
        return apiKey;
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
                Objects.equals(apiKey, service.apiKey) &&
                Objects.equals(primary, service.primary) &&
                Objects.equals(enabled, service.enabled);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name) + Objects.hashCode(primary);
    }

}