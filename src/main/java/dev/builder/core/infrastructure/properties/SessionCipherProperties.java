package dev.builder.core.infrastructure.properties;

/**
 * Obtiene las propiedades para el cifrado de los archivos de sesión
 */
public interface SessionCipherProperties {
    /**
     * Obtiene el patrón para la transformación de {@link javax.crypto.Cipher}
     * @return la combinación de la transformación
     */
    String getCipherTransformation();

    /**
     * Obtiene el tipo de cifrado que requiere {@link javax.crypto.SecretKey}
     * @return el nombre del cifrado
     */
    String getCipherKeyType();


    String getCipherPassword();
    String getCipherSalt();
    int getCipherIterations();
    int getCipherKeyLength();
}
