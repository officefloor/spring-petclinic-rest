package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.function.common.FiscalYear;
import org.springframework.samples.petclinic.rest.function.common.Hashes;
import org.springframework.samples.petclinic.rest.function.common.IdentityVersion;
import org.springframework.samples.petclinic.rest.function.common.Localities;

/**
 * Assigns the owner's {@code memberId}, formatted {@code <REGION><FY><HASH8><CHK>} where {@code REGION}
 * is the identity version-2 region code — the plain region derived from the owner's postcode (see
 * {@link Localities#ofPostcode(String)}) with the fixed {@link IdentityVersion#TAG} version tag mixed
 * in (e.g. {@code NSWV2}) — {@code FY} is the 2-digit fiscal year of the {@code registrationDate},
 * {@code HASH8} is the first 8 upper-case hex characters of SHA-256 over the owner's normalized
 * telephone concatenated with their last name, and {@code CHK} is a single Luhn check digit over the
 * digits of {@code <REGION><FY><HASH8>} (e.g. {@code NSWV2261A2B3C4D7}). The version tag ensures no
 * member id produced under version 1 is produced again. Apart from that check digit the id is a pure
 * function of postcode, registration date, telephone and last name.
 *
 * <p>The tag is confined to the identifier: the user-facing {@code locality}, {@code timezone} and the
 * owner segment's derived region stay the plain region code (see {@code OwnerMapper}), so the {@code V2}
 * tag never leaks into those user-facing fields.
 *
 * <p>Wired after {@link NormalizeOwnerTelephone} (so the telephone read here is already in E.164 form)
 * and after {@link DefaultOwnerRegistrationDate} (so the fiscal year is known).
 */
public class AssignOwnerMemberId {

    /** Length of the hash segment: the first 8 upper-case hex characters of the SHA-256 digest. */
    private static final int HASH_LENGTH = 8;

    public void service(@Val Owner owner) {
        String region = Localities.ofPostcode(owner.getPostcode()) + IdentityVersion.TAG;
        int fy = FiscalYear.yy(owner.getRegistrationDate());
        String hash8 = Hashes.sha256UpperHex(owner.getTelephone() + owner.getLastName(), HASH_LENGTH);
        owner.setMemberId(MemberId.format(region, fy, hash8));
    }
}
