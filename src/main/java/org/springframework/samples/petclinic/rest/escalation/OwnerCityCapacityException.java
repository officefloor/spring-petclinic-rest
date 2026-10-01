package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.CheckOwnerCityCapacity}
 * when the city of the owner being created already contains the maximum number of owners (50). The
 * city is compared case-insensitively with surrounding whitespace trimmed. Handled by
 * {@link OwnerCityCapacityExceptionHandler}, which responds 409.
 */
public class OwnerCityCapacityException extends Exception {

    private final String city;

    private final int count;

    public OwnerCityCapacityException(String city, int count) {
        super("The city already contains the maximum number of owners: " + city + " (" + count + ")");
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
