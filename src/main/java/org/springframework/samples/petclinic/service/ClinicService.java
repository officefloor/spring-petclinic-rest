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
	/**
	 * Finds the owner that was created with the given 'Idempotency-Key'.
	 *
	 * @return the owner, or {@code null} if no owner was created with the key
	 */
	Owner findOwnerByIdempotencyKey(String idempotencyKey) throws DataAccessException;
	Collection<Owner> findAllOwners() throws DataAccessException;
	Page<Owner> findOwners(String lastName, Pageable pageable) throws DataAccessException;
	void saveOwner(Owner owner) throws DataAccessException;
	/**
	 * Checks whether another non-deleted owner has the same identity key (telephone, email and household ID) as the given owner.
	 */
	boolean ownerIdentityKeyExists(Owner owner) throws DataAccessException;
	/**
	 * Finds the id of the earliest other non-deleted owner in the given owner's household (see {@link #householdIdOf(Owner)})
	 * but with a different telephone, or null when there is none.
	 */
	Integer findPossibleDuplicateOf(Owner owner) throws DataAccessException;
	/**
	 * Counts the owners whose city equals the given city, compared case-insensitively.
	 */
	long countOwnersInCity(String city) throws DataAccessException;
	/**
	 * Counts the owners whose registration date equals the given date.
	 */
	long countOwnersRegisteredOn(LocalDate registrationDate) throws DataAccessException;
	/**
	 * Checks whether an existing non-deleted owner already belongs to the given owner's household (see {@link #householdIdOf(Owner)}).
	 */
	boolean householdExists(Owner owner) throws DataAccessException;
	/**
	 * Finds the non-deleted owners, other than the given owner, that belong to the given owner's household
	 * (see {@link #householdIdOf(Owner)}); empty when the owner has no household ID.
	 */
	Collection<Owner> findHouseholdMembers(Owner owner) throws DataAccessException;
	/**
	 * Computes the owner's household ID: the first 12 lower-case hex characters of the SHA-256 of the UTF-8 bytes of
	 * '&lt;normalizedLastName&gt;|&lt;postcode&gt;', where the last name is trimmed, whitespace-collapsed and
	 * lower-cased. Null when the owner has no last name or no postcode.
	 */
	String householdIdOf(Owner owner);
	/**
	 * Soft-deletes the owner: flags it deleted and retains the record.
	 */
	void deleteOwner(Owner owner) throws DataAccessException;
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
