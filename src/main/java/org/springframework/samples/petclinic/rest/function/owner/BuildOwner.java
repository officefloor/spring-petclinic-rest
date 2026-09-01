package org.springframework.samples.petclinic.rest.function.owner;

import jakarta.validation.Valid;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

@Validated
public class BuildOwner {

    public void service(@Valid @RequestBody OwnerFieldsDto request, OwnerMapper ownerMapper, Out<Owner> built,
            Out<Boolean> sharesHousehold) throws InvalidTelephoneException {
        Owner owner = ownerMapper.toOwner(request);
        owner.setTelephone(toE164(owner.getTelephone()));
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(java.time.LocalDate.now());
        }
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
        if (!digits.matches("\\d{8,15}")) {
            throw new InvalidTelephoneException(raw);
        }
        return "+" + digits;
    }
}
