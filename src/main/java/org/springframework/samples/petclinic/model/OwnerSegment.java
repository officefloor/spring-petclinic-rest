package org.springframework.samples.petclinic.model;

/**
 * The owner's marketing segment, formatted as {@code <TIER>_<AREA>}, one of
 * {@code PREMIUM_METRO}, {@code PREMIUM_REGIONAL}, {@code STANDARD_METRO} or
 * {@code STANDARD_REGIONAL}. It composes two independent axes derived at read time:
 *
 * <ul>
 *   <li>{@link Tier} — {@link Tier#PREMIUM} at {@link #PREMIUM_MIN_LEVEL membership level 3}
 *   or more, otherwise {@link Tier#STANDARD}.</li>
 *   <li>{@link Area} — {@link Area#METRO} when the locality is a
 *   {@link CityRegion#isKnownRegion(String) known region} (NSW, VIC or QLD), otherwise
 *   {@link Area#REGIONAL}.</li>
 * </ul>
 */
public final class OwnerSegment {

    /** The membership level at or above which the owner is {@link Tier#PREMIUM}. */
    public static final int PREMIUM_MIN_LEVEL = 3;

    /** The membership pricing tier axis of the segment. */
    public enum Tier {
        PREMIUM, STANDARD;

        /** The tier for the given membership level: {@link #PREMIUM} at
         *  {@link #PREMIUM_MIN_LEVEL} or more, otherwise {@link #STANDARD}. */
        public static Tier of(int membershipLevel) {
            return membershipLevel >= PREMIUM_MIN_LEVEL ? PREMIUM : STANDARD;
        }
    }

    /** The geographic area axis of the segment. */
    public enum Area {
        METRO, REGIONAL;

        /** The area for the given locality: {@link #METRO} for a
         *  {@link CityRegion#isKnownRegion(String) known region}, otherwise
         *  {@link #REGIONAL}. */
        public static Area of(String locality) {
            return CityRegion.isKnownRegion(locality) ? METRO : REGIONAL;
        }
    }

    private OwnerSegment() {
    }

    /**
     * The segment for the given membership level and locality, formatted as
     * {@code <TIER>_<AREA>}.
     */
    public static String of(int membershipLevel, String locality) {
        return Tier.of(membershipLevel) + "_" + Area.of(locality);
    }
}
