package dev.builder.core.application;

/**
 * Representa una acción que modifica el estado del sistema.
 *
 * @param <R> El tipo de resultado retornado después de ejecutar el comando. Puede ser {@code Void} si no hay valor útil que retornar.
 *
 * <p>Los comandos son usados para crear, actualizar o eliminar datos. No deben contener lógica de negocio,
 * pero pueden incluir validaciones estructurales básicas.</p>
 *
 * <p>Ejemplo: {@code CreateUserCommand implements Command<UUID>}</p>
 */
public interface Command<R> extends Request<R> {
}
