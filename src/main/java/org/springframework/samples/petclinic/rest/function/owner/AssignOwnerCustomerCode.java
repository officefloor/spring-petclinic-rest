package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's unified {@code memberId} formatted {@code <REGION><FY><HASH8><CHK>}: REGION is
 * the region code (see {@link Locality}), FY is the 2-digit fiscal year (see {@link FiscalYear}),
 * HASH8 is the first 8 upper-case hex characters of SHA-256 over the normalized telephone followed
 * by the last name, and CHK is the single Luhn check digit (see {@link CheckDigit}) over the digits
 * of {@code <REGION><FY><HASH8>} (e.g. 'NSW271A2B3C4D5'). Stored in the {@code customerCode} slot,
 * so downstream deduplication, the create audit record and the response all read this identity.
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner) {
        owner.setCustomerCode("V2" + Locality.of(owner) + FiscalYear.yy(owner) + hash8(owner));
        owner.setCustomerCode(owner.getCustomerCode() + CheckDigit.of(owner));
    }

    private static String hash8(Owner owner) {
        String key = owner.getTelephone() + owner.getLastName();
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder(8);
            for (int i = 0; i < 4; i++) {
                hash.append(String.format("%02X", digest[i]));
            }
            return hash.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
