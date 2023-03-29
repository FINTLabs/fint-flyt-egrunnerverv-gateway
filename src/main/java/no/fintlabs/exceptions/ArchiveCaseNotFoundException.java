package no.fintlabs.exceptions;

public class ArchiveCaseNotFoundException extends RuntimeException {
    public ArchiveCaseNotFoundException(String caseId) {
        super("No archive case found for caseId='" + caseId + "'");
    }
}
