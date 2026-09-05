package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the {@code customerCode} formatted {@code <REGION>-<HASH8>}, where REGION is the region
 * code derived from the owner's postcode (see {@link Locality}, which prefers the postcode's range,
 * falling back to the city table and finally {@code "UNKNOWN"}) and HASH8 is the first 8 upper-case
 * hex characters of SHA-256 over the normalized telephone concatenated with the last name (e.g.
 * {@code NSW-1A2B3C4D}). The telephone has already been normalized to E.164 form by
 * {@link NormalizeTelephone} earlier in the pipeline, so the stored value is used directly.
 * <p>
 * When the computed code collides with an existing owner's {@code customerCode}, {@code -<n>} is
 * appended with the smallest {@code n} of 2 or more that makes it unique (e.g. {@code NSW-1A2B3C4D-2}).
 * Runs before the owner is saved, so {@link OwnerRepository#findAll()} sees only the owners that
 * existed before this create.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = Locality.of(owner.getCity(), owner.getPostcode());
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        String hash8 = Sha256.hex(telephone + lastName).substring(0, 8).toUpperCase(Locale.ROOT);
        String base = region + "-" + hash8;

        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            if (other.getCustomerCode() != null) {
                existing.add(other.getCustomerCode());
            }
        }

        String customerCode = base;
        for (int n = 2; existing.contains(customerCode); n++) {
            customerCode = base + "-" + n;
        }
        owner.setCustomerCode(customerCode);
    }
}
