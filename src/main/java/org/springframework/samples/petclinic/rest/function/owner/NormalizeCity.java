package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Normalises the city of a newly built owner. The city is title-cased (each
 * whitespace-separated word starts with an upper-case letter and continues in
 * lower case), unless an owner already exists in that city (ignoring letter
 * case), in which case that existing owner's exact spelling is reused so a city
 * is spelt consistently across all its owners.
 */
public class NormalizeCity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        if (city == null) {
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
