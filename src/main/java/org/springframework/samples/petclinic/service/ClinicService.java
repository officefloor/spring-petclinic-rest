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
	Collection<Owner> findOwnerByTelephone(String telephone) throws DataAccessException;

	/**
	 * Determine whether a persisted owner is the same person as the given owner, i.e. shares its
	 * {@link Owner#getIdentityKey() identity key} (the digest of normalized telephone, email and the
	 * Soundex code of the last name). Household members with different telephones have different
	 * identity keys and so are not duplicates of one another; soft-deleted owners are ignored.
	 *
	 * @param owner the candidate owner whose identity is checked
	 * @return {@code true} if an existing owner has the same identity key, {@code false} otherwise
	 */
	boolean isIdentityDuplicate(Owner owner) throws DataAccessException;

	/**
	 * Retrieve the owners whose email matches the given value, compared case-insensitively.
	 *
	 * @param email the email to match, case-insensitively
	 * @return a <code>Collection</code> of matching <code>Owner</code>s (empty if none)
	 */
	Collection<Owner> findOwnerByEmail(String email) throws DataAccessException;

	/**
	 * Count the number of owners located in the given city.
	 *
	 * @param city the city to match
	 * @return the number of persisted owners in that city
	 */
	long countOwnersByCity(String city) throws DataAccessException;

	/**
	 * Count the number of owners registered on the given date.
	 *
	 * @param registrationDate the registration date to match
	 * @return the number of persisted owners whose registration date equals the given date
	 */
	long countOwnersRegisteredOn(LocalDate registrationDate) throws DataAccessException;

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
