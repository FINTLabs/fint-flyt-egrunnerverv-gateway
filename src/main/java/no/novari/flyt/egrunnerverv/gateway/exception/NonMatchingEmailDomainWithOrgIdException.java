package no.novari.flyt.egrunnerverv.gateway.exception;

import no.novari.flyt.instance.gateway.exception.AbstractInstanceRejectedException;

public class NonMatchingEmailDomainWithOrgIdException extends AbstractInstanceRejectedException {
    public NonMatchingEmailDomainWithOrgIdException(String domain, String orgId) {
        super("Email domain='" + domain + "' does not match orgId='" + orgId + "'");
    }
}