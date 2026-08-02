package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Normalises the owner's city on creation: it is title-cased (each whitespace-separated
 * word capitalised, the rest lower-cased). However, when an owner already exists in that
 * city&mdash;ignoring letter case and surrounding or repeated whitespace&mdash;that
 * existing owner's exact spelling is reused instead, so a city is spelt consistently
 * across all its owners.
 *
 * <p>Runs before {@link AssignCustomerCode}, so the per-city customer code sequence is
 * counted against a single, consistent spelling of the city.
 */
public class NormalizeCity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        if (city == null || city.isBlank()) {
            return;
        }
        String key = matchKey(city);
        for (Owner existing : ownerRepository.findAll()) {
            if (isDifferentOwner(existing, owner)) {
                String existingCity = existing.getCity();
                if (existingCity != null && key.equals(matchKey(existingCity))) {
                    owner.setCity(existingCity);
                    return;
                }
            }
        }
        owner.setCity(titleCase(city));
    }

    private static boolean isDifferentOwner(Owner existing, Owner candidate) {
        return existing.getId() == null || !existing.getId().equals(candidate.getId());
    }

    /** Capitalises the first letter of each whitespace-separated word, lower-casing the rest. */
    private static String titleCase(String value) {
        StringBuilder sb = new StringBuilder();
        for (String word : value.trim().split("\\s+")) {
            if (word.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1).toLowerCase(Locale.ROOT));
        }
        return sb.toString();
    }

    /** Case- and whitespace-insensitive key used to match owners in the same city. */
    private static String matchKey(String value) {
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
