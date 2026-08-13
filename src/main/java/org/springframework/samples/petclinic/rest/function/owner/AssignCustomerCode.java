package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code '<REGION>-<HASH8>'} where
 * {@code REGION} is the region derived from the owner (postcode first, then city; see
 * {@link Owner#getRegion()}) and {@code HASH8} is the first 8 upper-case hex characters
 * of {@code SHA-256(normalizedTelephone + lastName)} — the telephone in the E.164 form
 * already normalized by {@link ValidateOwnerFields}. There are no sequence numbers: the
 * code is a pure function of the owner's region and identity, so it is stable and needs
 * no per-city count.
 *
 * <p>Runs after {@link BuildOwner} and before {@link SaveOwner} in the
 * {@code POST /api/owners} pipeline; the code is persisted with the new owner and
 * returned by later reads. Because it shares {@link Owner#getRegion()} with the
 * {@code locality} field, the two always agree. It mutates the built {@link Owner} in
 * place (see {@code @Val} semantics).
 *
 * <p>Should the computed {@code '<REGION>-<HASH8>'} collide with an existing owner's
 * {@code customerCode}, {@code '-<n>'} is appended with the smallest {@code n} of 2 or
 * more that makes it unique; the de-duplicated code is what gets stored. The new owner
 * is not yet persisted here, so it is never compared against itself.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String hash8 = shaHex8(owner.getTelephone() + owner.getLastName());
        String base = owner.getRegion() + "-" + hash8;

        Set<String> existingCodes = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            existingCodes.add(existing.getCustomerCode());
        }

        String code = base;
        for (int n = 2; existingCodes.contains(code); n++) {
            code = base + "-" + n;
        }
        owner.setCustomerCode(code);
    }

    /** First 8 upper-case hex characters of SHA-256 over the UTF-8 bytes of {@code value}. */
    private static String shaHex8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
