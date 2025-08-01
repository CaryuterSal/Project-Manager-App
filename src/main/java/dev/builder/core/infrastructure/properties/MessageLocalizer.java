package dev.builder.core.infrastructure.properties;

/**
 * Interfaz para la resolución de mensajes parametrizados a partir de claves, generalmente utilizados en
 * validaciones, mensajes de error, o internacionalización (i18n).
 *
 * <p>Permite recuperar un mensaje formateado dinámicamente usando una clave y parámetros opcionales,
 * similar al comportamiento de {@link java.text.MessageFormat}.</p>
 *
 * <p>Puede utilizarse junto con archivos `.properties` o cualquier otro origen de mensajes localizado.</p>
 *
 * <p>Ejemplo de uso:
 * <pre>
 *     String msg = resolver.getMessage("validation.field.length.min", "nombre", 3);
 *     // → "El campo nombre debe tener al menos 3 caracteres de longitud"
 * </pre>
 * </p>
 *
 * @see java.text.MessageFormat
 */
@FunctionalInterface
public interface MessageLocalizer {

    /**
     * Devuelve el mensaje correspondiente a la clave dada, reemplazando los parámetros si es necesario.
     *
     * @param key  Clave del mensaje (por ejemplo, "validation.email.invalid").
     * @param args Argumentos opcionales para reemplazar en el mensaje.
     * @return Mensaje localizado y formateado con los parámetros provistos.
     */
    String getMessage(String key, Object... args);
}
