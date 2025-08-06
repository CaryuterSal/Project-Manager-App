package dev.builder.core.infrastructure.persistence;

import java.sql.PreparedStatement;

public interface PreparedStatementFiller extends SQLConsumer<PreparedStatement> {
    PreparedStatementFiller NO_OP = ps -> {};
}
