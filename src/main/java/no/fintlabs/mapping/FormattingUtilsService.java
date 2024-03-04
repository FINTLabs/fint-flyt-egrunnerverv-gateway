package no.fintlabs.mapping;

import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

@Service
public class FormattingUtilsService {
    public String extractEmailDomain(String email) {
        if (email == null || !email.contains("@")) {
            return "Invalid email";
        }
        return email.substring(email.indexOf("@") + 1).toLowerCase();
    }

    public String formatKommunenavn(String kommunenavn) {
        return StringUtils.capitalize(kommunenavn.toLowerCase());
    }
}
