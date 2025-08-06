package dev.builder.core.infrastructure.di.definition;

public interface OptionalConfigStep<T> extends BuildStep<T> {
    BeanNameStep<T> initCustomizer(InitCustomizer<T> initCustomizer);
    InitCustomizerStep<T> withName(String name);
}