package no.fintlabs.mapping;

public final class EmailUtils {
    private EmailUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    public static String extractEmailDomain(String email) {
        if (email == null || !email.contains("@")) {
            return "Invalid email";
        }
        return email.substring(email.indexOf("@") + 1).toLowerCase();
    }
}
