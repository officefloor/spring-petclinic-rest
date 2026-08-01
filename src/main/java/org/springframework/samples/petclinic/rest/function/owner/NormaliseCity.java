package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Normalises the city of a newly built owner. The city is title-cased (each word
 * capitalised, remaining letters lower-cased) so that varied input casing is stored
 * consistently. However, if an owner already exists in the same city (compared
 * ignoring case and surrounding/repeated whitespace), that existing owner's exact
 * spelling of the city is reused instead, keeping a single canonical spelling per
 * city.
 */
public class NormaliseCity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        String key = DuplicateKey.normalize(city);
        if (key == null) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (key.equals(DuplicateKey.normalize(existing.getCity()))) {
                owner.setCity(existing.getCity());
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
                result.append(Character.toString(c).toLowerCase(Locale.ROOT));
            }
        }
        return result.toString();
    }
}
