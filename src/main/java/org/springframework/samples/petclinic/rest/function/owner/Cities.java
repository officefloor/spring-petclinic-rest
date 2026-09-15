package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Groups owners by city, comparing city names case-insensitively. Used both to size a
 * city's per-city customer-code sequence ({@link AssignCustomerCode}) and to enforce the
 * per-city capacity limit ({@link EnsureCityCapacity}).
 */
final class Cities {

    private Cities() {
    }

    /** How many existing owners live in the given city (case-insensitive match). */
    static long countIn(OwnerRepository repository, String city) {
        String key = cityKey(city);
        long count = 0;
        for (Owner existing : repository.findAll()) {
            if (key.equals(cityKey(existing.getCity()))) {
                count++;
            }
        }
        return count;
    }

    /** Case-folded city name, so trivial casing differences still group together. */
    static String cityKey(String city) {
        return city == null ? "" : city.toLowerCase(Locale.ROOT);
    }
}
