package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Canonicalizes a newly created owner's city. By default the city is title-cased (each
 * word capitalized, e.g. {@code "riverdale"} → {@code "Riverdale"}). However, when an
 * owner already exists in that city (matched case-insensitively), that existing owner's
 * exact spelling of the city name is reused instead, so every owner in a city shares one
 * canonical spelling.
 */
public class NormalizeOwnerCity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        if (city == null) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (isSameOwner(owner, existing)) {
                continue;
            }
            String existingCity = existing.getCity();
            if (existingCity != null && existingCity.equalsIgnoreCase(city)) {
                owner.setCity(existingCity);
                return;
            }
        }
        owner.setCity(titleCase(city));
    }

    private static boolean isSameOwner(Owner a, Owner b) {
        return a.getId() != null && Objects.equals(a.getId(), b.getId());
    }

    /**
     * Title-cases a value: the first letter of each whitespace-separated word is
     * uppercased and the remaining letters are lowercased.
     */
    private static String titleCase(String value) {
        StringBuilder result = new StringBuilder(value.length());
        boolean startOfWord = true;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isWhitespace(c)) {
                startOfWord = true;
                result.append(c);
            } else if (startOfWord) {
                result.append(Character.toUpperCase(c));
                startOfWord = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }
}
