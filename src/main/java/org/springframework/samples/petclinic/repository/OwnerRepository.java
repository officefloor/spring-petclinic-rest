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
     * Retrieve an <code>Owner</code> from the data store by id.
     *
     * @param id the id to search for
     * @return the <code>Owner</code> if found
     * @throws org.springframework.dao.DataRetrievalFailureException if not found
     */
    Owner findById(int id) throws DataAccessException;

    /**
     * Check whether an <code>Owner</code> with the given (already normalized) telephone exists in the data store.
     *
     * @param telephone the normalized telephone to search for
     * @return <code>true</code> if at least one owner already uses this telephone
     */
    boolean existsByTelephone(String telephone) throws DataAccessException;

    /**
     * Retrieve every <code>Owner</code> whose last name equals the given value, ignoring
     * case. Used to narrow the candidates for the duplicate-household check before the
     * address is compared.
     *
     * @param lastName the last name to match (case-insensitively)
     * @return a <code>Collection</code> of matching <code>Owner</code>s (or an empty
     * <code>Collection</code> if none found)
     */
    Collection<Owner> findByLastNameIgnoreCase(String lastName) throws DataAccessException;


    /**
     * Save an <code>Owner</code> to the data store, either inserting or updating it.
     *
     * @param owner the <code>Owner</code> to save
     * @see BaseEntity#isNew
     */
    void save(Owner owner) throws DataAccessException;

    /**
     * Count all <code>Owner</code>s currently held in the data store.
     *
     * @return the total number of owners
     */
    long count() throws DataAccessException;

    /**
     * Count the <code>Owner</code>s already registered in the given city, compared
     * case-insensitively. Used to assign the per-city sequence in a new owner's
     * customer code.
     *
     * @param city the city to match (case-insensitively)
     * @return the number of owners already living in that city
     */
    long countByCity(String city) throws DataAccessException;

    /**
     * Count the <code>Owner</code>s registered on the given date. Used to enforce the
     * daily cap on how many owners may be created in a single day.
     *
     * @param registrationDate the registration date to match
     * @return the number of owners registered on that date
     */
    long countByRegistrationDate(LocalDate registrationDate) throws DataAccessException;

    /**
     * Count the <code>Owner</code>s that already belong to the given household, i.e. that
     * carry the given shared household id. Used to size a new owner's household when
     * assigning its membership tier.
     *
     * @param householdId the shared household id to match
     * @return the number of owners already sharing that household id
     */
    long countByHouseholdId(String householdId) throws DataAccessException;

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
