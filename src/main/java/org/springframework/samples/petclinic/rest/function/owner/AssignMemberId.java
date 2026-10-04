package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code memberId}, the one unified identity formatted
 * {@code <REGION><FY><HASH8><CHK>} (e.g. {@code NSW271A2B3C4D5}). The format and its derivation live
 * in {@link OwnerMemberId}; this step just stores it on the owner.
 *
 * <p>The identity is deterministic from the owner's own fields alone — there is no per-city sequence
 * number, so it does not depend on how many owners already exist. It is the single identity every
 * derived owner value flows from: the create audit record and the read-time {@code locality} (and
 * through it the {@code ownerSegment}) all read it rather than recomputing region independently.
 *
 * <p>Runs after {@link BuildOwner} (which maps the normalised request onto the owner and defaults
 * and business-day-adjusts the {@code registrationDate}, so both the fiscal-year and hash inputs are
 * already set) and before {@link DeduplicateMemberId} and {@link SaveOwner}.
 */
public class AssignMemberId {

    public void service(@Val Owner owner) {
        owner.setMemberId(OwnerMemberId.of(owner));
    }
}
