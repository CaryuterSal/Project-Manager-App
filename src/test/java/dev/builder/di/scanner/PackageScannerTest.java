package dev.builder.di.scanner;

import dev.builder.core.infrastructure.di.exception.PackageNotFoundException;
import dev.builder.core.infrastructure.di.runtime.AnnotationAwareDependencyContainer;
import dev.builder.core.infrastructure.di.runtime.BeanPackageScanner;
import dev.builder.core.infrastructure.di.runtime.DependencyContainer;
import dev.builder.di.scanner.lazy.EagerClass;
import dev.builder.di.scanner.lazy.LazyClass;
import dev.builder.di.scanner.mock.inner.InnerSingleton;
import dev.builder.di.scanner.mock.inner.moreinner.MoreInnerBean;
import dev.builder.di.scanner.mock.MockBean;
import dev.builder.di.scanner.mock.MockSingleton;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class PackageScannerTest {


    private static final DependencyContainer container = AnnotationAwareDependencyContainer.getInstance();

    @BeforeEach
    public void setup(){
        container.clear();
    }

    @Test
    void test_scan_not_existent_package_fails(){
        assertThrowsExactly(PackageNotFoundException.class, () -> new BeanPackageScanner("dev.builder.not.existent.mock"));
    }

    @Test
    void test_scan(){
        Set<Class<?>> scannedBeans = new BeanPackageScanner("dev.builder.di.scanner.mock").scan();
        assertThat(scannedBeans).containsExactlyInAnyOrder(MockBean.class, MockSingleton.class, InnerSingleton.class, InnerSingleton.InnerInnerSingleton.class, MoreInnerBean.class);
    }

    @Test
    void test_scan_setter_injection_and_post_construct(){
        container.scanPackage("dev.builder.di.scanner.lazy");
        EagerClass eagerClass = container.getInstance(EagerClass.class);
        LazyClass lazyClass = container.getInstance(LazyClass.class);
        assertNotNull(eagerClass);
        assertNotNull(lazyClass);
        assertThat(eagerClass.lazyClass()).isNotNull();
        assertThat(lazyClass.eagerClass()).isNotNull();
        assertThat(lazyClass.receivedMessage()).isEqualTo("Secret Message");
    }
}
