package com.mesutpiskin.keycloak.auth.email;

import org.keycloak.models.UserModel;

import java.util.Map;

/**
 * Utility class for partially masking email addresses before they are exposed
 * on a login or enrolment screen.
 * <p>
 * The masked form confirms which inbox received a one-time code without
 * disclosing the full address to anyone looking at the screen. Both the OTP
 * form shown during authentication and the verification form shown during
 * enrolment share this implementation so the two screens always render the
 * address the same way.
 * </p>
 */
final class EmailMaskUtils {

    private EmailMaskUtils() {
        throw new UnsupportedOperationException("EmailMaskUtils is a utility class and cannot be instantiated");
    }

    /**
     * Returns whether the masked address should be shown, according to the
     * {@value EmailConstants#SHOW_MASKED_EMAIL_ON_OTP_FORM} setting in the given
     * authenticator configuration.
     *
     * @param configValues the resolved authenticator configuration (never {@code null})
     * @return {@code true} when the masked address should be rendered
     */
    static boolean isMaskedEmailEnabled(Map<String, String> configValues) {
        return Boolean.parseBoolean(configValues.getOrDefault(
                EmailConstants.SHOW_MASKED_EMAIL_ON_OTP_FORM,
                String.valueOf(EmailConstants.DEFAULT_SHOW_MASKED_EMAIL_ON_OTP_FORM)));
    }

    /**
     * Returns the masked email address of the given user, or {@code null} when
     * the user is unknown or has no email address on their profile.
     *
     * @param user the user whose email address should be masked (may be {@code null})
     * @return the masked address, or {@code null} when there is nothing to mask
     */
    static String maskEmailAddress(UserModel user) {
        if (user == null) {
            return null;
        }
        return maskEmailAddress(user.getEmail());
    }

    /**
     * Returns a partially masked form of the given email address.
     * <p>
     * The first and last characters of the local part are preserved
     * ({@code username@example.com} becomes {@code u***e@example.com}); local
     * parts of two characters or fewer keep only their first character
     * ({@code ab@example.com} becomes {@code a***@example.com}). Values that do
     * not look like an email address are returned unchanged.
     * </p>
     *
     * @param email the address to mask (may be {@code null})
     * @return the masked address, or {@code null} when the input is {@code null} or blank
     */
    static String maskEmailAddress(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 0 || atIndex == email.length() - 1) {
            return email;
        }

        String localPart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex);

        if (localPart.length() <= 2) {
            return localPart.charAt(0) + "***" + domainPart;
        }

        return localPart.charAt(0) + "***" + localPart.charAt(localPart.length() - 1) + domainPart;
    }
}
