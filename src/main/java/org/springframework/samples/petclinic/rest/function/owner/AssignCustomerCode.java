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
 * Assigns the owner's customerCode as {@code <REGION>-<HASH8>}: REGION is the region code
 * derived from the postcode (see {@link CityLocality}) and HASH8 is the first eight
 * upper-case hex characters of SHA-256 over the normalized telephone followed by the last
 * name. On a collision with an existing owner's code, {@code -<n>} (smallest n of 2 or more)
 * is appended so distinct owners always get distinct codes.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = CityLocality.of(owner.getCity(), owner.getPostcode());
        String base = region + "-" + hash8(owner.getTelephone() + owner.getLastName());
        Set<String> taken = ownerRepository.findAll().stream()
                .map(Owner::getCustomerCode).collect(Collectors.toSet());
        String code = base;
        for (int n = 2; taken.contains(code); n++) {
            code = base + "-" + n;
        }
        owner.setCustomerCode(code);
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
