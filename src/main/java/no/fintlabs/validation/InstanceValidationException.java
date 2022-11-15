package no.fintlabs.validation;

import lombok.Getter;

import java.util.List;

public class InstanceValidationException extends RuntimeException {

    @Getter
    private final List<EgrunnervervInstanceValidationService.Error> validationErrors;

    public InstanceValidationException(List<EgrunnervervInstanceValidationService.Error> validationErrors) {
        super("Instance validation error(s): " + validationErrors);
        this.validationErrors = validationErrors;
    }
}
