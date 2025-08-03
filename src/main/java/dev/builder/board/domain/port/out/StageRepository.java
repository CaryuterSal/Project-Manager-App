package dev.builder.board.domain.port.out;

import dev.builder.board.domain.model.Stage;
import dev.builder.core.domain.port.CrudRepository;
import dev.builder.core.domain.port.TransactionalCrudRepository;

public interface StageRepository extends TransactionalCrudRepository<Stage, Stage.Id> {
}
