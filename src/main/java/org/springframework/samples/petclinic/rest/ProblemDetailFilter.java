package org.springframework.samples.petclinic.rest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Ensures every rejection response (400, 409, 429) carries an RFC7807
 * {@code application/problem+json} body. Handlers that already write a problem detail are
 * left untouched; a bodyless rejection (e.g. a controller returning only a status) is given
 * a minimal problem detail so the contract holds uniformly.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class ProblemDetailFilter extends OncePerRequestFilter {

    private static final Set<Integer> REJECTIONS = Set.of(400, 409, 429);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        ContentCachingResponseWrapper buffered = new ContentCachingResponseWrapper(response);
        chain.doFilter(request, buffered);
        if (REJECTIONS.contains(buffered.getStatus()) && buffered.getContentSize() == 0) {
            writeProblem(buffered, HttpStatus.valueOf(buffered.getStatus()));
        }
        buffered.copyBodyToResponse();
    }

    /** Write a minimal RFC7807 problem detail ('type', 'title', 'status', 'detail') for the status. */
    private void writeProblem(ContentCachingResponseWrapper response, HttpStatus status) throws IOException {
        response.setContentType("application/problem+json");
        String reason = status.getReasonPhrase();
        byte[] body = ("{\"type\":\"about:blank\",\"title\":\"" + reason + "\",\"status\":" + status.value()
            + ",\"detail\":\"" + reason + "\"}").getBytes(StandardCharsets.UTF_8);
        response.getOutputStream().write(body);
    }
}
