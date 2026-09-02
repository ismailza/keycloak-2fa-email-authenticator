package com.mesutpiskin.keycloak.auth.email;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.keycloak.models.UserModel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link EmailMaskUtils}.
 */
@DisplayName("EmailMaskUtils Tests")
class EmailMaskUtilsTest {

    @Test
    @DisplayName("Should keep first and last character of the local part")
    void testMasksLongLocalPart() {
        assertEquals("u***e@example.com", EmailMaskUtils.maskEmailAddress("username@example.com"));
    }

    @Test
    @DisplayName("Should keep only the first character for short local parts")
    void testMasksShortLocalPart() {
        assertEquals("a***@example.com", EmailMaskUtils.maskEmailAddress("ab@example.com"));
        assertEquals("a***@example.com", EmailMaskUtils.maskEmailAddress("a@example.com"));
    }

    @Test
    @DisplayName("Should preserve the full domain and mask plus aliases with the local part")
    void testPreservesDomain() {
        assertEquals("t***a@mail.corp.example.com",
                EmailMaskUtils.maskEmailAddress("thomas+2fa@mail.corp.example.com"));
    }

    @Test
    @DisplayName("Should return null for null or blank input")
    void testReturnsNullForMissingEmail() {
        assertNull(EmailMaskUtils.maskEmailAddress((String) null));
        assertNull(EmailMaskUtils.maskEmailAddress(""));
        assertNull(EmailMaskUtils.maskEmailAddress("   "));
    }

    @Test
    @DisplayName("Should return malformed addresses unchanged")
    void testReturnsMalformedAddressUnchanged() {
        assertEquals("not-an-email", EmailMaskUtils.maskEmailAddress("not-an-email"));
        assertEquals("@example.com", EmailMaskUtils.maskEmailAddress("@example.com"));
        assertEquals("username@", EmailMaskUtils.maskEmailAddress("username@"));
    }

    @Test
    @DisplayName("Should mask the email address of a user")
    void testMasksUserEmail() {
        UserModel user = mock(UserModel.class);
        when(user.getEmail()).thenReturn("username@example.com");

        assertEquals("u***e@example.com", EmailMaskUtils.maskEmailAddress(user));
    }

    @Test
    @DisplayName("Should return null for a null user or a user without an email")
    void testReturnsNullForUserWithoutEmail() {
        UserModel user = mock(UserModel.class);
        when(user.getEmail()).thenReturn(null);

        assertNull(EmailMaskUtils.maskEmailAddress((UserModel) null));
        assertNull(EmailMaskUtils.maskEmailAddress(user));
    }
}
