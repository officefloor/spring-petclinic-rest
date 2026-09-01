package org.springframework.samples.petclinic.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Builds an owner's customerCode as {@code <REGION>-<HASH8>}: REGION is the region
 * derived from the postcode (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099, otherwise
 * "UNKNOWN") and HASH8 is the first 8 upper-case hex characters of
 * SHA-256(normalizedTelephone + lastName).
 */
public final class CustomerCode {

    private CustomerCode() {
    }

    /** Region-and-hash customerCode for {@code owner}. */
    public static String of(Owner owner) {
        String region = "UNKNOWN";
        String postcode = owner.getPostcode();
        if (postcode != null && postcode.matches("\\d{4}")) {
            region = switch (Integer.parseInt(postcode) / 100) {
                case 20 -> "NSW";
                case 30 -> "VIC";
                case 40 -> "QLD";
                default -> "UNKNOWN";
            };
        }
        byte[] digest;
        try {
            digest = MessageDigest.getInstance("SHA-256")
                .digest((owner.getTelephone() + owner.getLastName()).getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
        String hash8 = HexFormat.of().withUpperCase().formatHex(digest).substring(0, 8);
        return region + "-" + hash8;
    }
}
