package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link EnsureCityCapacity} when a create-owner request names a city that
 * already contains the maximum number of owners. Handled by
 * {@code CityAtCapacityExceptionHandler}, which responds 409.
 */
public class CityAtCapacityException extends Exception {

    public CityAtCapacityException(String city, int capacity) {
        super("City '" + city + "' already contains its maximum of " + capacity + " owners");
    }
}
