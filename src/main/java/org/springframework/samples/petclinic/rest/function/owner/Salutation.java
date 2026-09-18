package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The single definition of an owner's salutation: the optional title (e.g. MR/MRS/MS/DR)
 * prefixed to the last name, separated by a single space. When no title is on file the
 * salutation is the last name alone.
 *
 * <p>A pure function of the title and last name, so it is composed on read rather than stored.
 */
public final class Salutation {

    private Salutation() {
    }

    /** The salutation for {@code lastName}: {@code title + " " + lastName} when a title is
     *  present, otherwise {@code lastName} on its own. */
    public static String of(String title, String lastName) {
        return title == null || title.isBlank() ? lastName : title + " " + lastName;
    }
}
