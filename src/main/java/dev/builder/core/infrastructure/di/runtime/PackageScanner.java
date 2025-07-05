package dev.builder.core.infrastructure.di.runtime;

import dev.builder.core.infrastructure.di.exception.PackageNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.stream.Collectors;

public class PackageScanner {

    private final String packageName;

    public PackageScanner(String packageName) {
        this.packageName = packageName;
    }

    public Set<Class<?>> scan() {
        Set<Class<?>> packageClasses;
        try(InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream(packageName.replaceAll("\\.", "/"))){
            if(stream == null) throw new IOException("Cannot find resource " + packageName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            packageClasses = reader.lines()
                    .filter(line -> line.endsWith(".class"))
                    .map(className -> getPackageClass(className, packageName)).collect(Collectors.toSet());
        } catch (IOException e){
            throw new PackageNotFoundException(e.getMessage());
        }
        return packageClasses;
    }

    private @Nullable  Class<?> getPackageClass(@NotNull String className, String packageName){
        try {
            return Class.forName(packageName + "."
                    + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException ignored) {
        }
        return null;
    }
}
