package no.novari.flyt.egrunnerverv.gateway.instance.mapping

import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FormattingUtilsServiceTest {
    private lateinit var formattingUtilsService: FormattingUtilsService

    @BeforeEach
    fun setUp() {
        formattingUtilsService = FormattingUtilsService()
    }

    @Test
    fun givenNullEmailShouldReturnInvalidEmailString() {
        val formattedEmail = formattingUtilsService.extractEmailDomain(null)

        assertThat(formattedEmail).isEqualTo("Invalid email")
    }

    @Test
    fun givenInvalidEmailShouldReturnInvalidEmailString() {
        val email = "test"
        val formattedEmail = formattingUtilsService.extractEmailDomain(email)

        assertThat(formattedEmail).isEqualTo("Invalid email")
    }

    @Test
    fun givenValidEmailShouldReturnEmailDomain() {
        val email = "test@test.no"
        val formattedEmail = formattingUtilsService.extractEmailDomain(email)

        assertThat(formattedEmail).isEqualTo("test.no")
    }

    @Test
    fun givenNullStringShouldReturnNull() {
        val formattedKommunenavn = formattingUtilsService.formatKommunenavn(null)

        assertThat(formattedKommunenavn).isEqualTo(null)
    }

    @Test
    fun givenEmptyStringShouldReturnEmptyString() {
        val kommunenavn = ""
        val formattedKommunenavn = formattingUtilsService.formatKommunenavn(kommunenavn)

        assertThat(formattedKommunenavn).isEqualTo("")
    }

    @Test
    fun givenUpperCaseStringShouldReturnFormattedStringWithCapitalizedFirstLetter() {
        val kommunenavn = "SKIEN"
        val formattedKommunenavn = formattingUtilsService.formatKommunenavn(kommunenavn)

        assertThat(formattedKommunenavn).isEqualTo("Skien")
    }

    @Test
    fun givenUpperCaseStringWithOneDashBetweenWordsShouldReturnCapitalizedFirstLetterOnEachWord() {
        val kommunenavn = "NORD-ODAL"
        val formattedKommunenavn = formattingUtilsService.formatKommunenavn(kommunenavn)

        assertThat(formattedKommunenavn).isEqualTo("Nord-Odal")
    }

    @Test
    @Suppress("ktlint:standard:max-line-length")
    fun givenUpperCaseStringWithMultipleDashesBetweenWordsShouldReturnCapitalizedFirstLetterOnEachWordWIthDashesInBetweenWords() {
        val kommunenavn = "NORD-ODAL-DONALD"
        val formattedKommunenavn = formattingUtilsService.formatKommunenavn(kommunenavn)

        assertThat(formattedKommunenavn).isEqualTo("Nord-Odal-Donald")
    }

    @Test
    @Suppress("ktlint:standard:max-line-length")
    fun givenUpperCaseStringWithMultipleSpacesBetweenWordsShouldReturnCapitalizedFirstLetterOnEachWordWIthSpacesInBetweenWords() {
        val kommunenavn = "NORD ODAL DONALD"
        val formattedKommunenavn = formattingUtilsService.formatKommunenavn(kommunenavn)

        assertThat(formattedKommunenavn).isEqualTo("Nord Odal Donald")
    }

    @Test
    fun givenStringWithTabsBetweenWordsShouldNormalizeToSpaces() {
        val kommunenavn = "NORD\tODAL\tDONALD"
        val formatted = formattingUtilsService.formatKommunenavn(kommunenavn)
        assertThat(formatted).isEqualTo("Nord Odal Donald")
    }

    @Test
    fun givenStringWithNewlinesBetweenWordsShouldNormalizeToSpaces() {
        val kommunenavn = "NORD\nODAL\nDONALD"
        val formatted = formattingUtilsService.formatKommunenavn(kommunenavn)
        assertThat(formatted).isEqualTo("Nord Odal Donald")
    }

    @Test
    fun givenStringWithCarriageReturnsBetweenWordsShouldNormalizeToSpaces() {
        val kommunenavn = "NORD\rODAL\rDONALD"
        val formatted = formattingUtilsService.formatKommunenavn(kommunenavn)
        assertThat(formatted).isEqualTo("Nord Odal Donald")
    }

    @Test
    fun givenStringWithFormFeedBetweenWordsShouldNormalizeToSpaces() {
        val kommunenavn = "NORD\u000CODAL\u000CDONALD"
        val formatted = formattingUtilsService.formatKommunenavn(kommunenavn)
        assertThat(formatted).isEqualTo("Nord Odal Donald")
    }

    @Test
    fun givenStringWithMixedSeparatorsShouldPreserveDashesAndNormalizeSpaces() {
        val kommunenavn = "NORD-ODAL\tDONALD\nDUCK"
        val formatted = formattingUtilsService.formatKommunenavn(kommunenavn)
        assertThat(formatted).isEqualTo("Nord-Odal Donald Duck")
    }
}
