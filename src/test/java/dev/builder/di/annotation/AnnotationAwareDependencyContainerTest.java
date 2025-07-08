package dev.builder.di.annotation;

import dev.builder.core.infrastructure.di.definition.InstantiationMode;
import dev.builder.core.infrastructure.di.exception.BeanNotFoundException;
import dev.builder.core.infrastructure.di.exception.ConstructorNotFoundException;
import dev.builder.core.infrastructure.di.exception.UnsupportedFieldInjectionException;
import dev.builder.core.infrastructure.di.runtime.AnnotationAwareDependencyContainer;
import dev.builder.core.infrastructure.di.runtime.DependencyContainer;
import dev.builder.di.scanner.mock.MockBean;
import dev.builder.di.scanner.mock.MockSingleton;
import dev.builder.di.scanner.mock.inner.InnerSingleton;
import dev.builder.di.scanner.mock.inner.moreinner.MoreInnerBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class AnnotationAwareDependencyContainerTest {
    private static final DependencyContainer container = AnnotationAwareDependencyContainer.getInstance();

    @BeforeEach
    public void setup(){
        container.clear();
    }

    private static void assertLazy(Class<?> clazz){
        assertThat(container.getInstantiationMode(clazz)).isEqualTo(InstantiationMode.LAZY);
    }

    private static void assertEager(Class<?> clazz){
        assertThat(container.getInstantiationMode(clazz)).isEqualTo(InstantiationMode.EAGER);
    }


    @Test
    void testAnnotatedSingletonIsSingleton(){
        boolean registered = container.register(SingletonAnnotatedClass.class);
        assertTrue(registered);
        assertTrue(container.isRegistered(SingletonAnnotatedClass.class));
        assertTrue(container.isSingleton(SingletonAnnotatedClass.class));
    }

    @Test
    void testAnnotatedLazySingletonIsSingleton(){
        container.register(SingletonAnnotatedClass.LazySingletonClass.class);
        assumeTrue(container.isSingleton(SingletonAnnotatedClass.LazySingletonClass.class));
        assertLazy(SingletonAnnotatedClass.LazySingletonClass.class);
    }

    @Test
    void testAnnotatedEagerSingletonIsEager(){
        boolean registered = container.register(SingletonAnnotatedClass.EagerSingletonClass.class);
        assertTrue(registered);
        assertTrue(container.isRegistered(SingletonAnnotatedClass.EagerSingletonClass.class));
        assertTrue(container.isSingleton(SingletonAnnotatedClass.EagerSingletonClass.class));
        assertEager(SingletonAnnotatedClass.EagerSingletonClass.class);
    }

    @Test
    void testAnnotatedSingletonRetrievesSameInstance(){
        boolean registered = container.register(SingletonAnnotatedClass.class);
        assertTrue(registered);
        SingletonAnnotatedClass firstSingleton = container.getInstance(SingletonAnnotatedClass.class);
        SingletonAnnotatedClass secondSingleton = container.getInstance(SingletonAnnotatedClass.class);
        assertEquals(firstSingleton, secondSingleton);
    }

    @Test
    void testBeanRegisters(){
        boolean registered = container.register(FooClass.class);
        assertTrue(registered);
        assertTrue(container.isRegistered(FooClass.class));
    }

    @Test
    void testNoAnnotationBeanIsPrototype(){
        container.register(FooClass.class);
        assertFalse(container.isSingleton(FooClass.class));
    }

    @Test
    void testPrototypeBeanReturnsDifferentInstance(){
        container.register(FooClass.class);
        assumeFalse(container.isSingleton(FooClass.class));
        FooClass firstInstance = container.getInstance(FooClass.class);
        FooClass secondInstance = container.getInstance(FooClass.class);
        assertNotEquals(firstInstance, secondInstance);
    }

    @Test
    void testCustomSingletonNameRetrieves(){
        container.register(SingletonAnnotatedClass.class);
        assumeTrue(container.isSingleton(SingletonAnnotatedClass.class));
        container.getInstance(SingletonAnnotatedClass.class, "singletonBean");
    }

    @Test
    void testCustomSingletonNameFails(){
        container.register(SingletonAnnotatedClass.class);
        assumeTrue(container.isSingleton(SingletonAnnotatedClass.class));
        assertThrowsExactly(BeanNotFoundException.class, () -> container.getInstance(SingletonAnnotatedClass.class, "mockName"));
    }

    @Test
    void test_register_bean_with_private_constructor_fails(){
        assertThrowsExactly(ConstructorNotFoundException.class, () -> container.register(PrivateConstructorClass.class));
    }

    @Test
    void test_constructor_injectable_class_succeeds(){
        container.register(DefaultCommon.class);
        container.register(ConstructorInjectionClass.class);
        ConstructorInjectionClass constructorInjectionClass = container.getInstance(ConstructorInjectionClass.class);
        assertNotNull(constructorInjectionClass);
        assertNotNull(constructorInjectionClass.common());
        assertNull(constructorInjectionClass.fooClass());
    }

    @Test
    void test_retrieve_bean_by_super_type(){
        container.register(DefaultCommonChild.class);
        CommonI commonI = container.getInstance(CommonI.class);
        CommonSuperI commonSuperI =  container.getInstance(CommonSuperI.class);
        DefaultCommon defaultCommon = container.getInstance(DefaultCommon.class);

        assertInstanceOf(DefaultCommonChild.class, commonI);
        assertInstanceOf(DefaultCommonChild.class, commonSuperI);
        assertInstanceOf(DefaultCommonChild.class, defaultCommon);
    }

    @Test
    void test_assignable_types_register(){
        container.register(DefaultCommon.class);
        assertEquals(container.getRegisteredTypes(), Set.of(DefaultCommon.class, CommonI.class, CommonSuperI.class));
    }

    @Test
    void test_constructor_param_injection_succeeds(){
        container.register(FooClass.class);
        container.register(BarClass.class);
        container.register(ConstructorParamInjectionClass.class);
        ConstructorParamInjectionClass constructorParamInjectionClass = container.getInstance(ConstructorParamInjectionClass.class);
        assertNotNull(constructorParamInjectionClass);
        assertNotNull(constructorParamInjectionClass.getInjectable1());
        assertNotNull(constructorParamInjectionClass.getInjectable2());
        assertNull(constructorParamInjectionClass.getUnused());
    }

    @Test
    void test_private_constructor_class_fails(){
        assertThrowsExactly(ConstructorNotFoundException.class, () -> container.register(PrivateConstructorClass.class));

    }

    @Test
    void test_register_interface_fails(){
        assertThrowsExactly(IllegalArgumentException.class, () -> container.register(CommonI.class));
    }

    @Test
    void test_register_enum_fails(){
        assertThrowsExactly(IllegalArgumentException.class, () -> container.register(EnumClass.class));
    }


    @Test
    void test_register_abstract_class_fails(){
        assertThrowsExactly(IllegalArgumentException.class, () -> container.register(AbstractClass.class));
    }


    @Test
    void test_field_injection_succeeds(){
        container.register(FieldInjectionClass.class);
        container.register(FooClass.class);
        assumeTrue(container.isRegistered(FieldInjectionClass.class));
        FieldInjectionClass fieldInjectionClass = container.getInstance(FieldInjectionClass.class);
        assumeTrue(fieldInjectionClass != null);
        assertNotNull(fieldInjectionClass.dependency());
    }

    @Test
    void test_final_field_injection_fails(){
        assertThrowsExactly(UnsupportedFieldInjectionException.class, () -> container.register(FinalFieldInjection.class));
    }

    @Test
    void test_static_field_injection_fails(){
        assertThrowsExactly(UnsupportedFieldInjectionException.class, () -> container.register(StaticFieldInjection.class));
    }

    @Test
    void test_initialize_beans_throws_for_missing_dependencies(){

        container.register(FieldInjectionClass.class);
        assertThrowsExactly(BeanNotFoundException.class, container::initialize);
        container.register(FooClass.class);
    }

    @Test
    void test_initialize_beans_initializes_lazy_singleton(){
        container.register(FieldInjectionClass.class);
        container.register(FooClass.class);
        container.initialize();
    }

    @Test
    void test_scan_package(){
        container.scanPackage("dev.builder.di.scanner.mock");
        assertEquals(Set.of(MockBean.class, MockSingleton.class, InnerSingleton.class, InnerSingleton.InnerInnerSingleton.class, MoreInnerBean.class), container.getRegisteredTypes());
    }

    @Test
    void test_bean_annotated_bean_is_lazy(){
        container.register(PublicConstructorBean.class);
    }

    @Test
    void test_public_constructor_bean(){
        container.register(PublicConstructorBean.class);
        container.register(FooClass.class);
        PublicConstructorBean publicConstructorBean = container.getInstance(PublicConstructorBean.class);
        assertNotNull(publicConstructorBean);
        assertNotNull(publicConstructorBean.getFooClass());
        assertThat(publicConstructorBean.getFooClass()).isNotNull();
    }
}
