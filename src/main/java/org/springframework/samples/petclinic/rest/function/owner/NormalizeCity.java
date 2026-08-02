package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Canonicalizes the city of a newly created owner.
 *
 * <p>The city is title-cased (each whitespace-separated word capitalized, the rest of
 * the word lower-cased) so {@code "riverdale"} becomes {@code "Riverdale"}. However, if
 * an owner already exists in that city — matched case-insensitively — the existing
 * owner's exact spelling of the city name is reused instead, so a city is spelled
 * consistently across all owners.
 */
public class NormalizeCity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        if (city == null) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getCity() != null && existing.getCity().equalsIgnoreCase(city)) {
                owner.setCity(existing.getCity());
                return;
            }
        }
        owner.setCity(titleCase(city));
    }

    /**
     * Title-cases a value: each whitespace-separated word has its first letter
     * upper-cased and its remaining letters lower-cased.
     */
    static String titleCase(String value) {
        StringBuilder result = new StringBuilder(value.length());
        boolean startOfWord = true;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isWhitespace(c)) {
                startOfWord = true;
                result.append(c);
            }
            else if (startOfWord) {
                result.append(Character.toUpperCase(c));
                startOfWord = false;
            }
            else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }
}
