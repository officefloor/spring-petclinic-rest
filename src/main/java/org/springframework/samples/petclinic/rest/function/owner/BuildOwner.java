package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

@Validated
public class BuildOwner {

    /** Simple syntactic email check: non-space local part, '@', non-space domain with a dot. */
    private static final java.util.regex.Pattern EMAIL =
            java.util.regex.Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public void service(@Valid @RequestBody OwnerFieldsDto request, OwnerMapper ownerMapper,
            OwnerRepository ownerRepository, Out<Owner> built, Out<Boolean> sharesHousehold)
            throws MissingOwnerFieldsException, InvalidTelephoneException, InvalidEmailException {
        List<String> missing = new ArrayList<>();
        // Normalize the address up front so the required-field check rejects an address that is
        // blank once trimmed/collapsed, and every later step (and the stored value) sees the
        // canonical form.
        String normalizedAddress = AddressNormalizer.normalize(request.getAddress());
        if (isBlank(request.getFirstName())) {
            missing.add("firstName");
        }
        if (isBlank(request.getLastName())) {
            missing.add("lastName");
        }
        if (normalizedAddress.isEmpty()) {
            missing.add("address");
        }
        if (isBlank(request.getCity())) {
            missing.add("city");
        }
        if (isBlank(request.getTelephone())) {
            missing.add("telephone");
        }
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
        request.setTelephone(TelephoneNormalizer.normalize(request.getTelephone()));
        request.setAddress(normalizedAddress);
        request.setEmail(normalizeEmail(request.getEmail()));
        Owner owner = ownerMapper.toOwner(request);
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(java.time.LocalDate.now());
        }
        owner.setCustomerCode(nextCustomerCode(owner.getCity(), owner.getLastName(), ownerRepository));
        built.set(owner);
        sharesHousehold.set(Boolean.TRUE.equals(request.getSharesHousehold()));
    }

    /**
     * Build a customer code '<CITY3>-<LAST3>-<NNNN>': the upper-cased first three letters of the
     * city, a hyphen, the upper-cased first three letters of the last name, a hyphen, then a
     * per-city 4-digit zero-padded sequence equal to one more than the number of owners already
     * in that city (e.g. 'LON-SMI-0007').
     */
    private static String nextCustomerCode(String city, String lastName, OwnerRepository ownerRepository) {
        String city3 = prefix3(city);
        String last3 = prefix3(lastName);
        int sequence = 1;
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getCity() != null && existing.getCity().equalsIgnoreCase(city)) {
                sequence++;
            }
        }
        return String.format("%s-%s-%04d", city3, last3, sequence);
    }

    /** Upper-cased first three letters (fewer if the value is shorter) of {@code value}. */
    private static String prefix3(String value) {
        return value.substring(0, Math.min(3, value.length())).toUpperCase(java.util.Locale.ROOT);
    }

    /** When present, require a syntactically valid address and store it lower-cased; else reject with 400. */
    private static String normalizeEmail(String email) throws InvalidEmailException {
        if (email == null || email.isBlank()) {
            return null;
        }
        String trimmed = email.trim();
        if (!EMAIL.matcher(trimmed).matches()) {
            throw new InvalidEmailException(email);
        }
        return trimmed.toLowerCase(java.util.Locale.ROOT);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
