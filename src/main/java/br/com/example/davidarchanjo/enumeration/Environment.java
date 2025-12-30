package br.com.example.davidarchanjo.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumeration for environment/stage types
 */
@Getter
@RequiredArgsConstructor
public enum Environment {
    DEVELOPMENT("dev"),
    STAGING("staging"),
    PRODUCTION("prod"),
    TEST("test"),
    UAT("uat");

    private final String value;

    /**
     * Get environment from string value
     *
     * @param value environment value
     * @return Environment enum
     */
    public static Environment fromValue(String value) {
        for (Environment env : Environment.values()) {
            if (env.value.equalsIgnoreCase(value)) {
                return env;
            }
        }
        throw new IllegalArgumentException("Unknown environment: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}
