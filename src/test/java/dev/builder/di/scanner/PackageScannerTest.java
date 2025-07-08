package dev.builder.di.scanner;

import dev.builder.core.infrastructure.di.exception.PackageNotFoundException;
import dev.builder.core.infrastructure.di.runtime.BeanPackageScanner;
import dev.builder.di.scanner.mock.inner.InnerSingleton;
import dev.builder.di.scanner.mock.inner.moreinner.MoreInnerBean;
import dev.builder.di.scanner.mock.MockBean;
import dev.builder.di.scanner.mock.MockSingleton;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

public class PackageScannerTest {

    @Test
    void test_scan_not_existent_package_fails(){
        assertThrowsExactly(PackageNotFoundException.class, () -> new BeanPackageScanner("dev.builder.not.existent.mock"));
    }

    @Test
    void test_scan(){
        Set<Class<?>> scannedBeans = new BeanPackageScanner("dev.builder.di.scanner.mock").scan();
        assertThat(scannedBeans).containsExactlyInAnyOrder(MockBean.class, MockSingleton.class, InnerSingleton.class, InnerSingleton.InnerInnerSingleton.class, MoreInnerBean.class);
    }
}
