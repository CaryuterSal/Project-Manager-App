package dev.builder.board.domain.model;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
/**
 * Representa un archivo de imagen almacenado en el sistema.
 *
 * Extiende la entidad {@link StoredFile}, especializándola para archivos
 * que corresponden exclusivamente a imágenes con formatos soportados.
 *
 * Proporciona validación específica para nombres de archivo que deben
 * tener extensiones válidas de imágenes comunes.
 */
public final class Image extends StoredFile<Image.Id> {

    /**
     * Crea una nueva instancia de {@code Image} con identificador, nombre y tipo MIME.
     *
     * @param id Identificador único de la imagen.
     * @param name Nombre del archivo de imagen (validado).
     * @param mimeType Tipo MIME correspondiente al archivo.
     */
    public Image(Image.Id id, Task.Id attachedTo, Filename name, MimeType mimeType, long contentLength) {
        super(id, attachedTo, name, mimeType, contentLength);
    }

    public static class Id extends StoredFile.Id<Id> {
        public Id(UUID uuid) {
            super(uuid);
        }

        @Override
        public int compareTo(Image.@NotNull Id  id) {
            return id.compareTo(this);
        }
    }
    /**
     * Clase interna que representa un nombre válido para un archivo de imagen.
     *
     * Extiende {@link StoredFile.Filename} añadiendo validación de extensiones
     * para formatos de imagen soportados.
     */
    public static final class Filename extends StoredFile.Filename {


        /**
         * Patrón regex para validar extensiones de archivo de imagen soportadas:
         * GIF, JPEG/JPG, TIFF, PNG, BMP.
         */
        private static final Pattern extensionPattern = Pattern.compile("\\.(gif|jpe?g|tiff?|png|bmp)$", Pattern.CASE_INSENSITIVE);

        /**
         * Crea un nuevo nombre de archivo para imagen validando su formato.
         *
         * @param value El nombre del archivo a validar.
         * @throws IllegalArgumentException Si el nombre no cumple con las extensiones permitidas.
         */
        public Filename(String value) {
            super(validate(value));
        }

        /**
         * Valida que el nombre de archivo tenga una extensión soportada para imágenes.
         * También invoca la validación base de {@link StoredFile.Filename}.
         *
         * @param value El nombre del archivo a validar.
         * @return El mismo valor si es válido.
         * @throws NullPointerException Si el valor es null.
         * @throws IllegalArgumentException Si la extensión no es soportada.
         */
        public static String validate(String value) {
            StoredFile.Filename.validate(value);
            if (!isValid(Objects.requireNonNull(value))) {
                throw new IllegalArgumentException("Image name isn't of a supported format");
            }
            return value;
        }

        /**
         * Verifica si el nombre de archivo tiene una extensión soportada.
         *
         * @param value El nombre del archivo a comprobar.
         * @return {@code true} si el archivo tiene una extensión soportada, {@code false} en caso contrario.
         */
        public static boolean isValid(String value) {
            return value != null && extensionPattern.matcher(value).find();
        }


    }
}
