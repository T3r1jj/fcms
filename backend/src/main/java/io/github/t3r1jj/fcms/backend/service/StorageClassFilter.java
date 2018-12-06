package io.github.t3r1jj.fcms.backend.service;

import io.github.t3r1jj.storapi.Storage;
import org.jetbrains.annotations.Nullable;

class StorageClassFilter<I extends Storage> {
    private final Class<I> includeClass;
    private final Class excludeClass;

    StorageClassFilter(Class<I> includeClass, @Nullable Class<?> excludeClass) {
        this.includeClass = includeClass;
        if (excludeClass == null) {
            this.excludeClass = Void.class;
        } else {
            this.excludeClass = excludeClass;
        }
    }

    StorageClassFilter(Class<I> includeClass) {
        this.includeClass = includeClass;
        this.excludeClass = Void.class;
    }

    Class<I> getIncludeClass() {
        return includeClass;
    }

    Class getExcludeClass() {
        return excludeClass;
    }
}