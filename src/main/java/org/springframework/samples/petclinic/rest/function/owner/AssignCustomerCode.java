package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the unified {@code memberId} (see {@link MemberId}). REGION is derived from the
 * postcode (see {@link Locality}) and HASH8 is the first 8 upper-case hex characters of
 * SHA-256 over the normalised telephone plus lastName.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        // Version-2 region: the plain region with a fixed 'V2' tag mixed in, so the memberId
        // changes and never reproduces a version-1 value. The tag stays inside the identifier;
        // the user-facing locality/timezone/segment still use the plain region.
        String region = "V2" + Locality.of(owner.getCity(), owner.getPostcode());
        String hash8 = hash8(E164Telephone.toE164(owner.getTelephone()) + owner.getLastName());
        owner.setCustomerCode(MemberId.of(region, owner.getRegistrationDate(), hash8));
    }

    private static String hash8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                hex.append(String.format("%02X", digest[i]));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
