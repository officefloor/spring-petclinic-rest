package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

public class RespondWithOwner {

    private static final Map<String, String> REGION_TIMEZONE = Map.of(
            "NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    private static final Set<String> DISPOSABLE_DOMAINS =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    public void service(@Val Owner owner, OwnerMapper ownerMapper, OwnerRepository ownerRepository,
            ObjectResponse<OwnerDto> response) {
        OwnerDto dto = ownerMapper.toOwnerDto(owner);
        dto.setSelfLink("/api/owners/" + owner.getId());
        int namesakeCount = NamesakeCount.of(owner, ownerRepository);
        dto.setNamesakeCount(namesakeCount);
        int membershipPoints = MembershipPoints.of(owner, namesakeCount, HouseholdSize.of(owner, ownerRepository));
        dto.setMembershipPoints(membershipPoints);
        dto.setMembershipLevel(HouseholdLevelCeiling.cap(owner, MembershipLevel.of(membershipPoints), ownerRepository));
        dto.setLocality(Locality.of(owner));
        dto.setTimezone(REGION_TIMEZONE.get(dto.getLocality()));
        dto.setOwnerSegment(OwnerSegment.of(dto.getMembershipLevel(), dto.getLocality()));
        dto.setBulkSignupWarning(BulkSignupWarning.of(owner, ownerRepository));
        dto.setCapacityWarning(CapacityWarning.of(owner, ownerRepository));
        dto.setContactPreference(ContactPreference.of(owner));
        Integer possibleDuplicateOf = PossibleDuplicate.of(owner, ownerRepository);
        dto.setPossibleDuplicate(possibleDuplicateOf != null);
        dto.setPossibleDuplicateOf(possibleDuplicateOf);
        dto.setRiskFlag(possibleDuplicateOf != null || Boolean.TRUE.equals(dto.getCapacityWarning())
                || disposableAdjacent(owner.getEmail()));
        response.send(dto);
    }

    /** A domain is disposable-adjacent when it is a known disposable domain or a subdomain of one. */
    private static boolean disposableAdjacent(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        String domain = email.substring(email.lastIndexOf('@') + 1).toLowerCase(Locale.ROOT);
        return DISPOSABLE_DOMAINS.stream().anyMatch(d -> domain.equals(d) || domain.endsWith("." + d));
    }
}
