package no.fintlabs.mapping;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        kommunenavn = kommunenavn.replaceAll("\\s+", " ").toLowerCase();

        Matcher matcher = Pattern.compile("[^- ]+|[- ]").matcher(kommunenavn);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String token = matcher.group();
            if (token.equals(" ") || token.equals("-")) {
                result.append(token);
            } else {
                result.append(StringUtils.capitalize(token));
            }
        }

        return result.toString();
    }
}
