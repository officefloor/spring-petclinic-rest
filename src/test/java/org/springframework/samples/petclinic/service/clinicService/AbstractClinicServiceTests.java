package org.springframework.samples.petclinic.service.clinicService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.samples.petclinic.model.*;
import org.springframework.samples.petclinic.repository.*;
import org.springframework.samples.petclinic.util.EntityUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

abstract class AbstractClinicServiceTests {

    @Autowired
    protected OwnerRepository ownerRepository;

    @Autowired
    protected PetRepository petRepository;

    @Autowired
    protected PetTypeRepository petTypeRepository;

    @Autowired
    protected SpecialtyRepository specialtyRepository;

    @Autowired
    protected VetRepository vetRepository;

    @Autowired
    protected VisitRepository visitRepository;

    @Test
    void shouldFindOwnersByLastName() {
        Collection<Owner> owners = ownerRepository.findByLastName("Davis");
        assertThat(owners.size()).isEqualTo(2);

        owners = ownerRepository.findByLastName("Daviss");
        assertThat(owners.isEmpty()).isTrue();
    }

    @Test
    void shouldFindSingleOwnerWithPet() {
        Owner owner = findEntityById(() -> ownerRepository.findById(1));
        assertThat(owner.getLastName()).startsWith("Franklin");
        assertThat(owner.getPets().size()).isEqualTo(1);
        assertThat(owner.getPets().get(0).getType()).isNotNull();
        assertThat(owner.getPets().get(0).getType().getName()).isEqualTo("cat");
    }

    @Test
    @Transactional
    void shouldInsertOwner() {
        Collection<Owner> owners = ownerRepository.findByLastName("Schultz");
        int found = owners.size();

        Owner owner = new Owner();
        owner.setFirstName("Sam");
        owner.setLastName("Schultz");
        owner.setAddress("4, Evans Street");
        owner.setCity("Wollongong");
        owner.setTelephone("4444444444");
        ownerRepository.save(owner);
        assertThat(owner.getId().longValue()).isNotEqualTo(0);
        assertThat(owner.getPet("null value")).isNull();
        owners = ownerRepository.findByLastName("Schultz");
        assertThat(owners.size()).isEqualTo(found + 1);
    }

    @Test
    @Transactional
    void shouldUpdateOwner() {
        Owner owner = findEntityById(() -> ownerRepository.findById(1));
        String oldLastName = owner.getLastName();
        String newLastName = oldLastName + "X";

        owner.setLastName(newLastName);
        ownerRepository.save(owner);

        owner = findEntityById(() -> ownerRepository.findById(1));
        assertThat(owner.getLastName()).isEqualTo(newLastName);
    }

    @Test
    void shouldFindPetWithCorrectId() {
        Pet pet7 = findEntityById(() -> petRepository.findById(7));
        assertThat(pet7.getName()).startsWith("Samantha");
        assertThat(pet7.getOwner().getFirstName()).isEqualTo("Jean");
    }

    @Test
    @Transactional
    void shouldInsertPetIntoDatabaseAndGenerateId() {
        Owner owner6 = findEntityById(() -> ownerRepository.findById(6));
        int found = owner6.getPets().size();

        Pet pet = new Pet();
        pet.setName("bowser");
        Collection<PetType> types = petRepository.findPetTypes();
        pet.setType(EntityUtils.getById(types, PetType.class, 2));
        pet.setBirthDate(LocalDate.now());
        owner6.addPet(pet);
        assertThat(owner6.getPets().size()).isEqualTo(found + 1);

        savePet(pet);
        ownerRepository.save(owner6);

        owner6 = findEntityById(() -> ownerRepository.findById(6));
        assertThat(owner6.getPets().size()).isEqualTo(found + 1);
        assertThat(pet.getId()).isNotNull();
    }

    @Test
    @Transactional
    void shouldUpdatePetName() throws Exception {
        Pet pet7 = findEntityById(() -> petRepository.findById(7));
        String oldName = pet7.getName();

        String newName = oldName + "X";
        pet7.setName(newName);
        savePet(pet7);

        pet7 = findEntityById(() -> petRepository.findById(7));
        assertThat(pet7.getName()).isEqualTo(newName);
    }

