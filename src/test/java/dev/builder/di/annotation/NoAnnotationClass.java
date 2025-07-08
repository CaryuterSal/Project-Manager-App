package dev.builder.di.annotation;

import dev.builder.di.DependencyContainerTest;

public class NoAnnotationClass {
    private FooClass foo;
    private SingletonAnnotatedClass dependency;

    public NoAnnotationClass(SingletonAnnotatedClass commonString) {
        this.dependency = commonString;
    }

    public SingletonAnnotatedClass dependency() {
        return dependency;
    }

    public FooClass foo() {
        return foo;
    }
}
