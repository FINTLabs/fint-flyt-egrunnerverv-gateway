package no.novari.flyt.egrunnerverv.gateway.instance.mapping

import org.apache.commons.text.WordUtils
import org.springframework.stereotype.Service

@Service
class FormattingUtilsService {
    fun formatEmail(email: String): String = email.trim().lowercase()

    fun extractEmailDomain(email: String?): String {
        if (email.isNullOrBlank() || !email.contains("@")) {
            return "Invalid email"
        }
        return email.substring(email.indexOf("@") + 1).lowercase()
    }

    fun formatKommunenavn(kommunenavn: String?): String? {
        if (kommunenavn.isNullOrBlank()) {
            return kommunenavn
        }

        val normalized = kommunenavn.replace("\\s+".toRegex(), " ").trim()
        return WordUtils.capitalizeFully(normalized, ' ', '-')
    }
}
