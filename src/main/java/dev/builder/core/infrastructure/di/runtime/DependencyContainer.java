package dev.builder.core.infrastructure.di.runtime;

import dev.builder.core.infrastructure.di.definition.BeanMetadataAccessor;

public interface DependencyContainer extends
        BeanRegistrar,
        BeanRetriever,
        BeanMetadataAccessor,
        ContainerLifecycleManager {

}
