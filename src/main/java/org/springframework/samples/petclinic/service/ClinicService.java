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

import java.time.LocalDate;
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
	void deleteOwner(Owner owner) throws DataAccessException;
	Collection<Owner> findOwnerByLastName(String lastName) throws DataAccessException;

	/**
	 * Returns {@code true} if any existing owner already belongs to the given candidate's
	 * household, i.e. carries the same {@code householdId}. Because the household id is derived
	 * deterministically from the owner's (normalized) last name and postcode, this is equivalent
	 * to another owner already sharing the candidate's last name and postcode. The
	 * {@code householdId} is derived on the owner itself (see {@link Owner#getHouseholdId()}), so
	 * no assignment step is needed. Callers can reject a duplicate create unless the household is
	 * shared intentionally.
	 *
	 * @param owner the candidate owner, with its {@code householdId} already set
	 */
	boolean isHouseholdInUse(Owner owner) throws DataAccessException;

	/**
	 * Returns the existing (non-deleted, persisted) owners that belong to the given candidate's
	 * household, i.e. carry the same {@code householdId} (see {@link Owner#getHouseholdId()}). The
	 * candidate itself is not included, since it has not been persisted yet. Used to derive the
	 * candidate's membership-level ceiling: a new owner's level may not exceed one above the current
	 * maximum level among these members. An empty collection means the candidate is the first member
	 * of its household, so no cap applies.
	 *
	 * @param owner the candidate owner, whose {@code householdId} is derived from its own fields
	 */
	Collection<Owner> findHouseholdMembers(Owner owner) throws DataAccessException;

	/**
	 * Returns {@code true} if any existing owner already has the same identity key as the given
	 * candidate, where the identity key is the owner's normalized telephone, (lower-cased) email
	 * or empty string, and household id or empty string, joined with {@code '|'}. This is the
	 * single consolidated duplicate check: a create is a duplicate only when the candidate's WHOLE
	 * identity key matches an existing owner's, so two household members with different telephones
	 * (hence different keys) are both allowed.
	 *
	 * @param owner the candidate owner, with its telephone, email and household id already set
	 */
	boolean isIdentityKeyInUse(Owner owner) throws DataAccessException;

	/**
	 * Returns the id of an existing owner that is a soft "possible duplicate" of the given
	 * candidate — one that shares the candidate's last name (compared case-insensitively) and
	 * postcode but carries a different telephone — or {@code null} when none exists. Unlike
	 * {@link #isIdentityKeyInUse(Owner)}, which detects a hard duplicate that blocks the create,
	 * this flags a near-match that is still allowed to be created. When several owners match, the
	 * one with the lowest id is returned so the result is deterministic.
	 *
	 * @param owner the candidate owner, with its last name, postcode and telephone already set
	 * @return the matching owner's id, or {@code null} when none exists
	 */
	Integer findPossibleDuplicateOwnerId(Owner owner) throws DataAccessException;

	/**
	 * Returns the number of existing owners that share the given first name and last name,
	 * compared case-insensitively. Used on create to record how many namesakes an owner already
	 * had at the moment it was created.
	 *
	 * @param firstName the first name to match
	 * @param lastName  the last name to match
	 * @return the count of existing owners with the same first and last name
	 */
	int countNamesakes(String firstName, String lastName) throws DataAccessException;

	/**
	 * Returns the number of existing owners located in the given city, compared
	 * case-insensitively with surrounding whitespace ignored. Used on create to enforce the
	 * per-city capacity rule, so a city that has already reached its limit rejects further owners.
	 *
	 * @param city the city to match
	 * @return the count of existing owners in the given city
	 */
	int countOwnersInCity(String city) throws DataAccessException;

	/**
	 * Returns the number of existing owners whose {@code registrationDate} equals the given date.
	 * Used on create to enforce the per-day registration cap, so that once a day has reached its
	 * limit of newly created owners further creates on that day are rejected.
	 *
	 * @param date the registration date to match
	 * @return the count of existing owners registered on the given date
	 */
	int countOwnersRegisteredOn(LocalDate date) throws DataAccessException;

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
