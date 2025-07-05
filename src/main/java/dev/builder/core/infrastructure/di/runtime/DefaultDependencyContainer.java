package dev.builder.core.infrastructure.di.runtime;

public class DefaultDependencyContainer extends AbstractDependencyContainer {
    @Override
    public void scanPackage(String packageName) throws UnsupportedOperationException{
        throw new UnsupportedOperationException("Cannot scan package for " + getClass().getSimpleName());
    }
}
