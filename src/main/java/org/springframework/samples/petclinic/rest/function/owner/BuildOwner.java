package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

public class BuildOwner {

    public void service(@Val OwnerFieldsDto request, @Val Household.Id householdId,
            OwnerMapper ownerMapper, OwnerRepository ownerRepository, Out<Owner> built) {
        Owner owner = ownerMapper.toOwner(request);
        // The effective registration date defaults an unsupplied value to the server's
        // current date, then rolls any weekend forward to the next Monday so derived values
        // (e.g. the membership number's year) use the adjusted business day.
        owner.setRegistrationDate(BusinessDay.effectiveRegistrationDate(owner.getRegistrationDate()));
        owner.setCustomerCode(customerCode(owner));
        // Record how many existing owners already share this first+last name (case-insensitive),
        // counted before this owner is persisted so the value is fixed at creation time.
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName(), ownerRepository));
        // Join a shared household when the gate resolved an identifier.
        if (householdId != null && householdId.isPresent()) {
            owner.setHouseholdId(householdId.value());
        }
        built.set(owner);
    }

    /**
     * Build the '<REGION>-<HASH8>' customer code: REGION is the region derived from the owner's
     * postcode and city ({@link Owner#regionFor(String, String)}, e.g. 'NSW' or 'UNKNOWN'), and
     * HASH8 is the first 8 upper-case hex characters of the SHA-256 of the normalized telephone
     * followed by the last name (e.g. 'NSW-1A2B3C4D'). There is no sequence number, so the code
     * is stable for an owner regardless of creation order.
     */
    private static String customerCode(Owner owner) {
        String region = Owner.regionFor(owner.getPostcode(), owner.getCity());
        String normalizedTelephone = OwnerTelephone.normalize(owner.getTelephone());
        String seed = (normalizedTelephone == null ? "" : normalizedTelephone) + owner.getLastName();
        return region + "-" + hash8(seed);
    }

    /**
     * The first 8 upper-case hex characters of the SHA-256 of the UTF-8 bytes of {@code seed}.
     */
    private static String hash8(String seed) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(seed.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /**
     * Count the existing owners whose first and last names both match the given names,
     * compared case-insensitively.
     */
    private static int countNamesakes(String firstName, String lastName,
            OwnerRepository ownerRepository) {
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (firstName.equalsIgnoreCase(existing.getFirstName())
                    && lastName.equalsIgnoreCase(existing.getLastName())) {
                count++;
            }
        }
        return count;
    }
}
