package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Assigns the owner's soft-match duplicate signal. A create request that clears the hard
 * duplicate check (see {@link EnsureUniqueIdentity}) may still resemble an existing owner: when
 * the new owner shares an existing owner's lastName (compared case-insensitively with collapsed
 * whitespace) and postcode while carrying a different telephone, it is still created but flagged
 * with {@code possibleDuplicate} true and {@code possibleDuplicateOf} set to that existing
 * owner's id. Otherwise {@code possibleDuplicate} is false and {@code possibleDuplicateOf} null.
 *
 * <p>A declared household member ({@code sharesHousehold} true) is never flagged: it shares an
 * existing member's lastName and postcode by design, so it is a known household member rather than
 * a suspected duplicate. (A same lastName + postcode owner that did <em>not</em> declare the
 * household is rejected as a household duplicate by {@link EnsureUniqueIdentity} unless it carries a
 * distinct email; such an email-distinguished owner does reach this step and, having a different
 * telephone, is flagged as a possible duplicate of the existing member.)
 *
 * <p>Only owners with a postcode participate: a match requires both postcodes to be present and
 * equal. When several existing owners match, the earliest (lowest id) is reported. Runs before
 * {@code save}, so the new owner is not compared against itself.
 */
public class AssignPossibleDuplicate {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
            return; // a declared household member is not a suspected duplicate
        }
        String lastName = normalize(owner.getLastName());
        String postcode = owner.getPostcode();
        String telephone = owner.getTelephone();
        Owner match = null;
        if (postcode != null && !postcode.isBlank()) {
            for (Owner existing : ownerRepository.findAll()) {
                if (existing.isDeleted()) {
                    continue; // a soft-deleted owner is not a soft-match candidate
                }
                if (lastName.equals(normalize(existing.getLastName()))
                        && postcode.equals(existing.getPostcode())
                        && !equalsTelephone(telephone, existing.getTelephone())) {
                    if (match == null || lessThan(existing.getId(), match.getId())) {
                        match = existing;
                    }
                }
            }
        }
        if (match != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.getId());
        }
        else {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
        }
    }

    private static boolean equalsTelephone(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    private static boolean lessThan(Integer a, Integer b) {
        if (a == null) {
            return false;
        }
        if (b == null) {
            return true;
        }
        return a < b;
    }

    /** Case-insensitive with collapsed whitespace: trim, fold internal whitespace runs to a
     *  single space, and lower-case. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
