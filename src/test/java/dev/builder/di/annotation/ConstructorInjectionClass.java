package dev.builder.di.annotation;

import dev.builder.core.infrastructure.di.annotation.Inject;

public class ConstructorInjectionClass {
    private final CommonI common;
    private final FooClass fooClass;

    ConstructorInjectionClass(){
        this.common = null;
        this.fooClass = null;
    }

    @Inject
    ConstructorInjectionClass(CommonI common){
        this.common = common;
        this.fooClass = null;
    }

    ConstructorInjectionClass(CommonI common, FooClass fooClass){
        this.common = common;
        this.fooClass = fooClass;
    }

    public CommonI common() {
        return common;
    }

    public FooClass fooClass() {
        return fooClass;
    }
}
