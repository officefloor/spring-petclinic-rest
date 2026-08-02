package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.util.StringUtils;

/**
 * Normalizes a newly created owner's city name. The city is title-cased (each
 * whitespace-separated word gets a leading capital and a lower-cased remainder,
 * e.g. {@code "new york"} becomes {@code "New York"}). However, if an owner already
 * exists in that city - matched case-insensitively - the existing owner's exact
 * spelling of the city name is reused instead, so a city is spelled consistently
 * across all owners regardless of how later creators capitalize it.
 */
public class NormalizeOwnerCity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        if (!StringUtils.hasText(city)) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            String existingCity = existing.getCity();
            if (existingCity != null && existingCity.equalsIgnoreCase(city)) {
                owner.setCity(existingCity);
                return;
            }
        }
        owner.setCity(titleCase(city));
    }

    /**
     * Title-cases each whitespace-separated word: leading character upper-cased,
     * the remainder lower-cased. Preserves the original whitespace between words.
     */
    private static String titleCase(String value) {
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
