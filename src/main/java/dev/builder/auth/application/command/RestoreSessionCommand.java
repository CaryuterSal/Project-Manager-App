package dev.builder.auth.application.command;

import dev.builder.core.application.Command;

/**
 * Restablece la sesión existente en caso de reinicio de aplicación gracias al archivo persistente encriptado.
 *
 * </br>
 * Regresa un <b>boolean</b> indicando si se restableció exitosamente la sesión.
 * </br>
 * Casos negativos podrían ser:
 * <ul>
 *     <li>El usuario no tenía una sesión activa</li>
 *     <li>El tiempo de vida de la sesión expiró (por defecto 1 día)</li>
 * </ul>
 */
public record RestoreSessionCommand() implements Command<Boolean> {
}
