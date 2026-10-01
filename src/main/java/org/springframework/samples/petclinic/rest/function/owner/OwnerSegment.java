package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.OwnerRegion;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Single source of truth for an owner's derived {@code ownerSegment} — the marketing segment
 * formatted {@code <TIER>_<AREA>}: one of {@code PREMIUM_METRO}, {@code PREMIUM_REGIONAL},
 * {@code STANDARD_METRO} or {@code STANDARD_REGIONAL}.
 *
 * <p>TIER is {@code PREMIUM} when {@code membershipLevel} is 3 or more, otherwise {@code STANDARD}.
 * AREA is {@code METRO} when the owner's locality is a known region (NSW, VIC or QLD), otherwise
 * {@code REGIONAL}.
 *
 * <p>Derived at read time by the owner mapper from the already-stored membership level and the
 * owner's derived locality, so it needs no column of its own and can never drift.
 */
public final class OwnerSegment {

    private OwnerSegment() {
    }

    /**
     * The owner segment for the given {@code membershipLevel} and {@code locality}. A null
     * membership level is treated as below 3 (STANDARD); any locality that is not a known region
     * (per {@link OwnerRegion#isKnownRegion}) is REGIONAL.
     */
    public static OwnerDto.OwnerSegmentEnum segment(Integer membershipLevel, String locality) {
        boolean premium = membershipLevel != null && membershipLevel >= 3;
        boolean metro = OwnerRegion.isKnownRegion(locality);
        String tier = premium ? "PREMIUM" : "STANDARD";
        String area = metro ? "METRO" : "REGIONAL";
        return OwnerDto.OwnerSegmentEnum.fromValue(tier + "_" + area);
    }
}
