package io.github.t3r1jj.fcms.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.t3r1jj.fcms.backend.controller.RecordController;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

@Document
public class Configuration {
    public static String getDefaultId() {
        return "DEFAULT";
    }

    @Id
    private final String id = getDefaultId();
    private ExternalService[] services;
    private int primaryBackupCount = 1;
    private int secondaryBackupCount = 0;

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

    public int getPrimaryBackupCount() {
        return primaryBackupCount;
    }

    public void setPrimaryBackupCount(int primaryBackupCount) {
        this.primaryBackupCount = primaryBackupCount;
    }

    public int getSecondaryBackupCount() {
        return secondaryBackupCount;
    }

    public void setSecondaryBackupCount(int secondaryBackupCount) {
        this.secondaryBackupCount = secondaryBackupCount;
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

}