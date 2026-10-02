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
package org.springframework.samples.petclinic.service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.model.Visit;

/**
 * Mostly used as a facade so all controllers have a single point of entry
 *
 * @author Michael Isvy
 * @author Vitaliy Fedoriv
 */
public interface ClinicService {

	Pet findPetById(int id) throws DataAccessException;
	Collection<Pet> findAllPets() throws DataAccessException;
    Page<Pet> findPets(Pageable pageable) throws DataAccessException;
	void savePet(Pet pet) throws DataAccessException;
	void deletePet(Pet pet) throws DataAccessException;

	Collection<Visit> findVisitsByPetId(int petId);
	Visit findVisitById(int visitId) throws DataAccessException;
	Collection<Visit> findAllVisits() throws DataAccessException;
	void saveVisit(Visit visit) throws DataAccessException;
	void deleteVisit(Visit visit) throws DataAccessException;
	Vet findVetById(int id) throws DataAccessException;
	Collection<Vet> findVets() throws DataAccessException;
	Collection<Vet> findAllVets() throws DataAccessException;
	void saveVet(Vet vet) throws DataAccessException;
	void deleteVet(Vet vet) throws DataAccessException;
	Owner findOwnerById(int id) throws DataAccessException;
	Collection<Owner> findAllOwners() throws DataAccessException;
	Page<Owner> findOwners(String lastName, Pageable pageable) throws DataAccessException;
	void saveOwner(Owner owner) throws DataAccessException;

	/**
	 * Saves the given owner, additionally recording whether it was created as a <em>declared</em>
	 * household member (via {@code sharesHousehold}). A declared member is explicitly not treated as a
	 * suspected duplicate of the existing household it joins, so its {@code possibleDuplicate} flag is
	 * left clear; otherwise duplicate detection runs as normal. Equivalent to
	 * {@link #saveOwner(Owner)} with {@code declaredHouseholdMember = false}.
	 *
	 * @param owner the owner to save
	 * @param declaredHouseholdMember whether the owner declared itself a member of an existing household
	 */
	void saveOwner(Owner owner, boolean declaredHouseholdMember) throws DataAccessException;

	void deleteOwner(Owner owner) throws DataAccessException;

	/**
	 * Returns {@code true} if the given owner's derived identity key equals that of another
	 * non-deleted owner, i.e. the create would introduce a duplicate. The identity key
	 * ({@link Owner#getIdentityKey()}) is the full lower-case hexadecimal SHA-256 digest over
	 * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)} and is the single,
	 * consolidated criterion for duplicate detection: only an exact whole-key match counts. Because
	 * the telephone is part of the key, two owners with the same last name and postcode but different
	 * telephones have distinct keys and are both allowed. Enforces the "no duplicate identity key"
	 * invariant on owner creation, ignoring owners flagged deleted.
	 *
	 * @param owner the (not yet persisted) owner whose identity key to check
	 * @return whether an existing owner already has the same identity key
	 */
	boolean isIdentityKeyInUse(Owner owner) throws DataAccessException;

	/**
	 * Returns a deterministic, shared household identifier derived from the given last name and
	 * postcode: the first 12 hexadecimal characters of the SHA-256 digest over
	 * {@code normalizedLastName + '|' + postcode}. The last name is normalized (trimmed, runs of
	 * whitespace collapsed, lower-cased) before hashing, so every owner with the same last name and
	 * postcode deterministically receives the identical value and therefore shares a household
	 * automatically. The household id drives the household-derived fields but is no longer part of
	 * the identity key used for duplicate detection.
	 *
	 * @param lastName the last name of the household
	 * @param postcode the postcode of the household
	 * @return a stable identifier shared by all owners of the household
	 */
	String householdIdFor(String lastName, String postcode) throws DataAccessException;

	/**
	 * Returns {@code true} if the given city has already reached its owner capacity, i.e. it already
	 * contains 50 or more owners (compared case-insensitively, the same way the per-city customer-code
	 * sequence counts them). Enforces the "at most 50 owners per city" invariant on owner creation.
	 *
	 * @param city the city to check
	 * @return whether the city already contains 50 or more owners
	 */
	boolean isCityAtCapacity(String city) throws DataAccessException;

	/**
	 * Returns {@code true} if 100 or more owners have already been registered today, i.e. the number
	 * of owners whose {@code registrationDate} equals the server's current date has reached the daily
	 * create limit. Enforces the "at most 100 owners created per day" invariant on owner creation.
	 *
	 * @return whether 100 or more owners already carry today's registration date
	 */
	boolean isDailyRegistrationAtCapacity() throws DataAccessException;

	/**
	 * Returns {@code true} when more than 80 owners have already been created today, i.e. the
	 * number of owners whose {@code registrationDate} equals the server's current date exceeds
	 * the bulk-signup threshold. Drives the read-only {@code bulkSignupWarning} response flag.
	 *
	 * @return whether more than 80 owners already carry today's registration date
	 */
	boolean isBulkSignupDay() throws DataAccessException;

	Collection<Owner> findOwnerByLastName(String lastName) throws DataAccessException;

	PetType findPetTypeById(int petTypeId);
	Collection<PetType> findAllPetTypes() throws DataAccessException;
	Collection<PetType> findPetTypes() throws DataAccessException;
	void savePetType(PetType petType) throws DataAccessException;
	void deletePetType(PetType petType) throws DataAccessException;
	Specialty findSpecialtyById(int specialtyId);
	Collection<Specialty> findAllSpecialties() throws DataAccessException;
	void saveSpecialty(Specialty specialty) throws DataAccessException;
	void deleteSpecialty(Specialty specialty) throws DataAccessException;

    List<Specialty> findSpecialtiesByNameIn(Set<String> names) throws DataAccessException;
}