    @Test
    void shouldFindVets() {
        Collection<Vet> vets = vetRepository.findAll();

        Vet vet = EntityUtils.getById(vets, Vet.class, 3);
        assertThat(vet.getLastName()).isEqualTo("Douglas");
        assertThat(vet.getNrOfSpecialties()).isEqualTo(2);
        assertThat(vet.getSpecialties().get(0).getName()).isEqualTo("dentistry");
        assertThat(vet.getSpecialties().get(1).getName()).isEqualTo("surgery");
    }

    @Test
    @Transactional
    void shouldAddNewVisitForPet() {
        Pet pet7 = findEntityById(() -> petRepository.findById(7));
        int found = pet7.getVisits().size();
        Visit visit = new Visit();
        pet7.addVisit(visit);
        visit.setDescription("test");
        visitRepository.save(visit);
        savePet(pet7);

        pet7 = findEntityById(() -> petRepository.findById(7));
        assertThat(pet7.getVisits().size()).isEqualTo(found + 1);
        assertThat(visit.getId()).isNotNull();
    }

    @Test
    void shouldFindVisitsByPetId() throws Exception {
        Collection<Visit> visits = visitRepository.findByPetId(7);
        assertThat(visits.size()).isEqualTo(2);
        Visit[] visitArr = visits.toArray(new Visit[visits.size()]);
        assertThat(visitArr[0].getPet()).isNotNull();
        assertThat(visitArr[0].getDate()).isNotNull();
        assertThat(visitArr[0].getPet().getId()).isEqualTo(7);
    }

    @Test
    void shouldFindAllPets() {
        Collection<Pet> pets = petRepository.findAll();
        Pet pet1 = EntityUtils.getById(pets, Pet.class, 1);
        assertThat(pet1.getName()).isEqualTo("Leo");
        Pet pet3 = EntityUtils.getById(pets, Pet.class, 3);
        assertThat(pet3.getName()).isEqualTo("Rosy");
    }

    @Test
    @Transactional
    void shouldDeletePet() {
        Pet pet = findEntityById(() -> petRepository.findById(1));
        petRepository.delete(pet);
        try {
            pet = findEntityById(() -> petRepository.findById(1));
        } catch (Exception e) {
            pet = null;
        }
        assertThat(pet).isNull();
    }

    @Test
    void shouldFindVisitDyId() {
        Visit visit = findEntityById(() -> visitRepository.findById(1));
        assertThat(visit.getId()).isEqualTo(1);
        assertThat(visit.getPet().getName()).isEqualTo("Samantha");
    }

    @Test
    void shouldFindAllVisits() {
        Collection<Visit> visits = visitRepository.findAll();
        Visit visit1 = EntityUtils.getById(visits, Visit.class, 1);
        assertThat(visit1.getPet().getName()).isEqualTo("Samantha");
        Visit visit3 = EntityUtils.getById(visits, Visit.class, 3);
        assertThat(visit3.getPet().getName()).isEqualTo("Max");
    }

    @Test
    @Transactional
    void shouldInsertVisit() {
        Collection<Visit> visits = visitRepository.findAll();
        int found = visits.size();

        Pet pet = findEntityById(() -> petRepository.findById(1));

        Visit visit = new Visit();
        visit.setPet(pet);
        visit.setDate(LocalDate.now());
        visit.setDescription("new visit");

        visitRepository.save(visit);
        assertThat(visit.getId().longValue()).isNotEqualTo(0);

        visits = visitRepository.findAll();
        assertThat(visits.size()).isEqualTo(found + 1);
    }

    @Test
    @Transactional
    void shouldUpdateVisit() {
        Visit visit = findEntityById(() -> visitRepository.findById(1));
        String oldDesc = visit.getDescription();
        String newDesc = oldDesc + "X";
        visit.setDescription(newDesc);
        visitRepository.save(visit);
        visit = findEntityById(() -> visitRepository.findById(1));
        assertThat(visit.getDescription()).isEqualTo(newDesc);
    }

    @Test
    @Transactional
    void shouldDeleteVisit() {
        Visit visit = findEntityById(() -> visitRepository.findById(1));
        visitRepository.delete(visit);
        try {
            visit = findEntityById(() -> visitRepository.findById(1));
        } catch (Exception e) {
            visit = null;
        }
        assertThat(visit).isNull();
    }

    @Test
    void shouldFindVetDyId() {
        Vet vet = findEntityById(() -> vetRepository.findById(1));
        assertThat(vet.getFirstName()).isEqualTo("James");
        assertThat(vet.getLastName()).isEqualTo("Carter");
    }

