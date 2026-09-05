package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;

/**
 * Normalizes the optional owner {@code email}. Email may be omitted; when present it must
 * be a syntactically valid address. A valid address is stored lower-cased (so it is later
 * mapped onto the {@link org.springframework.samples.petclinic.model.Owner} and returned
 * lower-cased); a present-but-invalid address is rejected 400 via
 * {@link InvalidEmailException}. A blank value is treated as absent.
 *
 * <p>Mutates the published body in place, mirroring {@link NormalizeOwnerTelephone}.
 */
public class NormalizeOwnerEmail {

    /**
     * Syntactic email check: a non-empty local part, an '@', then a domain with at least one
     * dot and non-empty labels, none of the parts containing whitespace or a second '@'.
     */
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@.]+(\\.[^\\s@.]+)+$");

    public void service(@Val OwnerFieldsDto request) throws InvalidEmailException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            request.setEmail(null);
            return;
        }
        String trimmed = email.trim();
        if (!EMAIL.matcher(trimmed).matches()) {
            throw new InvalidEmailException("Email must be a syntactically valid address");
        }
        request.setEmail(trimmed.toLowerCase(Locale.ROOT));
    }
}
