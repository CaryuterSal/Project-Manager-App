package dev.builder.usermanagement.domain.model;

import dev.builder.core.domain.ValueObject;
import org.jetbrains.annotations.NotNull;
/**
 * Encapsula la lógica de validación de contraseña con verificación de fuerza
 */
public final class Password implements ValueObject<Password> {

    private final String value;
    private final Strength strength;

    public Password(String value){
        this.value = validate(value).trim();
        this.strength = Strength.forPassword(value);
    }

    public String value() {
        return value;
    }

    public Strength strength() {
        return strength;
    }

    public static String validate(String password){
        if(!isValid(password)){
            throw new IllegalArgumentException("Invalid password");
        }
        return password;
    }

    public static boolean isValid(String password) {
        return password != null && !Strength.INVALID.equals(Strength.forPassword(password));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Password password)) return false;

        return value().equals(password.value()) && strength() == password.strength();
    }

    @Override
    public int hashCode() {
        int result = value().hashCode();
        result = 31 * result + strength().hashCode();
        return result;
    }

    /**
     * Representa la fuerza de una contraseña
     */
    public enum Strength implements ValueObject<Strength>{

        INVALID,
        WEAK,
        NORMAL,
        STRONG;

        /**
         * Valida la fuerza de una contraseña usando los siguientes criterios
         * <ol>
         *     <li>Las contraseñas deben tener al menos 8 caracteres</li>
         *     <li>Las contraseñas fuertes tienen:</li>
         *     <ul>
         *         <li>Al menos una letra minúscula</li>
         *         <li>Al menos una letra mayúscula</li>
         *         <li>Al menos un número</li>
         *         <li>Al menos 8 caractéres</li>
         *         <li>Al menos un símbolo</li>
         *     </ul>
         * </ol>
         * @param password la contraseña como String, no puede ser {@code null}
         * @return la fuerza de la contraseña
         */
        public static @NotNull Strength forPassword(@NotNull String password){
            String trimmed = password.trim();
            int score = 0;
            if(trimmed.length() <= 6) return INVALID;
            if(trimmed.matches(".*[a-z].*")) score++;
            if(trimmed.matches(".*[A-Z].*")) score++;
            if(trimmed.matches(".*[0-9].*")) score++;
            if(trimmed.matches(".*[!@#$%^&*()\\-_=+{}\\[\\]:;\"'<>,.?/].*")) score++;
            if(trimmed.length() > 8 && trimmed.length() < 12){
                score++;
            } else if (trimmed.length() >= 12) {
                score += 2;
            }

            return switch (score){
                case 2 -> Strength.WEAK;
                case 3,4 -> Strength.NORMAL;
                case 5,6 -> Strength.STRONG;
                default -> Strength.INVALID;
            };
        }
    }

    @Override
    public int compareTo(@NotNull Password password) {
        return strength().compareTo(password.strength());
    }
}
