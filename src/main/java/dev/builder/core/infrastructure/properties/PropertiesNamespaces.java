package dev.builder.core.infrastructure.properties;

class PropertiesNamespaces {

    static final String MODULE_CONTEXT_PATH = "/dev/builder/";
    static final String FILE_PREFIX = "application";
    static final String FILE_POSTFIX = ".properties";

    static class Session{
        private static final String PREFIX = "session.file";
        static final String TRANSFORMATION = PREFIX + ".secret.transformation";
        static final String KEY_TYPE = PREFIX + ".secret.keytype";
        static final String FILENAME = PREFIX + ".name";
        static final String TTL = PREFIX + ".ttl.seconds";
    }

    static final String ENV_PROFILE_NAMESPACE = "KEDU_PROFILE";
    static final String PROPERTIES_PROFILE_NAMESPACE = "env.profile";
}
