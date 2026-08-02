package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Title-cases the new owner's city (e.g. {@code "riverdale"} → {@code "Riverdale"}). If an owner
 * already lives in that city (ignoring case), the existing owner's exact spelling is reused instead
 * so a city has a single canonical spelling. Runs before {@link SaveOwner} so the stored and
 * returned city is the normalized value.
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
                result.append(Character.toString(c).toUpperCase(Locale.ROOT));
                startOfWord = false;
            }
            else {
                result.append(Character.toString(c).toLowerCase(Locale.ROOT));
            }
        }
        return result.toString();
    }
}
