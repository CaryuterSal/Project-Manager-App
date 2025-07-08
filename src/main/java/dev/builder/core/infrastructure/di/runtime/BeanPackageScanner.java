package dev.builder.core.infrastructure.di.runtime;

import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Singleton;
import dev.builder.core.infrastructure.di.exception.PackageNotFoundException;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.util.HashSet;
import java.util.Set;

public class BeanPackageScanner {

    private final String packageName;

    public BeanPackageScanner(String packageName) {
        this.packageName = packageName;
        if( new ConfigurationBuilder().forPackages(packageName).addScanners(Scanners.SubTypes).getUrls().isEmpty()) {
            throw new PackageNotFoundException(packageName);
        }
    }

    public Set<Class<?>> scan() {
        Set<Class<?>> packageClasses = new HashSet<>();
        Reflections reflections = new Reflections(packageName, Scanners.TypesAnnotated);
        packageClasses.addAll(reflections.getTypesAnnotatedWith(Bean.class));
        packageClasses.addAll(reflections.getTypesAnnotatedWith(Singleton.class));
        return packageClasses;
    }
}
