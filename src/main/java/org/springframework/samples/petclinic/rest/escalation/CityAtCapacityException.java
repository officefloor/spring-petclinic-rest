package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request targets a city that already contains the maximum number of
 * owners (50). The city is compared case-insensitively with surrounding whitespace trimmed, so
 * cosmetic differences do not evade the cap. Handled globally by
 * {@link CityAtCapacityExceptionHandler}, which responds 409.
 */
public class CityAtCapacityException extends Exception {

    /** A city is full once it holds this many owners; the next owner in it is rejected. */
    public static final int CAPACITY = 50;

    private final String city;

    private final int count;

    public CityAtCapacityException(String city, int count) {
        super("City '" + city + "' already has " + count + " owners (maximum " + CAPACITY + ")");
        this.city = city;
        this.count = count;
    }

    public String getCity() {
        return this.city;
    }

    public int getCount() {
        return this.count;
    }
}
