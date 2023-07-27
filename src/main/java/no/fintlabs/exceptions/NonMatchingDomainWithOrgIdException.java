package no.fintlabs.exceptions;

public class NonMatchingDomainWithOrgIdException extends RuntimeException {
    public NonMatchingDomainWithOrgIdException(String domain, String orgId) {
        super("Domain='" + domain + "' does not match orgId='" + orgId + "'");
    }
}
