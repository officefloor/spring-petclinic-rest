package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Set;
import java.util.stream.Collectors;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's memberId as {@code <REGION><FY><HASH8><CHK>}: REGION is the region code
 * derived from the postcode (see {@link CityLocality}), FY is the two-digit fiscal year of the
 * registrationDate (see {@link FiscalYear}), HASH8 is the first eight upper-case hex characters
 * of SHA-256 over the normalized telephone followed by the last name, and CHK is a single Luhn
 * check digit over the digits of {@code <REGION><FY><HASH8>} (see {@link CheckDigit}). On a
 * collision with an existing owner's memberId, {@code -<n>} (smallest n of 2 or more) is appended
 * so distinct owners always get distinct ids.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = "V2" + CityLocality.of(owner.getCity(), owner.getPostcode());
        String prefix = region + String.format("%02d", FiscalYear.shortYear(owner.getRegistrationDate()))
                + hash8(owner.getTelephone() + owner.getLastName());
        String base = prefix + CheckDigit.of(prefix);
        Set<String> taken = ownerRepository.findAll().stream()
                .map(Owner::getMemberId).collect(Collectors.toSet());
        String memberId = base;
        for (int n = 2; taken.contains(memberId); n++) {
            memberId = base + "-" + n;
        }
        owner.setMemberId(memberId);
    }

    private static String hash8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 8).toUpperCase();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
