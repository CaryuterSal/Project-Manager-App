package dev.builder.auth.application.command;

import dev.builder.core.application.Command;

/**
 * Comando para cerrar la sesión que tiene actualmente el usuario.
 * </br>
 * Esto elimina la información de sesión del {@link dev.builder.auth.domain.port.out.SessionContext}, y del archivo persistido, por lo que ya <b>no</b> se podrá recuperar la sesión con {@link RestoreSessionCommand}
 */
public record LogoutCommand() implements Command<Void> {
}
