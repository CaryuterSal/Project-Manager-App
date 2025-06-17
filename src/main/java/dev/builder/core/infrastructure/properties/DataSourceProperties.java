package dev.builder.core.infrastructure.properties;

import javax.sql.DataSource;

public interface DataSourceProperties {
    String geDbtUrl();
    String getDbUsername();
    String getDbPassword();
}
