package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Normalises the city of a newly created owner. The city is title-cased (e.g.
 * {@code "san francisco"} becomes {@code "San Francisco"}) so the value stored and
 * returned is consistently capitalised. However, if any owner already lives in that
 * city (matched case- and whitespace-insensitively), that owner's exact spelling of the
 * city name is reused instead, so a city is spelt the same way across all its owners.
 */
public class NormalizeCity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        if (city == null) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue;
            }
            if (DuplicateMatching.matches(existing.getCity(), city)) {
                owner.setCity(existing.getCity());
                return;
            }
        }
        owner.setCity(titleCase(city));
    }

    /**
     * Title-cases a city name: the first letter of each word is upper-cased and the rest
     * lower-cased, where words are separated by any non-letter character (so both
     * {@code "new york"} and {@code "winston-salem"} are handled). Surrounding and
     * repeated whitespace is collapsed. {@code null} stays {@code null}.
     */
    static String titleCase(String city) {
        if (city == null) {
            return null;
        }
        String collapsed = city.trim().replaceAll("\\s+", " ");
        StringBuilder result = new StringBuilder(collapsed.length());
        boolean startOfWord = true;
        for (int i = 0; i < collapsed.length(); i++) {
            char c = collapsed.charAt(i);
            if (Character.isLetter(c)) {
                result.append(startOfWord ? Character.toTitleCase(c) : Character.toLowerCase(c));
                startOfWord = false;
            } else {
                result.append(c);
                startOfWord = true;
            }
        }
        return result.toString();
    }
}
