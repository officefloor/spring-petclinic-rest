package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags whether the new owner shares a household with an existing owner, storing the
 * result on the owner as {@code sharesHousehold}. An owner shares a household when, at
 * the moment of creation, another owner already has the same address and city. Runs
 * before the owner is saved, so only owners that existed beforehand are considered.
 *
 * <p>Address and city matching is insensitive to letter case and to surrounding
 * whitespace (see {@link #normalize(String)}). Creation is always allowed; this step
 * only records the flag.
 */
public class DetermineSharesHousehold {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String address = normalize(owner.getAddress());
        String city = normalize(owner.getCity());
        boolean shares = false;
        for (Owner existing : ownerRepository.findAll()) {
            if (Objects.equals(existing.getId(), owner.getId())) {
                continue;
            }
            if (Objects.equals(normalize(existing.getAddress()), address)
                    && Objects.equals(normalize(existing.getCity()), city)) {
                shares = true;
                break;
            }
        }
        owner.setSharesHousehold(shares);
    }

    /**
     * Normalizes a value for household comparison: {@code null} becomes empty, letter
     * case is folded to lower case, surrounding whitespace is trimmed and any run of
     * internal whitespace is collapsed to a single space.
     */
    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
