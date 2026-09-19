/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.repository;

import java.time.LocalDate;
import java.util.Collection;

import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.model.BaseEntity;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Repository class for <code>Owner</code> domain objects All method names are compliant with Spring Data naming
 * conventions so this interface can easily be extended for Spring Data See here: http://static.springsource.org/spring-data/jpa/docs/current/reference/html/jpa.repositories.html#jpa.query-methods.query-creation
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Vitaliy Fedoriv
 */
public interface OwnerRepository {

    /**
     * Retrieve <code>Owner</code>s from the data store by last name, returning all owners whose last name <i>starts</i>
     * with the given name.
     *
     * @param lastName Value to search for
     * @return a <code>Collection</code> of matching <code>Owner</code>s (or an empty <code>Collection</code> if none
     * found)
     */
    Collection<Owner> findByLastName(String lastName) throws DataAccessException;

    Page<Owner> findByLastName(String lastName, Pageable pageable) throws DataAccessException;

    /**
     * Retrieve every <code>Owner</code> whose last name matches the given value ignoring case. Unlike
     * {@link #findByLastName(String)} this is an exact (not prefix) match and is used to gather the
     * candidates for the household-duplicate check.
     *
     * @param lastName the last name to match, case-insensitively
     * @return a <code>Collection</code> of matching <code>Owner</code>s (or an empty <code>Collection</code> if none
     * found)
     */
    Collection<Owner> findByLastNameIgnoreCase(String lastName) throws DataAccessException;

    /**
     * Retrieve an <code>Owner</code> from the data store by id.
     *
     * @param id the id to search for
     * @return the <code>Owner</code> if found
     * @throws org.springframework.dao.DataRetrievalFailureException if not found
     */
    Owner findById(int id) throws DataAccessException;

    /**
     * Report whether any <code>Owner</code> already stores the given (normalized) telephone number.
     *
     * @param telephone the normalized telephone digits to look for
     * @return <code>true</code> if an owner with that telephone exists, <code>false</code> otherwise
     */
    boolean existsByTelephone(String telephone) throws DataAccessException;

    /**
     * Report whether any <code>Owner</code> already stores the given email, compared case-insensitively.
     * Used to reject creating an owner whose lower-cased email is already used by another owner.
     *
     * @param email the email to look for, matched ignoring case
     * @return <code>true</code> if an owner with that email exists, <code>false</code> otherwise
     */
    boolean existsByEmailIgnoreCase(String email) throws DataAccessException;

    /**
     * Count the total number of <code>Owner</code>s currently in the data store.
     *
     * @return the number of stored owners
     */
    long count() throws DataAccessException;

    /**
     * Count the <code>Owner</code>s already registered in the given city, compared case-insensitively.
     * Used to derive the per-city sequence number embedded in an owner's customer code.
     *
     * @param city the city to count owners for
     * @return the number of stored owners in that city
     */
    long countByCity(String city) throws DataAccessException;

    /**
     * Count the <code>Owner</code>s already registered on the given date. Used to enforce the maximum
     * number of owner registrations allowed per day.
     *
     * @param registrationDate the registration date to count owners for
     * @return the number of stored owners registered on that date
     */
    long countByRegistrationDate(LocalDate registrationDate) throws DataAccessException;


    /**
     * Save an <code>Owner</code> to the data store, either inserting or updating it.
     *
     * @param owner the <code>Owner</code> to save
     * @see BaseEntity#isNew
     */
    void save(Owner owner) throws DataAccessException;
    
    /**
     * Retrieve <code>Owner</code>s from the data store, returning all owners 
     *
     * @return a <code>Collection</code> of <code>Owner</code>s (or an empty <code>Collection</code> if none
     * found)
     */
	Collection<Owner> findAll() throws DataAccessException;

    Page<Owner> findAll(Pageable pageable) throws DataAccessException;
	
    /**
     * Delete an <code>Owner</code> to the data store by <code>Owner</code>.
     *
     * @param owner the <code>Owner</code> to delete
     * 
     */
	void delete(Owner owner) throws DataAccessException;


}
