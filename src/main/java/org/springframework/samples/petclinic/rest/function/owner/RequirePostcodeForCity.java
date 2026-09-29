package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.PostcodeOutOfRangeException;
import org.springframework.samples.petclinic.rest.function.common.Localities;

/**
 * Validates the owner's optional {@code postcode} against the owner's city region. Postcode is checked
 * ONLY WHEN PRESENT: an owner with no postcode passes through untouched, keeping the request contract
 * backward-compatible. Its 4-digit shape is enforced earlier by bean validation ({@code @Pattern} on
 * the DTO, a 400 via {@code ValidationExceptionHandler}); this step adds the region range check.
 *
 * <p>The region is derived from the city via the fixed {@link Localities} table (NSW 2000-2099,
 * VIC 3000-3099, QLD 4000-4099). A city with no known region accepts any 4-digit postcode. A supplied
 * postcode outside its region's range is rejected with a 400 by {@code PostcodeOutOfRangeExceptionHandler}.
 */
public class RequirePostcodeForCity {

    public void service(@Val OwnerFieldsDto owner) throws PostcodeOutOfRangeException {
        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return;
        }
        int[] range = Localities.postcodeRange(owner.getCity());
        if (range == null) {
            return; // city has no known region: any 4-digit postcode is accepted
        }
        int value;
        try {
            value = Integer.parseInt(postcode);
        }
        catch (NumberFormatException ex) {
            throw new PostcodeOutOfRangeException(postcode, owner.getCity());
        }
        if (value < range[0] || value > range[1]) {
            throw new PostcodeOutOfRangeException(postcode, owner.getCity());
        }
    }
}
