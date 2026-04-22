package no.novari.flyt.egrunnerverv.gateway.instance.mapping

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FormattingUtilsServiceTest {
    private lateinit var formattingUtilsService: FormattingUtilsService

    @BeforeEach
    fun setUp() {
        formattingUtilsService = FormattingUtilsService()
    }

    @Test
    fun `extractEmailDomain returns null for invalid email`() {
        assertThat(formattingUtilsService.extractEmailDomain("test")).isNull()
        assertThat(formattingUtilsService.extractEmailDomain("")).isNull()
    }

    @Test
    fun `extractEmailDomain returns email domain for valid email`() {
        assertThat(formattingUtilsService.extractEmailDomain("test@test.no")).isEqualTo("test.no")
    }

    @Test
    fun `formatKommunenavn returns null for null input`() {
        assertThat(formattingUtilsService.formatKommunenavn(null)).isNull()
    }

    @Test
    fun `formatKommunenavn preserves blank input`() {
        assertThat(formattingUtilsService.formatKommunenavn("")).isEqualTo("")
    }

    @Test
    fun `formatKommunenavn capitalizes and normalizes whitespace`() {
        assertThat(
            formattingUtilsService.formatKommunenavn("NORD-ODAL\tDONALD\nDUCK"),
        ).isEqualTo("Nord-Odal Donald Duck")
    }
}
