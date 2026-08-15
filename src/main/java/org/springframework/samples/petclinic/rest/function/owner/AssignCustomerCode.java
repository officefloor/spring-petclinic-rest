package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.util.LocalityResolver;
import org.springframework.samples.petclinic.util.Sha256Hash;

/**
 * Assigns the owner's {@code customerCode} before it is saved. The code is formatted
 * {@code <REGION>-<HASH8>}, where {@code REGION} is the region code derived from the postcode
 * (see {@link LocalityResolver}) and {@code HASH8} is the first 8 upper-case hex characters of
 * SHA-256 over the concatenation of the normalized telephone and the last name
 * (e.g. {@code NSW-3F1A9C2B}). It carries no sequence number, so it depends only on the owner's own
 * data. The telephone was normalized to E.164 by {@link ValidateNewOwner}, so
 * {@code owner.getTelephone()} is already the normalized value.
 *
 * <p>Should the computed code collide with an existing owner's {@code customerCode}, it is
 * de-duplicated by appending {@code -<n>} with the smallest {@code n} of 2 or more that makes it
 * unique (e.g. {@code NSW-3F1A9C2B-2}). Runs after {@link BuildOwner} and before {@link SaveOwner},
 * so {@link OwnerRepository#findAll()} returns only the pre-existing owners. Mutates the entity in
 * place so {@link SaveOwner} persists it.
 */
public class AssignCustomerCode {

    /** Number of upper-case hex characters taken from the SHA-256 digest. */
    private static final int HASH_LENGTH = 8;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = LocalityResolver.localityOf(owner.getPostcode(), owner.getCity());
        String normalizedTelephone = owner.getTelephone();
        String hash8 = Sha256Hash.upperHex(normalizedTelephone + owner.getLastName(), HASH_LENGTH);
        String baseCode = region + "-" + hash8;

        Set<String> existingCodes = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            existingCodes.add(other.getCustomerCode());
        }

        String customerCode = baseCode;
        for (int n = 2; existingCodes.contains(customerCode); n++) {
            customerCode = baseCode + "-" + n;
        }
        owner.setCustomerCode(customerCode);
    }
}
