package org.springframework.samples.petclinic.util;

import java.util.Collection;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Builds an owner's {@code customerCode}, formatted {@code <CITY3>-<LAST3>-<NNNN>} where
 * {@code CITY3} and {@code LAST3} are the upper-cased first three letters of the city and
 * last name, and {@code NNNN} is a per-city 4-digit zero-padded sequence: one more than
 * the owners already in that city (e.g. {@code LON-SMI-0001}).
 */
public final class CustomerCode {

    private CustomerCode() {
    }

    public static String assign(String lastName, String city, Collection<Owner> existingOwners) {
        long cityCount = existingOwners.stream().filter(o -> city.equalsIgnoreCase(o.getCity())).count();
        return String.format("%s-%s-%04d", prefix(city), prefix(lastName), cityCount + 1);
    }

    private static String prefix(String value) {
        return value.substring(0, Math.min(3, value.length())).toUpperCase(Locale.ENGLISH);
    }
}
