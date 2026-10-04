package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link CheckOwnerCityCapacity} when an owner create request targets a city that
 * already contains 50 or more owners. Checked so it appears in the {@code throws} clause and is
 * routed to its escalation handler, which responds 409.
 */
public class CityAtCapacityException extends Exception {

    public CityAtCapacityException(String city) {
        super("The city is at capacity and cannot accept more owners: " + city);
    }
}
