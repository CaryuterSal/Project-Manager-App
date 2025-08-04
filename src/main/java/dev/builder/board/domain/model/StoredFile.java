package dev.builder.board.domain.model;

import dev.builder.core.domain.AggregateRoot;
import dev.builder.core.domain.ValueObject;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;


/**
 * Representa un archivo almacenado en el sistema, funcionando como un {@link AggregateRoot}
 * con un identificador único.
 *
 * Este tipo base incluye un nombre de archivo validado y un tipo MIME,
 * y puede ser extendido para tipos específicos de archivos.
 */
public abstract class StoredFile<ID extends StoredFile.Id<?>> extends AggregateRoot<ID> {

    protected final Task.Id attachedTo;
    /**
     * Nombre del archivo, encapsulado en un objeto de valor {@link Filename}.
     */
    protected final Filename filename;

    /**
     * Tipo MIME que describe el formato del archivo.
     */
    protected final MimeType mimeType;

    /**
     * Crea un StoredFile con ID, nombre y tipo MIME especificados.
     *
     * @param id Identificador único del archivo
     * @param filename Nombre del archivo
     * @param mimeType Tipo MIME del archivo
     * @throws NullPointerException si alguno de los parámetros son nulos
     */
    public StoredFile(ID id, Task.Id attachedTo, Filename filename, MimeType mimeType) {
        super(id);
        this.attachedTo = Objects.requireNonNull(attachedTo);
        this.filename = Objects.requireNonNull(filename, "filename must not be null");
        this.mimeType = Objects.requireNonNull(mimeType, "mimeType must not be null");
    }

    /**
     * Obtiene el nombre actual del archivo.
     *
     * @return El nombre encapsulado en un objeto {@link Filename}.
     */
    public Filename name(){
        return filename;
    }

    /**
     * Obtiene el tipo MIME del archivo.
     *
     * @return El tipo MIME.
     */
    public MimeType mimeType() {
        return mimeType;
    }

    public Task.Id attachedTo() {
        return attachedTo;
    }

    /**
     * Objeto de valor que representa el nombre de archivo, con validación.
     *
     * El nombre debe cumplir un patrón que permita caracteres alfanuméricos,
     * guiones bajos y guiones medios, y una extensión con punto (ejemplo: "archivo_1.txt").
     */
    public static class Filename implements ValueObject<Filename> {

        private final String value;

        private static final Pattern pattern = Pattern.compile("^[\\w-_]+(\\.\\w+)+$");

        /**
         * Crea un nuevo nombre de archivo validando su formato.
         *
         * @param value Nombre del archivo.
         * @throws IllegalArgumentException Si el nombre no es válido.
         */
        public Filename(String value) {
            this.value = validate(value);
        }

        /**
         * Devuelve el valor del nombre de archivo.
         *
         * @return Nombre del archivo como cadena.
         */
        public String value() {
            return value;
        }

        /**
         * Valida que el nombre cumpla con el patrón definido.
         *
         * @param value Nombre a validar.
         * @return El mismo valor si es válido.
         * @throws IllegalArgumentException Si el nombre no es válido.
         */
        public static String validate(String value) {
            if (!isValid(Objects.requireNonNull(value))) {
                throw new IllegalArgumentException("Invalid attachment name: " + value);
            }
            return value;
        }

        /**
         * Comprueba si el nombre cumple el patrón esperado.
         *
         * @param value Nombre a validar.
         * @return true si cumple el patrón, false en caso contrario.
         */
        public static boolean isValid(String value) {
            return pattern.matcher(value).matches();
        }

        @Override
        public int compareTo(@NotNull StoredFile.Filename filename) {
            return value.compareTo(filename.value());
        }
    }

    /**
     * Identificador único de un StoredFile, basado en UUID.
     */
    public static class Id<ID extends Id<ID>> implements ValueObject<ID>{

        private final UUID uuid;
        /**
         * Crea un nuevo Id validando el UUID.
         *
         * @param uuid UUID del identificador.
         * @throws IllegalArgumentException Si el UUID no es válido.
         */
        public Id(UUID uuid) {
            this.uuid = validate(uuid);
        }

        public UUID uuid() {
            return uuid;
        }

        /**
         * Valida que el UUID no sea null.
         *
         * @param uuid UUID a validar.
         * @return El mismo UUID si es válido.
         * @throws IllegalArgumentException Si el UUID es null.
         */
        public static UUID validate(UUID uuid) {
            if (!isValid(Objects.requireNonNull(uuid))) {
                throw new IllegalArgumentException("Invalid attachment id");
            }
            return uuid;
        }

        /**
         * Verifica que el UUID sea no null.
         *
         * @param uuid UUID a verificar.
         * @return true si no es null, false en caso contrario.
         */
        public static boolean isValid(UUID uuid) {
            return uuid != null;
        }

        @Override
        public int compareTo(@NotNull ID id) {
            return uuid.compareTo(id.uuid());
        }
    }

    /**
     * Enumeración de tipos MIME soportados para archivos almacenados.
     * Incluye formatos de imágenes, documentos y archivos comprimidos.
     */
    public enum MimeType implements ValueObject<MimeType> {
        // Imágenes
        JPEG("image/jpeg"),
        PNG("image/png"),
        GIF("image/gif"),
        WEBP("image/webp"),
        SVG("image/svg+xml"),

        // Documentos
        PDF("application/pdf"),
        DOC("application/msword"),
        DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
        XLS("application/vnd.ms-excel"),
        XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
        TXT("text/plain"),
        JSON("application/json"),

        // Comprimidos
        ZIP("application/zip"),
        TAR("application/x-tar"),
        GZ("application/gzip"),
        SEVENZ("application/x-7z-compressed");

        private final String text;

        MimeType(String type) {
            this.text = type;
        }

        /**
         * Devuelve el valor string del tipo MIME.
         *
         * @return El tipo MIME en formato texto.
         */
        public String asText() {
            return text;
        }

        /**
         * Obtiene el enum MimeType correspondiente a una cadena dada.
         *
         * @param raw Tipo MIME como cadena.
         * @return El enum correspondiente.
         * @throws IllegalArgumentException Si no se encuentra un tipo válido.
         */
        public static MimeType fromValue(String raw) {
            return Arrays.stream(values())
                    .filter(m -> m.text.equalsIgnoreCase(raw))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No se soporta este formato de archivo"));
        }


    }
}

