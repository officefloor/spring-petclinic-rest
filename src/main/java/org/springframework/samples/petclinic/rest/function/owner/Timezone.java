package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Maps an owner's region (the {@code locality} prefix of the customer code) to its IANA timezone via
 * the pinned table NSW-&gt;Australia/Sydney, VIC-&gt;Australia/Melbourne, QLD-&gt;Australia/Brisbane. Any
 * other region (e.g. UNKNOWN) has no timezone and yields {@code null}.
 */
public class Timezone {

    public static String of(String region) {
        return switch (region) {
            case "NSW" -> "Australia/Sydney";
            case "VIC" -> "Australia/Melbourne";
            case "QLD" -> "Australia/Brisbane";
            default -> null;
        };
    }
}
