package io.github.t3r1jj.fcms.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import static io.github.t3r1jj.fcms.backend.Utils.notIf;

@Document
public class Configuration {
    public static String getDefaultId() {
        return "DEFAULT";
    }

    @Id
    private final String id = getDefaultId();
    private ExternalService[] services;
    private int primaryBackupLimit = 1;
    private int secondaryBackupLimit = 0;

    public Configuration(@JsonProperty("services") ExternalService[] services) {
        this.services = services;
    }

    public ExternalService[] getServices() {
        return this.services;
    }

    public void setServices(ExternalService[] services) {
        this.services = services;
    }

    public Configuration withServices(ExternalService[] apiKeys) {
        this.services = apiKeys;
        return this;
    }

    public int getPrimaryBackupLimit() {
        return primaryBackupLimit;
    }

    public void setPrimaryBackupLimit(int primaryBackupLimit) {
        this.primaryBackupLimit = primaryBackupLimit;
    }

    public int getSecondaryBackupLimit() {
        return secondaryBackupLimit;
    }

    public void setSecondaryBackupLimit(int secondaryBackupLimit) {
        this.secondaryBackupLimit = secondaryBackupLimit;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Configuration)) {
            return false;
        }
        Configuration configuration = (Configuration) o;
        return Arrays.equals(services, configuration.services);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(services);
    }

    @Override
    public String toString() {
        return "{" + " services='" + Arrays.toString(getServices()) + "'" + "}";
    }

    public void merge(Configuration configuration) {
        ExternalService[] validServices = this.getServices();
        ExternalService[] services = configuration.getServices();
        for (int i = 0; i < validServices.length; i++) {
            for (ExternalService service : services) {
                if (validServices[i].hashCode() == service.hashCode()) {
                    validServices[i] = service;
                }
            }
        }
        this.setPrimaryBackupLimit(configuration.getPrimaryBackupLimit());
        this.setSecondaryBackupLimit(configuration.getSecondaryBackupLimit());
    }

    @JsonIgnore
    public Stream<ExternalService> getEnabledServicesStream(boolean primary) {
        return Stream.of(services)
                .filter(ExternalService::isEnabled)
                .filter(notIf(ExternalService::isPrimary, !primary));
    }

    public Stream<ExternalService> stream() {
        return Stream.of(services);
    }

}