package dev.builder.board.domain.port.out;

import dev.builder.board.domain.model.Stage;
import dev.builder.core.domain.port.CrudRepository;

public interface StageRepository extends CrudRepository<Stage, Stage.Id> {
}
