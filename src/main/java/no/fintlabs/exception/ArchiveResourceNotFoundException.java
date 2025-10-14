package no.fintlabs.exception;

import no.fintlabs.gateway.instance.exception.AbstractInstanceRejectedException;
import no.fintlabs.slack.SlackAlertService;

public class ArchiveResourceNotFoundException extends AbstractInstanceRejectedException {

    private static final String MESSAGE_TEMPLATE = "No archive resource found for saksansvarligEpost='%s'";

    public ArchiveResourceNotFoundException(String email, SlackAlertService slackAlertService) {
        super(formatMessage(email));
        slackAlertService.sendMessage(formatMessage(email));
    }

    private static String formatMessage(String email) {
        return String.format(MESSAGE_TEMPLATE, email);
    }
}
