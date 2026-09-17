package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Step of {@code POST /api/owners}: assigns the new owner's {@code membershipLevel} — a
 * number from 1 to 4. It starts at 1, gains a level when the owner has an email on file,
 * another when the owner has no namesakes ({@code namesakeCount} is 0), and the top level
 * when the owner's {@link Tenure tenure} exceeds {@link #TENURE_LEVEL_DAYS} days. A newly
 * created owner has zero tenure, so it never reaches level 4. It runs after
 * {@link CountNamesakes} so the namesake count is known, and after
 * {@link ResolveOwnerRegistrationDate} so the registration date is resolved, and before
 * {@link SaveOwner} persists the owner and {@link AuditOwnerCreated} records the level. The
 * value is stored with the row and returned unchanged on later reads.
 */
public class AssignMembershipLevel {

    /** Tenure, in days, that an owner must exceed to reach the top membership level (4). */
    private static final long TENURE_LEVEL_DAYS = 365;

    public void service(@Val Owner owner) {
        int level = 1;
        String email = owner.getEmail();
        if (email != null && !email.isBlank()) {
            level++;
        }
        Integer namesakeCount = owner.getNamesakeCount();
        if (namesakeCount != null && namesakeCount == 0) {
            level++;
        }
        if (Tenure.days(owner.getRegistrationDate(), LocalDate.now()) > TENURE_LEVEL_DAYS) {
            level++;
        }
        owner.setMembershipLevel(level);
    }
}
