package dev.builder.core.infrastructure.properties;

public interface DataSourcePropertiesHolder {
    String getDbUrl();
    String getUser();
    String getPassword();
    String getDbName();
}
