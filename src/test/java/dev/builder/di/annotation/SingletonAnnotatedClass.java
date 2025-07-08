package dev.builder.di.annotation;

import dev.builder.core.infrastructure.di.annotation.Singleton;
import dev.builder.core.infrastructure.di.definition.InstantiationMode;

@Singleton(name = "singletonBean")
public class SingletonAnnotatedClass {

    @Singleton(name = "lazySingletonBean", mode = InstantiationMode.LAZY)
    public static class LazySingletonClass{
    }

    @Singleton(mode = InstantiationMode.EAGER)
    public static class EagerSingletonClass {

    }
}
