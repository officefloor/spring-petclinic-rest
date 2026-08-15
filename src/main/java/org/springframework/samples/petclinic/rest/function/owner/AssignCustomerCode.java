package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <REGION>-<HASH8>} where REGION is the
 * region code derived from the postcode (see {@link OwnerLocality}) and HASH8 is the first eight
 * upper-case hex characters of SHA-256 over {@code normalizedTelephone + lastName}. The telephone
 * is normalized to E.164 (see {@link TelephoneE164}) exactly as the owner is stored, so the hash is
 * stable regardless of the telephone's input format. There are no sequence numbers: the identity is
 * fully determined by the owner's region, telephone and surname.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        String region = OwnerLocality.of(owner.getCity(), owner.getPostcode());
        String normalizedTelephone = normalizedTelephone(owner.getTelephone());
        String hash8 = sha256Hex8(normalizedTelephone + owner.getLastName());
        owner.setCustomerCode(region + "-" + hash8);
    }

    /** The owner's telephone in the same canonical E.164 form it is stored with. */
    private static String normalizedTelephone(String telephone) {
        String e164 = TelephoneE164.toE164(telephone);
        return e164 != null ? e164 : (telephone == null ? "" : telephone);
    }

    /** First eight upper-case hex characters of SHA-256 over the UTF-8 bytes of {@code value}. */
    private static String sha256Hex8(String value) {
        byte[] digest;
        try {
            digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex); // never on a standard JRE
        }
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 4; i++) {
            sb.append(String.format("%02X", digest[i]));
        }
        return sb.toString().toUpperCase(Locale.ROOT);
    }
}
