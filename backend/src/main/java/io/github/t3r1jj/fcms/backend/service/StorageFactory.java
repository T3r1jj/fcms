package io.github.t3r1jj.fcms.backend.service;

import io.github.t3r1jj.fcms.backend.model.Configuration;
import io.github.t3r1jj.fcms.backend.model.ExternalService;
import io.github.t3r1jj.fcms.external.Storage;
import io.github.t3r1jj.fcms.external.authenticated.AuthenticatedStorage;
import io.github.t3r1jj.fcms.external.upstream.CleanableStorage;
import io.github.t3r1jj.fcms.external.upstream.UpstreamStorage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.clapper.util.classutil.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class StorageFactory {
    private static Log logger = LogFactory.getLog(StorageFactory.class);
    private Configuration configuration;

    StorageFactory() {
        this.configuration = createEmptyExternalConfiguration();
    }

    private Configuration createEmptyExternalConfiguration() {
        ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();
        Collection<Class<UpstreamStorage>> foundClasses = findStorageClasses(UpstreamStorage.class);
        ExternalService[] externalServices = foundClasses.stream().map(aClass -> {
            Constructor constructor = getDefaultOrMaxConstructor(aClass);
            String[] parameterNames = Objects.requireNonNull(nameDiscoverer.getParameterNames(constructor));
            ExternalService.ApiKey[] apiKeys = Stream.of(parameterNames)
                    .map(p -> new ExternalService.ApiKey(p, ""))
                    .toArray(ExternalService.ApiKey[]::new);
            return new ExternalService(aClass.getSimpleName(), AuthenticatedStorage.class.isAssignableFrom(aClass), false, apiKeys);
        }).toArray(ExternalService[]::new);
        return new Configuration(externalServices);
    }

    @NotNull
    private <T> Class<T> storageClassForName(String name) {
        try {
            //noinspection unchecked
            return (Class<T>) Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private <T extends Storage> Collection<Class<T>> findStorageClasses(Class<T> storageBaseInterface) {
        ClassFinder finder = new ClassFinder();
        finder.addClassPath();
        ClassFilter filter =
                new AndClassFilter(
                        new NotClassFilter(new InterfaceOnlyClassFilter()),
                        new SubclassClassFilter(storageBaseInterface),
                        new NotClassFilter(new AbstractClassFilter()));
        Collection<ClassInfo> foundClasses = new ArrayList<>();
        finder.findClasses(foundClasses, filter);
        return foundClasses.stream()
                .map(c -> this.<T>storageClassForName(c.getClassName()))
                .collect(Collectors.toList());
    }

    @NotNull
    private Constructor getDefaultOrMaxConstructor(Class aClass) {
        Constructor[] constructors = aClass.getConstructors();
        return Stream.of(constructors)
                .min(Comparator.comparingInt(Constructor::getParameterCount))
                .orElseThrow(() -> new RuntimeException(
                        String.format("Cannot find a default constructor for %s or another one with minimal param count to instantiate it.",
                                aClass.getName())));
    }

    StorageFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * @param primaryService name of primary service
     * @return AuthenticatedStorage which requires login
     */
    @NotNull
    AuthenticatedStorage createAuthenticatedStorage(String primaryService) {
        return instantiate(primaryService, AuthenticatedStorage.class);
    }

    @NotNull
    private <T extends Storage> T instantiate(String externalService, Class<T> storageBaseInterface) {
        ExternalService service = Stream.of(configuration.getServices())
                .filter(s -> s.getName().equals(externalService))
                .findAny()
                .orElseThrow(() -> new RuntimeException(String.format("%s service not found", externalService)));
        Class<T> storageClass = findStorageClasses(storageBaseInterface).stream()
                .filter(s -> s.getSimpleName().equals(externalService))
                .findAny()
                .orElseThrow(() -> new RuntimeException(String.format("%s service class not found", externalService)));
        //noinspection unchecked
        Constructor<T> constructor = Stream.of((Constructor<T>[]) storageClass.getConstructors())
                .filter(c -> c.getParameterCount() == service.getApiKeys().length)
                .findAny()
                .orElseThrow(() -> new RuntimeException(String.format("%s constructor with %d args not found", externalService, service.getApiKeys().length)));
        Object[] params = Stream.of(service.getApiKeys()).map(ExternalService.ApiKey::getKey).toArray();
        try {
            return constructor.newInstance(params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param externalService any service
     * @return optional service which may require authentication by throwing StorageException on delete with AuthenticatedStorage getter
     */
    @NotNull
    Optional<CleanableStorage> createCleanableStorage(String externalService) {
        try {
            return Optional.of(instantiate(externalService, CleanableStorage.class));
        } catch (RuntimeException re) {
            logger.debug(String.format("CleanableStorage not found for %s", externalService), re);
            return Optional.empty();
        }
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
