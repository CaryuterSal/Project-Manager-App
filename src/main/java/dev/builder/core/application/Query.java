package dev.builder.core.application;

/**
 * Representa una solicitud de lectura o consulta de información.
 *
 * @param <R> El tipo de resultado esperado al ejecutar la consulta.
 *
 * <p>Las queries no deben producir efectos secundarios y deben ser puramente de lectura.</p>
 *
 * <p>Ejemplo: {@code GetUserByEmailQuery implements Query<UserDto>}</p>
 */
public interface Query<R> extends Request<R> {
}
