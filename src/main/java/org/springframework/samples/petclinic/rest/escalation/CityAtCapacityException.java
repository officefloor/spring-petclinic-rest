package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.CheckCityCapacity}
 * when the owner's city already contains the maximum number of owners (50), so no further owner
 * may be created there. Handled globally by {@link CityAtCapacityExceptionHandler}, which
 * responds 409.
 */
public class CityAtCapacityException extends Exception {

    private final String city;

    private final int count;

    public CityAtCapacityException(String city, int count) {
        super("City is at capacity (" + count + " owners): " + city);
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
