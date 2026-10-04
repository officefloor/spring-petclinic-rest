package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Create-time normalisation for {@code POST /owners}: canonicalises the telephone to E.164 form
 * (see {@link OwnerTelephone#toE164(String)}). The canonical value is written back onto the body
 * in place (the {@code @Val} object is the same instance {@link BuildOwner} then maps), so it is
 * what gets stored and returned. A value that cannot form a valid E.164 number is rejected with
 * 400 via {@link InvalidTelephoneException}.
 *
 * <p>Runs after {@link ValidateOwnerFields} (which has already guaranteed telephone is present
 * and non-blank) and before {@link BuildOwner}.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        String original = request.getTelephone();
        String e164 = OwnerTelephone.toE164(original);
        if (e164 == null) {
            throw new InvalidTelephoneException(original);
        }
        request.setTelephone(e164);
    }
}
