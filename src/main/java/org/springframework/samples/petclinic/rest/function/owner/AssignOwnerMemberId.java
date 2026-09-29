package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.function.common.FiscalYear;
import org.springframework.samples.petclinic.rest.function.common.Hashes;
import org.springframework.samples.petclinic.rest.function.common.Localities;

/**
 * Assigns the owner's {@code memberId}, formatted {@code <REGION><FY><HASH8><CHK>} where {@code REGION}
 * is the region code derived from the owner's postcode (see {@link Localities#ofPostcode(String)}),
 * {@code FY} is the 2-digit fiscal year of the {@code registrationDate}, {@code HASH8} is the first 8
 * upper-case hex characters of SHA-256 over the owner's normalized telephone concatenated with their
 * last name, and {@code CHK} is a single Luhn check digit over the digits of {@code <REGION><FY><HASH8>}
 * (e.g. {@code NSW261A2B3C4D7}). Apart from that check digit the id is a pure function of postcode,
 * registration date, telephone and last name.
 *
 * <p>Wired after {@link NormalizeOwnerTelephone} (so the telephone read here is already in E.164 form)
 * and after {@link DefaultOwnerRegistrationDate} (so the fiscal year is known), and every value derived
 * from the member id — the create audit line and the read-model locality and owner segment — reflects
 * this region-and-hash identity.
 */
public class AssignOwnerMemberId {

    /** Length of the hash segment: the first 8 upper-case hex characters of the SHA-256 digest. */
    private static final int HASH_LENGTH = 8;

    public void service(@Val Owner owner) {
        String region = Localities.ofPostcode(owner.getPostcode());
        int fy = FiscalYear.yy(owner.getRegistrationDate());
        String hash8 = Hashes.sha256UpperHex(owner.getTelephone() + owner.getLastName(), HASH_LENGTH);
        owner.setMemberId(MemberId.format(region, fy, hash8));
    }
}
