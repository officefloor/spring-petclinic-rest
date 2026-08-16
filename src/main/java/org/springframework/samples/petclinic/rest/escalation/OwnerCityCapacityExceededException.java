package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create-owner pipeline when the owner's {@code city} already contains 50 or
 * more owners (compared case-insensitively, matching the per-city sequence rule). The city
 * is full, so no further owner may be created there.
 *
 * <p>Carries the offending city so
 * {@link OwnerCityCapacityExceededExceptionHandler} can respond 409 explaining why the
 * create was rejected.
 */
public class OwnerCityCapacityExceededException extends Exception {

    private final String city;

    public OwnerCityCapacityExceededException(String city) {
        super("The city '" + city + "' already has 50 or more owners");
        this.city = city;
    }

    public String getCity() {
        return city;
    }
}
