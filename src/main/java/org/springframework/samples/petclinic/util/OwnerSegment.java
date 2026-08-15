package org.springframework.samples.petclinic.util;

/**
 * Derives an owner's {@code ownerSegment}, a marketing segment formatted {@code "<TIER>_<AREA>"}.
 *
 * <p>{@code TIER} is {@code "PREMIUM"} when the owner's {@code membershipLevel} is
 * {@value #PREMIUM_LEVEL} or more, otherwise {@code "STANDARD"}. {@code AREA} is {@code "METRO"}
 * when the owner's locality is a known region (NSW, VIC or QLD), otherwise {@code "REGIONAL"}.
 * The four possible values are {@code PREMIUM_METRO}, {@code PREMIUM_REGIONAL},
 * {@code STANDARD_METRO} and {@code STANDARD_REGIONAL}.
 */
public final class OwnerSegment {

    /** Membership level at or above which the tier is {@code "PREMIUM"}. */
    private static final int PREMIUM_LEVEL = 3;

    private OwnerSegment() {
    }

    /** The segment {@code "<TIER>_<AREA>"} for {@code membershipLevel} and {@code locality}. */
    public static String of(int membershipLevel, String locality) {
        String tier = membershipLevel >= PREMIUM_LEVEL ? "PREMIUM" : "STANDARD";
        String area = LocalityResolver.isKnownRegion(locality) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