    @Test
    @Transactional
    void shouldInsertVet() {
        Collection<Vet> vets = vetRepository.findAll();
        int found = vets.size();

        Vet vet = new Vet();
        vet.setFirstName("John");
        vet.setLastName("Dow");

        vetRepository.save(vet);
        assertThat(vet.getId().longValue()).isNotEqualTo(0);

        vets = vetRepository.findAll();
        assertThat(vets.size()).isEqualTo(found + 1);
    }

    @Test
    @Transactional
    void shouldUpdateVet() {
        Vet vet = findEntityById(() -> vetRepository.findById(1));
        String oldLastName = vet.getLastName();
        String newLastName = oldLastName + "X";
        vet.setLastName(newLastName);
        vetRepository.save(vet);
        vet = findEntityById(() -> vetRepository.findById(1));
        assertThat(vet.getLastName()).isEqualTo(newLastName);
    }

    @Test
    @Transactional
    void shouldDeleteVet() {
        Vet vet = findEntityById(() -> vetRepository.findById(1));
        vetRepository.delete(vet);
        try {
            vet = findEntityById(() -> vetRepository.findById(1));
        } catch (Exception e) {
            vet = null;
        }
        assertThat(vet).isNull();
    }

    @Test
    void shouldFindAllOwners() {
        Collection<Owner> owners = ownerRepository.findAll();
        Owner owner1 = EntityUtils.getById(owners, Owner.class, 1);
        assertThat(owner1.getFirstName()).isEqualTo("George");
        Owner owner3 = EntityUtils.getById(owners, Owner.class, 3);
        assertThat(owner3.getFirstName()).isEqualTo("Eduardo");
    }

    @Test
    void shouldFindOwnersPage() {
        Page<Owner> owners = findOwners(null, PageRequest.of(0, 3, Sort.by("id")));
        assertThat(owners.getTotalElements()).isEqualTo(10);
        assertThat(owners.getTotalPages()).isEqualTo(4);
        assertThat(owners.getContent())
            .extracting(Owner::getFirstName)
            .containsExactly("George", "Betty", "Eduardo");
    }

    @Test
    void shouldFindOwnersPageByLastName() {
        Page<Owner> owners = findOwners("Davis", PageRequest.of(0, 1, Sort.by("id")));
        assertThat(owners.getTotalElements()).isEqualTo(2);
        assertThat(owners.getTotalPages()).isEqualTo(2);
        assertThat(owners.getContent())
            .extracting(Owner::getFirstName)
            .containsExactly("Betty");
    }

    @Test
    @Transactional
    void shouldDeleteOwner() {
        Owner owner = findEntityById(() -> ownerRepository.findById(1));
        ownerRepository.delete(owner);
        try {
            owner = findEntityById(() -> ownerRepository.findById(1));
        } catch (Exception e) {
            owner = null;
        }
        assertThat(owner).isNull();
    }

    @Test
    void shouldFindPetTypeById() {
        PetType petType = findEntityById(() -> petTypeRepository.findById(1));
        assertThat(petType.getName()).isEqualTo("cat");
    }

    @Test
    void shouldFindAllPetTypes() {
        Collection<PetType> petTypes = petTypeRepository.findAll();
        PetType petType1 = EntityUtils.getById(petTypes, PetType.class, 1);
        assertThat(petType1.getName()).isEqualTo("cat");
        PetType petType3 = EntityUtils.getById(petTypes, PetType.class, 3);
        assertThat(petType3.getName()).isEqualTo("lizard");
    }

    @Test
    @Transactional
    void shouldInsertPetType() {
        Collection<PetType> petTypes = petTypeRepository.findAll();
        int found = petTypes.size();

        PetType petType = new PetType();
        petType.setName("tiger");

        petTypeRepository.save(petType);
        assertThat(petType.getId().longValue()).isNotEqualTo(0);

        petTypes = petTypeRepository.findAll();
        assertThat(petTypes.size()).isEqualTo(found + 1);
    }

    @Test
    @Transactional
    void shouldUpdatePetType() {
        PetType petType = findEntityById(() -> petTypeRepository.findById(1));
        String oldLastName = petType.getName();
        String newLastName = oldLastName + "X";
        petType.setName(newLastName);
        petTypeRepository.save(petType);
        petType = findEntityById(() -> petTypeRepository.findById(1));
        assertThat(petType.getName()).isEqualTo(newLastName);
    }

