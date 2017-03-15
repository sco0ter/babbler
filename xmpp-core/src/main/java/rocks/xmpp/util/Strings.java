package rocks.xmpp.util;

import java.util.regex.Pattern;

/**
 * Utility class for Strings.
 *
 * @author Christian Schudt
 */
public final class Strings {

    private static final Pattern CAMEL_CASE = Pattern.compile("(\\p{Ll})(\\p{Lu}+)");

    private Strings() {
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * Converts a string from camel case to a string with dashes.
     * <p>
     * Each upper case character (except the first one) followed by a lower case character is converted to a lower case character preceded by a dash.
     * <p>
     * E.g. "CamelCase" is converted to "camel-case".
     *
     * @param str The string.
     * @return The string with dashes.
     */
    public static String toDash(final String str) {
        if (str == null) {
            return null;
        }
        return CAMEL_CASE.matcher(str).replaceAll("$1-$2").toLowerCase();
    }

    /**
     * Converts a string from camel case to a string with underscores.
     * <p>
     * Each upper case character (except the first one) followed by a lower case character is converted to a lower case character preceded by an underscore.
     * <p>
     * E.g. "CamelCase" is converted to "camel_case".
     *
     * @param str The string.
     * @return The string with dashes.
     */
    public static String toUnderscore(final String str) {
        if (str == null) {
            return null;
        }
        return CAMEL_CASE.matcher(str).replaceAll("$1_$2").toLowerCase();
    }
}
