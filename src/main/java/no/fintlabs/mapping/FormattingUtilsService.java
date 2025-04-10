package no.fintlabs.mapping;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class FormattingUtilsService {

    public String formatEmail(String email) {
        return email.trim().toLowerCase();
    }

    public String extractEmailDomain(String email) {
        if (email == null || !email.contains("@")) {
            return "Invalid email";
        }
        return email.substring(email.indexOf("@") + 1).toLowerCase();
    }

    public String formatKommunenavn(String kommunenavn) {

        if (kommunenavn == null || kommunenavn.isEmpty()) {
            return kommunenavn;
        }

        return Arrays.stream(kommunenavn.toLowerCase().split("-"))
                .map(StringUtils::capitalize)
                .collect(Collectors.joining("-"));
    }
}
