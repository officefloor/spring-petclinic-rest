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
    @Mapping(target = "membershipNumber", expression = "java(membershipNumber(owner))")
    @Mapping(target = "checkDigit", expression = "java(checkDigit(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "bulkSignupWarning", ignore = true)
    @Mapping(target = "identityKey", ignore = true)
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
     * Derives the owner's {@code locality} by preferring the {@code postcode}: it first looks up the
     * region by {@link Region#forPostcode(String) postcode range} (NSW 2000-2099, VIC 3000-3099,
     * QLD 4000-4099), and only falls back to the shared {@link Region#forCity(String) city-to-region}
     * table ({@code Sydney -> NSW}, {@code Melbourne -> VIC}, {@code Brisbane -> QLD}) when the
     * postcode is absent or in no known range. Returns the canonical region code, or {@code UNKNOWN}
     * when neither the postcode nor the city resolves to a known region.
     *
     * @param owner the owner being mapped
     * @return the derived locality
     */
    default String locality(Owner owner) {
        return Region.forPostcode(owner.getPostcode())
            .or(() -> Region.forCity(owner.getCity()))
            .map(Region::name)
            .orElse("UNKNOWN");
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
     * {@code 'MEL-SMI-0007-M26'}). Returns {@code null} when either the customer code or the
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
     * contained in the {@code customerCode} (non-digit characters, such as the {@code '<LAST3>-'}
     * prefix and the separator, are ignored). Returns {@code null} when the customer code is absent.
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
