package no.fintlabs.mapping;

import org.apache.commons.text.WordUtils;
import org.springframework.stereotype.Service;


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
        if (kommunenavn == null || kommunenavn.isBlank()) {
            return kommunenavn;
        }

        String normalized = kommunenavn.replaceAll("\\s+", " ").trim();
        return WordUtils.capitalizeFully(normalized, ' ', '-');
    }
}
