package no.novari.flyt.egrunnerverv.gateway.instance.mapping;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class FormattingUtilsServiceTest {

    FormattingUtilsService formattingUtilsService;

    @BeforeEach
    public void setUp() {
        formattingUtilsService = new FormattingUtilsService();
    }

    @Test
    public void givenNullEmailShouldReturnInvalidEmailString() {
        String formattedEmail = formattingUtilsService.extractEmailDomain(null);

        assertThat(formattedEmail).isEqualTo("Invalid email");
    }

    @Test
    public void givenInvalidEmailShouldReturnInvalidEmailString() {
        String email = "test";
        String formattedEmail = formattingUtilsService.extractEmailDomain(email);

        assertThat(formattedEmail).isEqualTo("Invalid email");
    }

    @Test
    public void givenValidEmailShouldReturnEmailDomain() {
        String email = "test@test.no";
        String formattedEmail = formattingUtilsService.extractEmailDomain(email);

        assertThat(formattedEmail).isEqualTo("test.no");
    }

    @Test
    public void givenNullStringShouldReturnNull() {
        String formattedKommunenavn = formattingUtilsService.formatKommunenavn(null);

        assertThat(formattedKommunenavn).isEqualTo(null);
    }

    @Test
    public void givenEmptyStringShouldReturnEmptyString() {
        String kommunenavn = "";
        String formattedKommunenavn = formattingUtilsService.formatKommunenavn(kommunenavn);

        assertThat(formattedKommunenavn).isEqualTo("");
    }

    @Test
    public void givenUpperCaseStringShouldReturnFormattedStringWithCapitalizedFirstLetter() {
        String kommunenavn = "SKIEN";
        String formattedKommunenavn = formattingUtilsService.formatKommunenavn(kommunenavn);

        assertThat(formattedKommunenavn).isEqualTo("Skien");
    }

    @Test
    public void givenUpperCaseStringWithOneDashBetweenWordsShouldReturnCapitalizedFirstLetterOnEachWord() {
        String kommunenavn = "NORD-ODAL";
        String formattedKommunenavn = formattingUtilsService.formatKommunenavn(kommunenavn);

        assertThat(formattedKommunenavn).isEqualTo("Nord-Odal");
    }

    @Test
    public void givenUpperCaseStringWithMultipleDashesBetweenWordsShouldReturnCapitalizedFirstLetterOnEachWordWIthDashesInBetweenWords() {
        String kommunenavn = "NORD-ODAL-DONALD";
        String formattedKommunenavn = formattingUtilsService.formatKommunenavn(kommunenavn);

        assertThat(formattedKommunenavn).isEqualTo("Nord-Odal-Donald");
    }

    @Test
    public void givenUpperCaseStringWithMultipleSpacesBetweenWordsShouldReturnCapitalizedFirstLetterOnEachWordWIthSpacesInBetweenWords() {
        String kommunenavn = "NORD ODAL DONALD";
        String formattedKommunenavn = formattingUtilsService.formatKommunenavn(kommunenavn);

        assertThat(formattedKommunenavn).isEqualTo("Nord Odal Donald");
    }

    @Test
    public void givenStringWithTabsBetweenWordsShouldNormalizeToSpaces() {
        String kommunenavn = "NORD\tODAL\tDONALD";
        String formatted = formattingUtilsService.formatKommunenavn(kommunenavn);
        assertThat(formatted).isEqualTo("Nord Odal Donald");
    }

    @Test
    public void givenStringWithNewlinesBetweenWordsShouldNormalizeToSpaces() {
        String kommunenavn = "NORD\nODAL\nDONALD";
        String formatted = formattingUtilsService.formatKommunenavn(kommunenavn);
        assertThat(formatted).isEqualTo("Nord Odal Donald");
    }

    @Test
    public void givenStringWithCarriageReturnsBetweenWordsShouldNormalizeToSpaces() {
        String kommunenavn = "NORD\rODAL\rDONALD";
        String formatted = formattingUtilsService.formatKommunenavn(kommunenavn);
        assertThat(formatted).isEqualTo("Nord Odal Donald");
    }

    @Test
    public void givenStringWithFormFeedBetweenWordsShouldNormalizeToSpaces() {
        String kommunenavn = "NORD\fODAL\fDONALD";
        String formatted = formattingUtilsService.formatKommunenavn(kommunenavn);
        assertThat(formatted).isEqualTo("Nord Odal Donald");
    }

    @Test
    public void givenStringWithMixedSeparatorsShouldPreserveDashesAndNormalizeSpaces() {
        String kommunenavn = "NORD-ODAL\tDONALD\nDUCK";
        String formatted = formattingUtilsService.formatKommunenavn(kommunenavn);
        assertThat(formatted).isEqualTo("Nord-Odal Donald Duck");
    }

}