    @Test
    @Transactional
    void shouldDeletePetType() {
        PetType petType = findEntityById(() -> petTypeRepository.findById(1));
        petTypeRepository.delete(petType);
        clearCache();
        try {
            petType = findEntityById(() -> petTypeRepository.findById(1));
        } catch (Exception e) {
            petType = null;
        }
        assertThat(petType).isNull();
    }

    @Test
    void shouldFindSpecialtyById() {
        Specialty specialty = findEntityById(() -> specialtyRepository.findById(1));
        assertThat(specialty.getName()).isEqualTo("radiology");
    }

    @Test
    void shouldFindAllSpecialtys() {
        Collection<Specialty> specialties = specialtyRepository.findAll();
        Specialty specialty1 = EntityUtils.getById(specialties, Specialty.class, 1);
        assertThat(specialty1.getName()).isEqualTo("radiology");
        Specialty specialty3 = EntityUtils.getById(specialties, Specialty.class, 3);
        assertThat(specialty3.getName()).isEqualTo("dentistry");
    }

    @Test
    @Transactional
    void shouldInsertSpecialty() {
        Collection<Specialty> specialties = specialtyRepository.findAll();
        int found = specialties.size();

        Specialty specialty = new Specialty();
        specialty.setName("dermatologist");

        specialtyRepository.save(specialty);
        assertThat(specialty.getId().longValue()).isNotEqualTo(0);

        specialties = specialtyRepository.findAll();
        assertThat(specialties.size()).isEqualTo(found + 1);
    }

    @Test
    @Transactional
    void shouldUpdateSpecialty() {
        Specialty specialty = findEntityById(() -> specialtyRepository.findById(1));
        String oldLastName = specialty.getName();
        String newLastName = oldLastName + "X";
        specialty.setName(newLastName);
        specialtyRepository.save(specialty);
        specialty = findEntityById(() -> specialtyRepository.findById(1));
        assertThat(specialty.getName()).isEqualTo(newLastName);
    }

    @Test
    @Transactional
    void shouldDeleteSpecialty() {
        Specialty specialty = new Specialty();
        specialty.setName("test");
        specialtyRepository.save(specialty);
        Integer specialtyId = specialty.getId();
        assertThat(specialtyId).isNotNull();
        specialty = findEntityById(() -> specialtyRepository.findById(specialtyId));
        assertThat(specialty).isNotNull();
        specialtyRepository.delete(specialty);
        try {
            specialty = findEntityById(() -> specialtyRepository.findById(specialtyId));
        } catch (Exception e) {
            specialty = null;
        }
        assertThat(specialty).isNull();
    }

    @Test
    @Transactional
    void shouldFindSpecialtiesByNameIn() {
        Specialty specialty1 = new Specialty();
        specialty1.setName("radiology");
        specialty1.setId(1);
        Specialty specialty2 = new Specialty();
        specialty2.setName("surgery");
        specialty2.setId(2);
        Specialty specialty3 = new Specialty();
        specialty3.setName("dentistry");
        specialty3.setId(3);
        List<Specialty> expectedSpecialties = List.of(specialty1, specialty2, specialty3);
        Set<String> specialtyNames = expectedSpecialties.stream()
            .map(Specialty::getName)
            .collect(Collectors.toSet());
        Collection<Specialty> actualSpecialties = specialtyRepository.findSpecialtiesByNameIn(specialtyNames);
        assertThat(actualSpecialties).isNotNull();
        assertThat(actualSpecialties.size()).isEqualTo(expectedSpecialties.size());
        for (Specialty expected : expectedSpecialties) {
            assertThat(actualSpecialties.stream()
                .anyMatch(
                    actual -> actual.getName().equals(expected.getName())
                    && actual.getId().equals(expected.getId()))).isTrue();
        }
    }

    void clearCache() {}

    private void savePet(Pet pet) {
        pet.setType(findEntityById(() -> petTypeRepository.findById(pet.getType().getId())));
        petRepository.save(pet);
    }

    private Page<Owner> findOwners(String lastName, Pageable pageable) {
        if (lastName != null) {
            return ownerRepository.findByLastName(lastName, pageable);
        }
        return ownerRepository.findAll(pageable);
    }

    private <T> T findEntityById(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (ObjectRetrievalFailureException | EmptyResultDataAccessException e) {
            return null;
        }
    }
}
