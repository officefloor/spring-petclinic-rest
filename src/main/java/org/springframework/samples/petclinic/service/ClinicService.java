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

	/**
	 * Returns {@code true} if any owner already uses the given telephone number. The argument and every
	 * stored value are compared in their normalized (digits-only) form, so differently formatted but
	 * equivalent numbers collide. Enforces the "unique telephone" invariant for every owner write path.
	 *
	 * @param telephone the telephone number to look for (in any format)
	 * @return whether the normalized telephone is already in use by an owner
	 */
	boolean isTelephoneInUse(String telephone) throws DataAccessException;

	/**
	 * Returns {@code true} if another owner already has both the given last name and address. The
	 * last name and address are compared case-insensitively with runs of whitespace collapsed to a
	 * single space (and surrounding whitespace trimmed), so differently cased or spaced but
	 * equivalent values collide. Enforces the "one household per last name + address" invariant on
	 * owner creation.
	 *
	 * @param lastName the last name to look for
	 * @param address the address to look for
	 * @return whether an existing owner shares the same normalized last name and address
	 */
	boolean isHouseholdInUse(String lastName, String address) throws DataAccessException;

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
