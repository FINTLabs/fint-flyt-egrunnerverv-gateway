package no.fintlabs.exceptions;

public class ArchiveResourceNotFoundException extends RuntimeException {
    public ArchiveResourceNotFoundException(String email) {
        super("No archive resource found for saksansvarligEpost='" + email + "'");
    }
}
