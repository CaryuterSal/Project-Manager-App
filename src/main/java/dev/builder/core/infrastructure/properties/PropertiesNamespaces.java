package dev.builder.core.infrastructure.properties;

public class PropertiesNamespaces {

    static final String MODULE_CONTEXT_PATH = "/";
    static final String FILE_PREFIX = "application";
    static final String FILE_POSTFIX = ".properties";

    static class Admin{
        static final String ADMIN_PASSWORD = "admin.password";
        static final String ADMIN_USERNAME= "admin.username";
    }

    static class DataSource{

        static final String  DB_URL = "db.url";
        static final String  DB_USERNAME = "db.username";
        static final String  DB_PASSWORD = "db.password";
    }
}
