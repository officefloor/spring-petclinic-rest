package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Formats a stored E.164 telephone for humans: the country calling code, a space, then
 * the national digits grouped in threes (e.g. {@code +61412345678 -> +61 412 345 678}).
 * The raw E.164 value is left untouched; this is display-only.
 */
public final class TelephoneDisplay {

    private TelephoneDisplay() {
    }

    /** Human-readable rendering of an E.164 number, or the input unchanged when it is not E.164. */
    public static String display(String telephone) {
        if (telephone == null || !telephone.startsWith("+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        String code = digits.startsWith("61") ? "61" : digits.startsWith("1") ? "1" : "";
        String national = digits.substring(code.length()).replaceAll("(\\d{3})(?=\\d)", "$1 ");
        return "+" + code + (national.isEmpty() ? "" : " " + national);
    }
}
