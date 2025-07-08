package dev.builder.core.infrastructure.di.exception;

public class PackageNotFoundException extends RuntimeException {
    public PackageNotFoundException(String packageName) {
        super("package with name '" + packageName + "' not found");
    }
}
