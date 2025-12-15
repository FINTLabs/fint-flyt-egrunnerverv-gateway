package no.novari.flyt.egrunnerverv.gateway.exception;

import no.fint.model.resource.FintLinks;

public class NoSuchLinkException extends RuntimeException {

    public static NoSuchLinkException noSelfLink(FintLinks resource) {
        return new NoSuchLinkException(String.format("No self link in resource=%s", resource.toString()));
    }

    public NoSuchLinkException(String message) {
        super(message);
    }
}
