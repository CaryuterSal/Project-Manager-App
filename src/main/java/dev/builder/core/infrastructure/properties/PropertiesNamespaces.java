package dev.builder.core.infrastructure.properties;

public class PropertiesNamespaces {

    static final String MODULE_CONTEXT_PATH = "/";
    static final String FILE_PREFIX = "application";
    static final String FILE_POSTFIX = ".properties";

    static class DataSource{
        static final String  DB_URL = "db.url";
    }

    static final String LOCALE_DEFAULT="locale.default";

    static final String ENV_PROFILE_NAMESPACE = "KEDU_PROFILE";
    static final String PROPERTIES_PROFILE_NAMESPACE = "env.profile";
}
