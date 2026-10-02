package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the {@code customerCode} formatted {@code <REGION>-<HASH8>}, the single region-and-hash owner
 * identity. REGION is the region code derived from the owner's {@link Owner#getPostcode() postcode} via
 * {@link Owner#regionOfPostcode(String)} (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099), or
 * {@code "UNKNOWN"} when the postcode is absent or in no known range. HASH8 is the first 8 upper-case
 * hex characters of the SHA-256 of the owner's {@link OwnerTelephone normalized (E.164) telephone}
 * concatenated with the last name.
 *
 * <p>There are no sequence numbers: two owners with the same normalized telephone and last name in the
 * same region map to the same customer code. Every value built from the customer code — the
 * {@link AssignMembershipNumber membership number} and its {@link Owner#getCheckDigit() check digit},
 * the {@link AuditOwnerCreated create audit record} and the {@link Owner#getLocality() locality} — reads
 * this region-and-hash value.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        String region = Owner.regionOfPostcode(owner.getPostcode());
        if (region == null) {
            region = "UNKNOWN";
        }
        String normalizedTelephone = OwnerTelephone.toE164(owner.getTelephone());
        if (normalizedTelephone == null) {
            normalizedTelephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        }
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        owner.setCustomerCode(region + "-" + hash8(normalizedTelephone + lastName));
    }

    /** First 8 upper-case hex characters of the SHA-256 of {@code value}'s UTF-8 bytes. */
    private static String hash8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString().toUpperCase(Locale.ROOT);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
