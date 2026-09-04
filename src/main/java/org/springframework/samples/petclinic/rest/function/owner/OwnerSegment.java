package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * The owner's marketing segment, formatted '&lt;TIER&gt;_&lt;AREA&gt;' — one of 'PREMIUM_METRO',
 * 'PREMIUM_REGIONAL', 'STANDARD_METRO' or 'STANDARD_REGIONAL'. TIER is 'PREMIUM' when the owner's
 * {@code membershipLevel} is 3 or more, otherwise 'STANDARD'. AREA is 'METRO' when the region
 * recovered from the owner's version-2 identity (see {@link OwnerRegion#segmentRegion(Owner)}, the
 * identity region with the 'V2' version tag stripped) is a known region (NSW, VIC or QLD), otherwise
 * 'REGIONAL'. The segment's derived region is the plain region and never carries the 'V2' tag.
 */
public final class OwnerSegment {

    /** The known metropolitan regions. */
    private static final Set<String> METRO_REGIONS = Set.of("NSW", "VIC", "QLD");

    private OwnerSegment() {
    }

    /** The '&lt;TIER&gt;_&lt;AREA&gt;' segment for {@code owner}. */
    public static OwnerDto.OwnerSegmentEnum of(Owner owner) {
        Integer level = owner.getMembershipLevel();
        boolean premium = level != null && level >= 3;
        boolean metro = METRO_REGIONS.contains(OwnerRegion.segmentRegion(owner));
        if (premium) {
            return metro ? OwnerDto.OwnerSegmentEnum.PREMIUM_METRO : OwnerDto.OwnerSegmentEnum.PREMIUM_REGIONAL;
        }
        return metro ? OwnerDto.OwnerSegmentEnum.STANDARD_METRO : OwnerDto.OwnerSegmentEnum.STANDARD_REGIONAL;
    }
}
