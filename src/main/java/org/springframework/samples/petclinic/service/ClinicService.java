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
	void deleteOwner(Owner owner) throws DataAccessException;
	Collection<Owner> findOwnerByLastName(String lastName) throws DataAccessException;

	/**
	 * Returns {@code true} if any existing owner already uses the given telephone once it has
	 * been normalized. Enforces the invariant that no two owners may share a normalized
	 * telephone, so callers can reject a duplicate create.
	 *
	 * @param telephone the candidate telephone (normalized before comparison)
	 */
	boolean isTelephoneInUse(String telephone) throws DataAccessException;

	/**
	 * Returns {@code true} if any existing owner already shares a household with the given
	 * candidate, i.e. has the same last name and the same address once both are compared
	 * case-insensitively with internal whitespace collapsed. Enforces the invariant that two
	 * owners may not be created at the same address under the same last name, so callers can
	 * reject a duplicate create unless the household is shared intentionally.
	 *
	 * @param lastName the candidate owner's last name
	 * @param address  the candidate owner's address
	 */
	boolean isHouseholdInUse(String lastName, String address) throws DataAccessException;

	/**
	 * Places the given owner into the household identified by its last name and address and
	 * returns the household's stable shared identifier. The identifier is derived
	 * deterministically from the normalized last name and address, so every owner in the same
	 * household (same last name and address, compared case-insensitively with internal
	 * whitespace collapsed) receives the same value. Any already-stored owners in that
	 * household that lack a household identifier are back-filled with the shared value, so the
	 * joining owner and the existing owner(s) end up sharing it.
	 *
	 * @param owner the owner joining the household; its {@code householdId} is set
	 * @return the shared household identifier assigned to the owner
	 */
	String assignHousehold(Owner owner) throws DataAccessException;

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
