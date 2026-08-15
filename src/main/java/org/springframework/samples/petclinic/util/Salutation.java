package org.springframework.samples.petclinic.util;

/**
 * Composes an owner's {@code salutation} from its optional {@code title} (MR/MRS/MS/DR)
 * and {@code lastName}: the title followed by a single space and the last name (e.g.
 * {@code "DR who"}), or just the last name when no title is given.
 */
public final class Salutation {

    private Salutation() {
    }

    /** Returns {@code title + " " + lastName}, or just {@code lastName} when no title is given. */
    public static String of(String title, String lastName) {
        return (title != null && !title.isEmpty()) ? title + " " + lastName : lastName;
    }
}
