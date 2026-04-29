package com.pangreksa.service.shared.security;

import org.jspecify.annotations.Nullable;

import java.time.ZoneId;
import java.util.Locale;

/**
 * Interface for accessing information about an application user.
 * <p>
 * This interface provides standard methods to access user identity and profile information throughout the application.
 * It can be used in various contexts, both for retrieving the current authenticated user and for accessing information
 * about any user in the system (e.g., when displaying audit information about who last modified a record).
 * </p>
 * <p>
 * Note: This interface intentionally does not extend Spring Security's {@code UserDetails} or
 * {@code AuthenticatedPrincipal} to maintain separation between authentication concerns and general user information
 * access. For the same reason, it does not contain information about the user's roles or authorities.
 * </p>
 * <p>
 * Lives in {@code shared.security} so the canonical {@code FwAppUser} entity can implement it without forcing the
 * domain core to depend on adapter-side security packages.
 * </p>
 */
public interface AppUserInfo {

    /**
     * Returns the user's unique identifier within the application.
     */
    UserId getUserId();

    /**
     * Returns the user's preferred username for display and identification purposes.
     */
    String getPreferredUsername();

    /**
     * Returns the user's full display name. Defaults to the preferred username.
     */
    default String getFullName() {
        return getPreferredUsername();
    }

    /**
     * Returns a URL to the user's profile page in the application or external system.
     */
    default @Nullable String getProfileUrl() {
        return null;
    }

    /**
     * Returns a URL to the user's profile picture or avatar.
     */
    default @Nullable String getPictureUrl() {
        return null;
    }

    /**
     * Returns the user's email address.
     */
    default @Nullable String getEmail() {
        return null;
    }

    /**
     * Returns the user's preferred time zone.
     */
    default ZoneId getZoneId() {
        return ZoneId.systemDefault();
    }

    /**
     * Returns the user's preferred locale for internationalization.
     */
    default Locale getLocale() {
        return Locale.getDefault();
    }
}
