package dev.builder.di.annotation;

import dev.builder.core.infrastructure.di.annotation.Bean;

@Bean
public class BeanAnnotationClass {

    @Bean(name = "withNameAnnoBean")
    public static class BeanAnnotationClassWithName{

    }

    @Bean(singleton = false)
    public static class BeanAnnotationClassAsPrototype{

    }
}
