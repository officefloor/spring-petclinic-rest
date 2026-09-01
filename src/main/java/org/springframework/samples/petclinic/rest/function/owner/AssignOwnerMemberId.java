package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.MemberId;
import org.springframework.samples.petclinic.model.Owner;

/**
 * On create, assigns the owner's unified {@code memberId} '&lt;REGION&gt;&lt;FY&gt;&lt;HASH8&gt;&lt;CHK&gt;'.
 * Runs after the telephone has been normalized and the registration date adjusted so both the HASH8
 * and the fiscal-year segment are stable; there are no sequence numbers.
 */
public class AssignOwnerMemberId {

    public void service(@Val Owner owner) {
        owner.setMemberId(MemberId.of(owner));
    }
}
