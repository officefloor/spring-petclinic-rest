package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

public class BuildOwner {

    public void service(@Val OwnerFieldsDto request, OwnerMapper ownerMapper, Out<Owner> built,
            Out<Boolean> sharesHousehold) throws InvalidTelephoneException {
        Owner owner = ownerMapper.toOwner(request);
        StructuredAddress.apply(owner);
        owner.setTelephone(toE164(owner.getTelephone()));
        java.time.LocalDate effective = owner.getRegistrationDate() == null
                ? java.time.LocalDate.now() : owner.getRegistrationDate();
        owner.setRegistrationDate(BusinessDay.roll(effective));
        sharesHousehold.set(Boolean.TRUE.equals(request.getSharesHousehold()));
        built.set(owner);
    }

    /**
     * Normalizes a telephone to E.164: strip spaces, dashes and brackets; keep a leading '+' with its
     * country code, otherwise assume '+61' and drop a single leading '0' from the national digits.
     * Requires 8 to 15 digits after the '+'.
     */
    private static String toE164(String raw) throws InvalidTelephoneException {
        String cleaned = (raw == null ? "" : raw).replaceAll("[\\s\\-()\\[\\]]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        } else {
            digits = "61" + (cleaned.startsWith("0") ? cleaned.substring(1) : cleaned);
        }
        if (!digits.matches("\\d{8,15}") || !hasValidNationalLength(digits)) {
            throw new InvalidTelephoneException(raw);
        }
        return "+" + digits;
    }

    /**
     * Checks the national-number length for the recognised country codes: '+61' requires 9 national
     * digits and '+1' requires 10. Other country codes have no per-country rule here.
     */
    private static boolean hasValidNationalLength(String digits) {
        if (digits.startsWith("61")) {
            return digits.length() - 2 == 9;
        }
        if (digits.startsWith("1")) {
            return digits.length() - 1 == 10;
        }
        return true;
    }
}
