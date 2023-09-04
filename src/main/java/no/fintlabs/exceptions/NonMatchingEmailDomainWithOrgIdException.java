package no.fintlabs.exceptions;

import no.fintlabs.gateway.instance.AbstractInstanceRejectedException;

public class NonMatchingEmailDomainWithOrgIdException extends AbstractInstanceRejectedException {
    public NonMatchingEmailDomainWithOrgIdException(String domain, String orgId) {
        super("Email domain='" + domain + "' does not match orgId='" + orgId + "'");
    }
}