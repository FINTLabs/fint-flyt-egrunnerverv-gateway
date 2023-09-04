package no.fintlabs.exceptions;

import no.fintlabs.gateway.instance.AbstractInstanceRejectedException;

public class ArchiveCaseNotFoundException extends AbstractInstanceRejectedException {
    public ArchiveCaseNotFoundException(String caseId) {
        super("No archive case found for caseId='" + caseId + "'");
    }
}
