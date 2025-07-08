package dev.builder.core.infrastructure.di.runtime;

import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Singleton;
import dev.builder.core.infrastructure.di.exception.PackageNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

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
