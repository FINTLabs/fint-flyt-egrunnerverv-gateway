package no.fintlabs.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Constraint(validatedBy = UniqueElementIdsValidator.class)
public @interface UniqueElementIds {

    String message() default "contains duplicate element ids: [{duplicateElementIds}]";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
