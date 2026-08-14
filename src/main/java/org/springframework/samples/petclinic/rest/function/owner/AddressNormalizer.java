package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Canonical normalization for an owner's {@code address}. Whitespace is trimmed and every run of
 * whitespace collapsed to a single space, the text is upper-cased, and common abbreviations are
 * expanded on a whole-word basis ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}).
 * The transform is idempotent, so normalizing an already-normalized address returns it unchanged.
 *
 * <p>Applied whenever an owner is created (see {@link ValidateOwnerFields}) so the stored and returned
 * {@code address} is the normalized string, and reused wherever addresses are compared
 * (see {@link AssignHousehold}) so those comparisons see the same form.
 */
final class AddressNormalizer {

    private AddressNormalizer() {
    }

    static String normalize(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase();
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder sb = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(expand(tokens[i]));
        }
        return sb.toString();
    }

    private static String expand(String token) {
        switch (token) {
            case "ST":
                return "STREET";
            case "RD":
                return "ROAD";
            case "AVE":
                return "AVENUE";
            default:
                return token;
        }
    }
}
