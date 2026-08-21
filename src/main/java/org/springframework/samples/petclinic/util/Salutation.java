package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code salutation} - the optional {@code title} (MR/MRS/MS/DR) followed by a
 * single space and the owner's {@code lastName}. When no title is supplied the salutation is just
 * the last name, with no leading space.
 */
public final class Salutation {

    private Salutation() {
    }

    /**
     * Builds the salutation for {@code owner}: {@code title + ' ' + lastName}, or just
     * {@code lastName} when the owner has no title.
     *
     * @param owner the owner whose salutation to build.
     * @return the composed salutation.
     */
    public static String of(Owner owner) {
        String title = owner.getTitle();
        String lastName = owner.getLastName();
        if (title == null || title.isBlank()) {
            return lastName;
        }
        return title + " " + lastName;
    }
}
