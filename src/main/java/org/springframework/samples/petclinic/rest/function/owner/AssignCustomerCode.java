package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.Regions;
import org.springframework.samples.petclinic.model.CustomerCode;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Runs after {@link BuildOwner} and before {@link SaveOwner} on {@code POST /api/owners}. Assigns the
 * new owner's {@code customerCode} formatted {@code <REGION>-<HASH8>}, where REGION is the region code
 * derived from the postcode (see {@link Regions#regionForPostcode(String)}, e.g. {@code NSW}, or
 * {@code UNKNOWN} when the postcode resolves to no known region) and HASH8 is the first 8 upper-case
 * hex characters of {@code SHA-256(normalizedTelephone + lastName)} (see
 * {@link CustomerCode#hash8(String, String)}, e.g. {@code NSW-1A2B3C4D}). The identity no longer uses
 * per-city sequence numbers, so this step depends only on the owner's own fields.
 *
 * <p>When the computed code collides with an existing owner's {@code customerCode}, {@code -<n>} is
 * appended with the smallest {@code n} of 2 or more that makes it unique (e.g. {@code NSW-1A2B3C4D-2}),
 * so distinct owners always get distinct customer codes. The new owner is still unsaved, so it is not
 * among the owners compared against here and cannot collide with itself.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = Regions.regionForPostcode(owner.getPostcode());
        String hash8 = CustomerCode.hash8(owner.getTelephone(), owner.getLastName());
        String base = CustomerCode.format(region, hash8);

        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            String code = other.getCustomerCode();
            if (code != null) {
                existing.add(code);
            }
        }

        String code = base;
        for (int n = 2; existing.contains(code); n++) {
            code = CustomerCode.withSuffix(base, n);
        }
        owner.setCustomerCode(code);
    }
}
