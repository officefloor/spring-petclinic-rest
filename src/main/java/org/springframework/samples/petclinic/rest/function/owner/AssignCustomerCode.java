package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.function.common.CustomerCodes;

/**
 * Step of {@code POST /api/owners} that assigns the owner's {@code customerCode}, formatted
 * {@code '<REGION>-<HASH8>'} where {@code REGION} is the region derived from the postcode (falling
 * back to the city, see {@link org.springframework.samples.petclinic.rest.function.common.Localities})
 * and {@code HASH8} is the first eight upper-case hex characters of
 * {@code SHA-256(normalizedTelephone + lastName)} (e.g. {@code 'NSW-1A2B3C4D'}).
 *
 * <p>Runs after {@link BuildOwner} and before {@link SaveOwner}; {@code @Val} yields the built owner so
 * the code is mutated in place and persisted by the save step. The identity is derived entirely from
 * the owner's own fields — the telephone is already normalized to E.164 by this point — so it is
 * seed-independent and carries no sequence number.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        owner.setCustomerCode(CustomerCodes.of(owner));
    }
}
