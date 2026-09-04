package org.springframework.samples.petclinic.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.function.owner.HouseholdTier;
import org.springframework.stereotype.Component;

/**
 * Fills the derived {@code membershipPoints} and {@code membershipLevel} on every owner
 * DTO. It needs the {@link OwnerRepository} to size the household, so it runs as a
 * MapStruct {@code @AfterMapping} step wired into {@code OwnerMapper} rather than as a
 * static expression.
 */
@Component
public class MembershipDecorator {

    @Autowired
    private OwnerRepository ownerRepository;

    @AfterMapping
    public void addMembership(Owner owner, @MappingTarget OwnerDto dto) {
        boolean goldHousehold = HouseholdTier.isGold(owner, ownerRepository);
        int points = MembershipPoints.of(owner.getNamesakeCount(), owner.getEmail(),
                owner.getRegistrationDate(), goldHousehold);
        dto.setMembershipPoints(points);
        dto.setMembershipLevel(MembershipLevel.of(points));
    }
}
