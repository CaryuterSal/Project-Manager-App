package dev.builder.auth.infrastructure;

import dev.builder.auth.domain.port.out.SessionContext;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.usermanagement.domain.model.User;
import org.jetbrains.annotations.NotNull;

/**
 * {@code InMemorySessionContext} mantiene la información de autenticación
 * del usuario actualmente logueado en la aplicación.
 *
 * <p>Esta clase se comporta como un contenedor de sesión en memoria y permite a los componentes
 * consultar el correo electrónico del usuario autenticado
 * y verificar su rol.</p>
 *
 * @see User
 * @see Role
 */
@Bean
public class InMemorySessionContext implements SessionContext {
    private String email;
    private Role role;

    @Override
    public void setAuthentication(@NotNull User<?> user) {
        this.email = user.email().value();
        this.role = Role.fromUser(user);
    }

    @Override
    public String getCurrentUser() {
        return email;
    }

    @Override
    public Role getCurrentRole() {
        return role;
    }

    @Override
    public boolean isAuthenticated() {
        return email != null;
    }

    @Override
    public boolean hasRole(@NotNull Role role) {
        return role.equals(this.role);
    }

    @Override
    public void clear(){
        this.email = null;
        this.role = null;
    }
}
