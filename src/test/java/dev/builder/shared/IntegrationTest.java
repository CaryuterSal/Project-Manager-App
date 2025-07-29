package dev.builder.shared;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Tag("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public @interface IntegrationTest {
}
