package dev.builder.board.domain.model;

import dev.builder.core.domain.ValueObject;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Representa un color predefinido utilizado en el dominio, identificado por su valor hexadecimal.
 *
 * Este enum actúa como un {@link ValueObject}, ya que cada valor representa un objeto
 * inmutable y sin identidad propia. Se utiliza típicamente para etiquetar o diferenciar entidades
 * visualmente.
 *
 * Cada color está asociado a su código hexadecimal mediante la clase {@code HexCode}, la cual
 * encapsula y valida dicho valor.
 */
public enum Color implements ValueObject {

    // Colores disponibles
    AMBER(new HexCode("#F59E0B")),
    BLUE(new HexCode("#3B82F6")),
    CYAN(new HexCode("#06B6D4")),
    EMERALD(new HexCode("#10B981")),
    FUCHSIA(new HexCode("#D946EF")),
    GRAY(new HexCode("#6B7280")),
    GREEN(new HexCode("#22C55E")),
    INDIGO(new HexCode("#6366F1")),
    LIME(new HexCode("#84CC16")),
    ORANGE(new HexCode("#F97316")),
    PINK(new HexCode("#EC4899")),
    RED(new HexCode("#EF4444")),
    ROSE(new HexCode("#F43F5E")),
    SLATE(new HexCode("#64748B")),
    TEAL(new HexCode("#14B8A6")),
    VIOLET(new HexCode("#8B5CF6")),
    YELLOW(new HexCode("#EAB308")),
    ZINC(new HexCode("#71717A"));

    private final HexCode hexCode;

    /**
     * Constructor privado para asociar un código hexadecimal al color.
     *
     * @param hexCode El valor hexadecimal representado como un {@link HexCode}.
     */
    Color(HexCode hexCode) {
        this.hexCode = hexCode;
    }

    /**
     * Devuelve el valor hexadecimal asociado a este color.
     *
     * @return El código hexadecimal encapsulado en {@link HexCode}.
     */
    public HexCode hexCode() {
        return hexCode;
    }

    /**
     * Representa el nombre del color en minúsculas.
     *
     * Esto puede ser útil para serialización, logs o visualización en UI.
     *
     * @return El nombre del color en formato lowercase.
     */
    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
    /**
     * Representa un código hexadecimal de color como un {@link ValueObject}.
     *
     * Esta clase encapsula y valida cadenas que representan colores en formato
     * hexadecimal (por ejemplo, {@code "#F59E0B"} o {@code "f59e0b"}), y las normaliza
     * eliminando el símbolo de numeral y convirtiéndolas a minúsculas.
     *
     * Es utilizado comúnmente en sistemas donde los colores son configurables o parte
     * del dominio visual (por ejemplo, para representar colores de tarjetas o listas).
     */
    public record HexCode(String value) implements ValueObject {

        /**
         * Expresión regular para validar códigos hexadecimales válidos.
         * Acepta:
         * <ul>
         *   <li>6 dígitos: {@code #A1B2C3} o {@code A1B2C3}</li>
         *   <li>3 dígitos: {@code #ABC} o {@code abc}</li>
         *   <li>1 dígito: {@code #F} o {@code f}</li>
         * </ul>
         * Se permite el uso opcional del símbolo '#' al inicio.
         */
        private static final Pattern hexPattern = Pattern.compile("^#?([0-9a-fA-F]{6}|[0-9a-fA-F]{3}|[0-9a-fA-F])$");

        /**
         * Crea una nueva instancia de {@code HexCode}, validando y normalizando el valor.
         *
         * @param value Cadena que representa el color en formato hexadecimal.
         * @throws IllegalArgumentException Si el valor no representa un código hexadecimal válido.
         * @throws NullPointerException Si el valor es null.
         */
        public HexCode(String value) {
            this.value = cleanup(validate(value));
        }

        /**
         * Normaliza el código hexadecimal eliminando el símbolo '#' (si existe)
         * y convirtiéndolo a minúsculas.
         *
         * @param hexCode Código hexadecimal ya validado.
         * @return El código normalizado (sin '#' y en minúsculas).
         */
        private @NotNull String cleanup(@NotNull String hexCode) {
            String cleanCode = hexCode.replace("#", "");
            return cleanCode.toLowerCase();
        }

        /**
         * Valida que el valor proporcionado sea un código hexadecimal válido.
         *
         * @param hexCode Código a validar.
         * @return El mismo valor si es válido.
         * @throws IllegalArgumentException Si no es un código hexadecimal válido.
         */
        public static String validate(String hexCode) {
            if (!isValid(Objects.requireNonNull(hexCode))) {
                throw new IllegalArgumentException("Invalid hex code");
            }
            return hexCode;
        }

        /**
         * Verifica si el valor proporcionado es un código hexadecimal válido.
         *
         * @param value Cadena a validar.
         * @return {@code true} si el valor cumple el patrón de código hexadecimal, {@code false} en caso contrario.
         */
        public static boolean isValid(String value) {
            return value != null && hexPattern.matcher(value).matches();
        }
    }
}