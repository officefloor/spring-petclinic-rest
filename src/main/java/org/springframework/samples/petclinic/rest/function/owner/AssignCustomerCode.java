package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <REGION>-<HASH8>} where REGION is the
 * region code derived from the postcode (see {@link CityRegion#localityOf(String, String)}) and
 * HASH8 is the first 8 upper-case hex characters of SHA-256 over {@code normalizedTelephone + lastName}
 * (see {@link OwnerIdentity#customerCodeHash}). For example {@code NSW-1A2B3C4D}.
 *
 * <p>The base code depends only on this owner's own fields and is stable regardless of how many owners
 * exist. When that base collides with an existing owner's {@code customerCode}, this step de-duplicates
 * it by appending {@code -<n>} with the smallest {@code n} of 2 or more that makes it unique (e.g.
 * {@code NSW-1A2B3C4D-2}). Every value derived from the customer code — the membership number and its
 * check digit (see {@link AssignMembershipNumber}, {@link CustomerCodeCheckDigit}), the create audit
 * line (see {@link AuditOwnerCreated}) and the response {@code locality} — follows the de-duplicated
 * region-and-hash identity.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = CityRegion.localityOf(owner.getCity(), owner.getPostcode());
        String hash8 = OwnerIdentity.customerCodeHash(owner.getTelephone(), owner.getLastName());
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
