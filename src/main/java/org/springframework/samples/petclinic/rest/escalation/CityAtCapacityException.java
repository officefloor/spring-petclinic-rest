package org.springframework.samples.petclinic.rest.escalation;

/**
 * Raised when a create-owner request names a city that already holds the maximum number of
 * owners ({@link org.springframework.samples.petclinic.rest.function.owner.CheckOwnerCityCapacity#CAPACITY}
 * or more), compared case-insensitively with surrounding whitespace trimmed. Carries the
 * requested city and the current count so the handler can report why the city is full.
 */
public class CityAtCapacityException extends Exception {

    private final String city;

    private final int count;

    public CityAtCapacityException(String city, int count) {
        super("City '" + city + "' already has " + count + " owners and is at capacity");
        this.city = city;
        this.count = count;
    }

    public String getCity() {
        return city;
    }

    public int getCount() {
        return count;
    }
}
