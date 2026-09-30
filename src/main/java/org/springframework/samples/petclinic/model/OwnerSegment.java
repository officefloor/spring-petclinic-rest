package org.springframework.samples.petclinic.model;

/**
 * The single definition of an owner's {@code ownerSegment}, formatted {@code <TIER>_<AREA>}:
 * <ul>
 * <li>TIER — {@code PREMIUM} when the {@code membershipLevel} is 3 or more, otherwise
 * {@code STANDARD};</li>
 * <li>AREA — {@code METRO} when the owner's region is a known region (NSW, VIC or QLD), otherwise
 * {@code REGIONAL}.</li>
 * </ul>
 *
 * <p>The AREA is derived from the <em>plain</em> {@link CityRegions#identityRegion(String) region}
 * — the same value as the user-facing {@code locality} — <strong>not</strong> the version-2 region
 * code used inside the identifiers, so the {@code V2} tag never leaks into the segment.
 *
 * <p>Shared by the mapping layer (the owner response) and the audit event so both report the same
 * segment. Lives in {@code model} beside {@link Owner} so both layers can depend on it without a
 * package cycle.
 */
public final class OwnerSegment {

    private OwnerSegment() {
    }

    /** The owner's segment {@code <TIER>_<AREA>} — see the class javadoc for the composition. */
    public static String of(Owner owner) {
        Integer level = owner.getMembershipLevel();
        String tier = (level != null && level >= 3) ? "PREMIUM" : "STANDARD";
        String region = CityRegions.identityRegion(owner.getPostcode());
        String area = CityRegions.UNKNOWN.equals(region) ? "REGIONAL" : "METRO";
        return tier + "_" + area;
    }
}
