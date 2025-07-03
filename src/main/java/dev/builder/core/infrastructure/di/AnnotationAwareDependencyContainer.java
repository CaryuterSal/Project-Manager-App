package dev.builder.core.infrastructure.di;

import dev.builder.core.infrastructure.di.definition.context.BeanScope;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class AnnotationAwareDependencyContainer extends AbstractDependencyContainer{

    private static volatile AnnotationAwareDependencyContainer INSTANCE;

    private AnnotationAwareDependencyContainer() {}
    public static AnnotationAwareDependencyContainer getInstance(){
        if(INSTANCE == null){
            synchronized (AnnotationAwareDependencyContainer.class){
                if(INSTANCE == null){
                    INSTANCE = new AnnotationAwareDependencyContainer();
                }
            }
        }
        return INSTANCE;
    }
}
