package no.novari.flyt.egrunnerverv.gateway.exception;

import no.novari.flyt.egrunnerverv.gateway.slack.SlackAlertService;
import no.novari.flyt.instance.gateway.exception.AbstractInstanceRejectedException;

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
