package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link RejectCityAtCapacity} when a create-owner request names a city that already
 * contains 50 or more owners. Handled globally by {@code CityAtCapacityExceptionHandler}, which
 * responds 409.
 */
public class CityAtCapacityException extends Exception {

    public CityAtCapacityException(String city, int limit) {
        super("City '" + city + "' already has " + limit + " or more owners and cannot accept another.");
    }
}
