package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.function.common.Hashes;
import org.springframework.samples.petclinic.rest.function.common.Localities;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <REGION>-<HASH8>} where {@code REGION} is
 * the region code derived from the owner's postcode (see {@link Localities#ofPostcode(String)}) and
 * {@code HASH8} is the first 8 upper-case hex characters of SHA-256 over the owner's normalized
 * telephone concatenated with their last name (e.g. {@code NSW-1A2B3C4D}). There are no sequence
 * numbers: the code is a pure function of postcode, telephone and last name.
 *
 * <p>Wired after {@link NormalizeOwnerTelephone}, so the telephone read here is already in E.164 form
 * and every value derived from the customer code — the membership number and its check digit, the
 * create audit line and the read-model locality — reflects this region-and-hash identity.
 */
public class AssignOwnerCustomerCode {

    /** Length of the hash segment: the first 8 upper-case hex characters of the SHA-256 digest. */
    private static final int HASH_LENGTH = 8;

    public void service(@Val Owner owner) {
        String region = Localities.ofPostcode(owner.getPostcode());
        String hash8 = Hashes.sha256UpperHex(owner.getTelephone() + owner.getLastName(), HASH_LENGTH);
        owner.setCustomerCode(CustomerCode.format(region, hash8));
    }
}
