package dev.builder.di.annotation;

import dev.builder.core.infrastructure.di.annotation.Inject;

public class StaticFieldInjection {

    @Inject
    static FooClass fooClass;
}
