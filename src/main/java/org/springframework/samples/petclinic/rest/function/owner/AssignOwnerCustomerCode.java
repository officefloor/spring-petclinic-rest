package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.function.common.Localities;

/**
 * Assigns the {@code customerCode} to a newly built owner, formatted
 * {@code <REGION>-<HASH8>} where {@code REGION} is the region code derived from the
 * owner's postcode (see {@link Localities#locality(String, String)}) and {@code HASH8}
 * is the first 8 upper-case hex characters of the SHA-256 digest over the owner's
 * normalized telephone (E.164, see {@link OwnerTelephone}) concatenated with its last
 * name (e.g. {@code NSW-1A2B3C4D}).
 *
 * <p>This is a stable identity: the region-and-hash value carries no sequence number, so
 * it does not depend on how many owners already exist. When that value collides with an
 * existing owner's {@code customerCode}, {@code -<n>} is appended with the smallest
 * {@code n} of 2 or more that makes it unique, so distinct owners always get distinct
 * customerCodes. Every value built from the customerCode — the
 * membershipNumber and its checkDigit, the create audit line and the derived locality —
 * follows from this region-and-hash identity. Mutates the owner in place (the same object
 * {@link SaveOwner} persists).
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = Localities.locality(owner.getCity(), owner.getPostcode());
        String telephone = OwnerTelephone.canonical(owner.getTelephone());
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        String base = region + "-" + hash8(telephone + lastName);

        Set<String> taken = ownerRepository.findAll().stream()
                .filter(existing -> existing.getId() == null
                        || !existing.getId().equals(owner.getId()))
                .map(Owner::getCustomerCode)
                .filter(code -> code != null)
                .collect(Collectors.toCollection(HashSet::new));

        String customerCode = base;
        for (int n = 2; taken.contains(customerCode); n++) {
            customerCode = base + "-" + n;
        }
        owner.setCustomerCode(customerCode);
    }

    /** The first 8 upper-case hex characters of the SHA-256 digest of {@code input}. */
    private static String hash8(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
