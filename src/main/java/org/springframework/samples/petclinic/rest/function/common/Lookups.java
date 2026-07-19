package org.springframework.samples.petclinic.rest.function.common;

import java.util.function.Supplier;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.samples.petclinic.rest.escalation.NotFoundException;

/**
 * Shared exception-translation for Load functions: the jdbc/jpa/spring-data-jpa repository
 * implementations all signal "not found" by throwing rather than returning null (see
 * ObjectRetrievalFailureException / EmptyResultDataAccessException), so every Load function
 * needs the same translation to NotFoundException.
 */
public final class Lookups {

    private Lookups() {
    }

    public static <T> T findOrNotFound(Supplier<T> supplier, String notFoundMessage) throws NotFoundException {
        try {
            T result = supplier.get();
            if (result == null) {
                throw new NotFoundException(notFoundMessage);
            }
            return result;
        } catch (ObjectRetrievalFailureException | EmptyResultDataAccessException e) {
            throw new NotFoundException(notFoundMessage);
        }
    }
}
