/*
 * Copyright 2002-2013 the original author or authors.
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
package org.springframework.samples.petclinic.repository.springdatajpa;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Spring Data JPA specialization of the {@link OwnerRepository} interface
 *
 * @author Michael Isvy
 * @since 15.1.2013
 */

@Profile("spring-data-jpa")
public interface SpringDataOwnerRepository extends OwnerRepository, Repository<Owner, Integer> {

    @Override
    @Query("SELECT DISTINCT owner FROM Owner owner left join fetch owner.pets WHERE owner.lastName LIKE :lastName%")
    Collection<Owner> findByLastName(@Param("lastName") String lastName);

    @Override
    @Query(
        value = "SELECT owner FROM Owner owner WHERE owner.lastName LIKE CONCAT(:lastName, '%')",
        countQuery = "SELECT COUNT(owner) FROM Owner owner WHERE owner.lastName LIKE CONCAT(:lastName, '%')")
    Page<Owner> findByLastName(@Param("lastName") String lastName, Pageable pageable);

    @Override
    @Query(
        value = "SELECT owner FROM Owner owner",
        countQuery = "SELECT COUNT(owner) FROM Owner owner")
    Page<Owner> findAll(Pageable pageable);

    @Override
    @Query("SELECT owner FROM Owner owner left join fetch owner.pets WHERE owner.id =:id")
    Owner findById(@Param("id") int id);

    @Override
    @Query("SELECT owner FROM Owner owner WHERE owner.telephone = :telephone")
    List<Owner> findByTelephone(@Param("telephone") String telephone);

    @Override
    @Query("SELECT owner FROM Owner owner WHERE owner.idempotencyKey = :idempotencyKey")
    Owner findByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);

    @Override
    @Query("SELECT owner FROM Owner owner WHERE LOWER(owner.lastName) = :lastName")
    List<Owner> findByLastNameIgnoringCase(@Param("lastName") String lastName);

    @Override
    @Query("SELECT COUNT(owner) FROM Owner owner WHERE LOWER(owner.firstName) = :firstName AND LOWER(owner.lastName) = :lastName")
    long countByFirstNameAndLastNameIgnoringCase(@Param("firstName") String firstName, @Param("lastName") String lastName);

    @Override
    @Query("SELECT COUNT(owner) FROM Owner owner WHERE LOWER(owner.city) = :city")
    long countByCityIgnoringCase(@Param("city") String city);

    @Override
    @Query("SELECT COUNT(owner) FROM Owner owner WHERE owner.registrationDate = :registrationDate")
    long countByRegistrationDate(@Param("registrationDate") LocalDate registrationDate);

    @Override
    @Query("SELECT COUNT(owner) FROM Owner owner WHERE owner.householdId = :householdId")
    long countByHouseholdId(@Param("householdId") String householdId);

    @Override
    @Query("SELECT COUNT(owner) FROM Owner owner WHERE owner.householdId = :householdId AND owner.deleted = false")
    long countNotDeletedByHouseholdId(@Param("householdId") String householdId);

    @Override
    @Query("SELECT COUNT(owner) FROM Owner owner WHERE owner.memberId = :memberId")
    long countByMemberId(@Param("memberId") String memberId);

    @Override
    @Query("SELECT COUNT(owner) FROM Owner owner")
    long count();
}
