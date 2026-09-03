package org.springframework.samples.petclinic.rest.function.owner;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

public class BuildOwner {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public void service(@RequestBody OwnerFieldsDto request, OwnerMapper ownerMapper, Out<Owner> built,
            Out<Boolean> sharesHousehold) throws MissingOwnerFieldsException {
        sharesHousehold.set(Boolean.TRUE.equals(request.getSharesHousehold()));
        Map<String, String> required = new LinkedHashMap<>();
        required.put("firstName", request.getFirstName());
        required.put("lastName", request.getLastName());
        required.put("address", request.getAddress());
        required.put("city", request.getCity());
        required.put("telephone", request.getTelephone());
        List<String> missing = required.entrySet().stream()
                .filter(e -> e.getValue() == null || e.getValue().isBlank())
                .map(Map.Entry::getKey).toList();
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
        Owner owner = ownerMapper.toOwner(request);
        String telephone = toE164(owner.getTelephone());
        if (telephone == null) {
            throw new MissingOwnerFieldsException(List.of("telephone"));
        }
        owner.setTelephone(telephone);
        String email = owner.getEmail();
        if (email != null && !EMAIL.matcher(email).matches()) {
            throw new MissingOwnerFieldsException(List.of("email"));
        }
        built.set(owner);
    }

    /**
     * Normalizes a raw telephone to E.164. A leading '+' keeps its country code;
     * otherwise '+61' is assumed and a single leading '0' is dropped. Spaces, dashes
     * and brackets are stripped. Returns null when the result is not 8..15 digits.
     */
    static String toE164(String raw) {
        String trimmed = raw.trim();
        String digits;
        if (trimmed.startsWith("+")) {
            digits = trimmed.substring(1).replaceAll("\\D", "");
        }
        else {
            String national = trimmed.replaceAll("\\D", "");
            digits = "61" + (national.startsWith("0") ? national.substring(1) : national);
        }
        return digits.length() >= 8 && digits.length() <= 15 ? "+" + digits : null;
    }
}
