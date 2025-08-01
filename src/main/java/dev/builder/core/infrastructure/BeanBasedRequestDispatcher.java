package dev.builder.core.infrastructure;

import dev.builder.core.application.RequestDispatcher;
import dev.builder.core.application.RequestHandler;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.di.runtime.BeanRetriever;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Bean
public class BeanBasedRequestDispatcher extends RequestDispatcher {

    private final BeanRetriever container;

    @Inject
    public BeanBasedRequestDispatcher(BeanRetriever container) {
        this.container = container;
        indexHandlers();
    }

    private void indexHandlers() {
        for (RequestHandler<?,?> handler : container.getInstancesOfType(RequestHandler.class)) {
            Class<?> requestType = extractRequestType(handler);
            if (requestType != null) {
                handlers.put(requestType, handler);
            }
        }
    }

    private @Nullable Class<?> extractRequestType(@NotNull RequestHandler<?, ?> handler) {
        for (Type iface : handler.getClass().getGenericInterfaces()) {
            if (iface instanceof ParameterizedType pt && pt.getRawType() == RequestHandler.class) {
                Type requestType = pt.getActualTypeArguments()[0];
                if (requestType instanceof Class<?> cls) {
                    return cls;
                }
            }
        }
        return null;
    }
}
