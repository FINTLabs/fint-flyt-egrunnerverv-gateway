package no.fintlabs.exception;

import no.fintlabs.gateway.instance.exception.AbstractInstanceRejectedException;

public class NonMatchingEmailDomainWithOrgIdException extends AbstractInstanceRejectedException {
    public NonMatchingEmailDomainWithOrgIdException(String domain, String orgId) {
        super("Email domain='" + domain + "' does not match orgId='" + orgId + "'");
    }
}