package dev.builder.core.infrastructure.di.runtime;

public interface DependencyContainer extends
        BeanRegistrar,
        BeanRetriever,
        BeanMetadataAccessor,
        ContainerLifecycleManager {

}
