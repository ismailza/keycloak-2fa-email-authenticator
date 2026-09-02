package com.mesutpiskin.keycloak.auth.email;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;

import java.util.Map;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link EmailAuthenticatorRequiredAction}.
 */
@DisplayName("EmailAuthenticatorRequiredAction Tests")
class EmailAuthenticatorRequiredActionTest {

    @Nested
    @DisplayName("Masked email exposure on the enrolment verification form")
    class MaskedEmailExposureTests {

        private static final String CONFIG_ID = "email-authenticator-config";

        private EmailAuthenticatorRequiredAction requiredAction;
        private RequiredActionContext context;
        private UserModel user;
        private AuthenticationSessionModel session;
        private LoginFormsProvider form;
        private AuthenticatorConfigModel config;

        @BeforeEach
        void setUp() {
            requiredAction = new EmailAuthenticatorRequiredAction();
            context = mock(RequiredActionContext.class);
            user = mock(UserModel.class);
            session = mock(AuthenticationSessionModel.class);
            form = mock(LoginFormsProvider.class);
            config = mock(AuthenticatorConfigModel.class);

            RealmModel realm = mock(RealmModel.class);
            AuthenticationFlowModel flow = mock(AuthenticationFlowModel.class);
            AuthenticationExecutionModel execution = mock(AuthenticationExecutionModel.class);
            HttpRequest httpRequest = mock(HttpRequest.class);

            MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
            formData.putSingle(EmailConstants.CODE, "000000");

            when(context.getUser()).thenReturn(user);
            when(context.getAuthenticationSession()).thenReturn(session);
            when(context.getRealm()).thenReturn(realm);
            when(context.getHttpRequest()).thenReturn(httpRequest);
            when(context.form()).thenReturn(form);
            when(httpRequest.getDecodedFormParameters()).thenReturn(formData);
            when(form.setAttribute(anyString(), any())).thenReturn(form);
            when(form.setError(anyString(), any(Object[].class))).thenReturn(form);
            when(form.createForm(anyString())).thenReturn(mock(Response.class));

            when(flow.getId()).thenReturn("flow-id");
            when(realm.getAuthenticationFlowsStream()).thenAnswer(inv -> Stream.of(flow));
            when(realm.getAuthenticationExecutionsStream("flow-id")).thenAnswer(inv -> Stream.of(execution));
            when(execution.getAuthenticator()).thenReturn(EmailAuthenticatorFormFactory.PROVIDER_ID);
            when(execution.getAuthenticatorConfig()).thenReturn(CONFIG_ID);
            when(realm.getAuthenticatorConfigById(CONFIG_ID)).thenReturn(config);

            // A code was sent, so processAction takes the "verify submitted code" path.
            when(session.getAuthNote(EmailConstants.CODE)).thenReturn(OtpHashUtils.hash("123456"));
            when(session.getAuthNote(EmailConstants.CODE_TTL))
                    .thenReturn(String.valueOf(System.currentTimeMillis() + 300_000));
            when(user.getEmail()).thenReturn("username@example.com");
        }

        @Test
        @DisplayName("Should expose masked email when enabled")
        void shouldExposeMaskedEmailWhenEnabled() {
            when(config.getConfig()).thenReturn(Map.of(
                    EmailConstants.SHOW_MASKED_EMAIL_ON_OTP_FORM, "true",
                    EmailConstants.CODE_LENGTH, "6"));

            requiredAction.processAction(context);

            verify(form).setAttribute("maskedEmail", "u***e@example.com");
        }

        @Test
        @DisplayName("Should not expose masked email when disabled")
        void shouldNotExposeMaskedEmailWhenDisabled() {
            when(config.getConfig()).thenReturn(Map.of(
                    EmailConstants.SHOW_MASKED_EMAIL_ON_OTP_FORM, "false",
                    EmailConstants.CODE_LENGTH, "6"));

            requiredAction.processAction(context);

            verify(form, never()).setAttribute(eq("maskedEmail"), any());
        }

        @Test
        @DisplayName("Should not expose masked email when the setting is absent")
        void shouldNotExposeMaskedEmailByDefault() {
            when(config.getConfig()).thenReturn(Map.of(EmailConstants.CODE_LENGTH, "6"));

            requiredAction.processAction(context);

            verify(form, never()).setAttribute(eq("maskedEmail"), any());
        }

        @Test
        @DisplayName("Should still expose masked email once the attempt limit is reached")
        void shouldExposeMaskedEmailWhenMaxAttemptsReached() {
            when(config.getConfig()).thenReturn(Map.of(
                    EmailConstants.SHOW_MASKED_EMAIL_ON_OTP_FORM, "true",
                    EmailConstants.MAX_ATTEMPTS, "1"));

            requiredAction.processAction(context);

            verify(form).setAttribute("maxAttemptsReached", true);
            verify(form).setAttribute("maskedEmail", "u***e@example.com");
        }
    }
}
