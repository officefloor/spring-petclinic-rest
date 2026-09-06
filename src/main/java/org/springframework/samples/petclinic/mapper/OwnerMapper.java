package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Region;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName", expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", expression = "java(owner.getFirstName().substring(0, 1).toUpperCase() + \".\" + owner.getLastName().substring(0, 1).toUpperCase() + \".\")")
    @Mapping(target = "telephoneDisplay", expression = "java(telephoneDisplay(owner))")
    @Mapping(target = "membershipNumber", expression = "java(membershipNumber(owner))")
    @Mapping(target = "checkDigit", expression = "java(checkDigit(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "bulkSignupWarning", ignore = true)
    @Mapping(target = "identityKey", ignore = true)
    @Mapping(target = "possibleDuplicate", ignore = true)
    @Mapping(target = "possibleDuplicateOf", ignore = true)
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Derives the owner's {@code contactPreference} from the owner's own fields:
     * {@code EMAIL} when an email is present, otherwise {@code PHONE}.
     *
     * @param owner the owner being mapped
     * @return the derived contact preference
     */
    default OwnerDto.ContactPreferenceEnum contactPreference(Owner owner) {
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        return hasEmail ? OwnerDto.ContactPreferenceEnum.EMAIL : OwnerDto.ContactPreferenceEnum.PHONE;
    }

    /**
     * Derives the owner's {@code ageBand} from its {@code birthDate}, measured against the owner's
     * {@code registrationDate}: {@code MINOR} when under 18, {@code ADULT} for 18 to 64, and
     * {@code SENIOR} for 65 and over. Returns {@code null} when no birth date is present, in which
     * case the field is absent from the response. The age in whole years is the number of complete
     * years between the birth date and the registration date (see {@link java.time.Period}); when the
     * registration date is somehow absent the current date is used as the reference.
     *
     * @param owner the owner being mapped
     * @return the derived age band, or {@code null} when no birth date is present
     */
    default OwnerDto.AgeBandEnum ageBand(Owner owner) {
        java.time.LocalDate birthDate = owner.getBirthDate();
        if (birthDate == null) {
            return null;
        }
        java.time.LocalDate reference = owner.getRegistrationDate() != null
            ? owner.getRegistrationDate() : java.time.LocalDate.now();
        int age = java.time.Period.between(birthDate, reference).getYears();
        if (age < 18) {
            return OwnerDto.AgeBandEnum.MINOR;
        }
        if (age < 65) {
            return OwnerDto.AgeBandEnum.ADULT;
        }
        return OwnerDto.AgeBandEnum.SENIOR;
    }

    /**
     * Derives the owner's {@code locality} as the {@code REGION} segment of its {@code customerCode}
     * (the part before the {@code '-'}), which is the owner's canonical region code baked into the
     * region-and-hash identity {@code '<REGION>-<HASH8>'}. When no customer code is present (e.g. seed
     * data) it falls back to resolving the region directly from the owner's {@code postcode} and
     * {@code city} through the shared {@link Region#code(String, String)}. Reading the region from the
     * identity keeps the locality consistent with the code an owner is issued.
     *
     * @param owner the owner being mapped
     * @return the derived locality
     */
    default String locality(Owner owner) {
        String code = owner.getCustomerCode();
        if (code == null) {
            return Region.code(owner.getPostcode(), owner.getCity());
        }
        int dash = code.indexOf('-');
        return dash >= 0 ? code.substring(0, dash) : code;
    }

    /**
     * Derives the owner's numeric {@code membershipLevel} from the owner's own fields. Starts at
     * {@code 1}, adds {@code 1} when an email is present, adds {@code 1} when the owner's
     * {@code namesakeCount} is {@code 0}, and is capped at {@code 3} (level {@code 4} is reserved
     * for tenure).
     *
     * @param owner the owner being mapped
     * @return the derived membership level, between {@code 1} and {@code 3}
     */
    default Integer membershipLevel(Owner owner) {
        int level = 1;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        if (hasEmail) {
            level++;
        }
        boolean uniqueName = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        if (uniqueName) {
            level++;
        }
        return Math.min(level, 3);
    }

    /**
     * Builds the owner's {@code membershipNumber}, formatted {@code '<customerCode>-M<YY>'},
     * where {@code YY} is the last two digits of the {@code registrationDate} year (e.g.
     * {@code 'NSW-1A2B3C4D-M26'}). Returns {@code null} when either the customer code or the
     * registration date is absent.
     *
     * @param owner the owner being mapped
     * @return the formatted membership number, or {@code null} when it cannot be derived
     */
    default String membershipNumber(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(), owner.getRegistrationDate().getYear() % 100);
    }

    /**
     * Computes the owner's {@code checkDigit}: a single Luhn check digit (0-9) over the digits
     * contained in the {@code customerCode} (non-digit characters, such as the {@code '<REGION>-'}
     * prefix, the separator and the hex letters {@code A-F}, are ignored). Returns {@code null} when
     * the customer code is absent.
     *
     * @param owner the owner being mapped
     * @return the Luhn check digit, or {@code null} when no customer code is present
     */
    default Integer checkDigit(Owner owner) {
        String code = owner.getCustomerCode();
        if (code == null) {
            return null;
        }
        int sum = 0;
        boolean dbl = true;
        for (int i = code.length() - 1; i >= 0; i--) {
            char c = code.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (dbl) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            dbl = !dbl;
        }
        return (10 - (sum % 10)) % 10;
    }

    /**
     * Formats the owner's stored E.164 {@code telephone} for human display: the country calling
     * code, a space, then the national-significant digits grouped in threes separated by spaces
     * (e.g. {@code '+61412345678'} becomes {@code '+61 412 345 678'}). The raw {@code telephone}
     * field keeps the unformatted E.164 value. The split between the country code and the national
     * digits mirrors the recognised calling codes of the {@code TelephoneNormalizer}: Australian
     * {@code +61} (9 national digits) and North American {@code +1} (10); an unrecognised or
     * malformed value falls back to a single leading digit. Returns the value unchanged when it is
     * {@code null}, blank, or not in E.164 form.
     *
     * @param owner the owner being mapped
     * @return the human-formatted telephone, or the raw value when it cannot be formatted
     */
    default String telephoneDisplay(Owner owner) {
        String telephone = owner.getTelephone();
        if (telephone == null || telephone.isBlank() || !telephone.startsWith("+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        int codeLength = (digits.startsWith("61") && digits.length() == 2 + 9) ? 2 : 1;
        String countryCode = digits.substring(0, codeLength);
        String national = digits.substring(codeLength);
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < national.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                grouped.append(' ');
            }
            grouped.append(national.charAt(i));
        }
        return "+" + countryCode + " " + grouped;
    }

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    Owner toOwner(OwnerFieldsDto ownerDto);

    List<OwnerDto> toOwnerDtoCollection(Collection<Owner> ownerCollection);

    Collection<Owner> toOwners(Collection<OwnerDto> ownerDtos);

    default OwnerPageDto toOwnerPageDto(@NonNull Page<Owner> ownerPage) {
        OwnerPageDto ownerPageDto = new OwnerPageDto();
        ownerPageDto.setContent(toOwnerDtoCollection(ownerPage.getContent()));
        ownerPageDto.setPage(ownerPage.getNumber());
        ownerPageDto.setSize(ownerPage.getSize());
        ownerPageDto.setTotalElements(ownerPage.getTotalElements());
        ownerPageDto.setTotalPages(ownerPage.getTotalPages());
        return ownerPageDto;
    }
}
