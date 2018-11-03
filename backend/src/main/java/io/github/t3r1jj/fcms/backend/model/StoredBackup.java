package io.github.t3r1jj.fcms.backend.model;

import java.util.Optional;

public class StoredBackup {
    public final String externalService;
    private String privatePath;
    private String publicPath;

    public StoredBackup(String externalService, String privatePath, String publicPath) {
        this.externalService = externalService;
        this.privatePath = privatePath;
        this.publicPath = publicPath;
    }

    public Optional<String> getPrivatePath() {
        return Optional.of(privatePath);
    }

    public void setPrivatePath(String privatePath) {
        this.privatePath = privatePath;
    }

    public Optional<String> getPublicPath() {
        return Optional.of(publicPath);
    }

    public void setPublicPath(String publicPath) {
        this.publicPath = publicPath;
    }

    public StoredBackup withPublicPath(String publicPath) {
        setPublicPath(publicPath);
        return this;
    }

    public StoredBackup withPrivatePath(String privatePath) {
        setPrivatePath(privatePath);
        return this;
    }

}
