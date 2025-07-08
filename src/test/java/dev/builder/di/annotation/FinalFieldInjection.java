package dev.builder.di.annotation;

import dev.builder.core.infrastructure.di.annotation.Inject;

public class FinalFieldInjection {

    @Inject
    final FooClass fooClass =  new FooClass();
}
