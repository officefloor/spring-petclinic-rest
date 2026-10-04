package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code customerCode}, the region-and-hash identity formatted
 * {@code <REGION>-<HASH8>} (e.g. {@code NSW-1A2B3C4D}). The format and its derivation live in
 * {@link OwnerCustomerCode}; this step just stores it on the owner.
 *
 * <p>The identity is deterministic from the owner's own fields alone — there is no per-city
 * sequence number any more, so it no longer depends on how many owners already exist. It is the
 * single region-and-hash identity every derived owner value flows from: the {@code membershipNumber}
 * and its {@code checkDigit}, the create audit record and the read-time {@code locality} all read it
 * rather than recomputing region independently.
 *
 * <p>Runs after {@link BuildOwner} (which maps the normalised request onto the owner) and before
 * {@link AssignMembershipNumber} and {@link SaveOwner}.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        owner.setCustomerCode(OwnerCustomerCode.of(owner));
    }
}
