package no.novari.flyt.egrunnerverv.gateway.instance.mapping

import org.springframework.stereotype.Service
import java.util.Locale

@Service
class FormattingUtilsService {
    private val locale: Locale = Locale.forLanguageTag("nb-NO")

    fun formatEmail(email: String): String = email.trim().lowercase()

    fun extractEmailDomain(email: String): String? =
        email
            .substringAfter('@', "")
            .trim()
            .lowercase()
            .takeIf { it.isNotBlank() }

    fun formatKommunenavn(kommunenavn: String?): String? {
        if (kommunenavn.isNullOrBlank()) {
            return kommunenavn
        }

        val normalized = kommunenavn.replace("\\s+".toRegex(), " ").trim()
        return capitalizeFully(normalized)
    }

    fun capitalizeFully(input: String): String {
        val delimiters = setOf(' ', '-')

        val result = StringBuilder(input.length)
        var capitalizeNext = true

        for (char in input.lowercase(locale)) {
            if (capitalizeNext && char.isLetter()) {
                result.append(char.titlecase(locale))
                capitalizeNext = false
            } else {
                result.append(char)
                capitalizeNext = char in delimiters
            }
        }

        return result.toString()
    }
}
