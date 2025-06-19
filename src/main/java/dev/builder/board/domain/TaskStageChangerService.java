package dev.builder.board.domain;

import dev.builder.board.domain.model.Stage;
import dev.builder.board.domain.model.Task;
import dev.builder.core.domain.DomainService;

public class TaskStageChangerService implements DomainService {

    public static boolean changeTaskToStage(Stage sourceStage, Stage targetStage, Task sourceTask, Task previousTask, Task nextTask) {
        if(!sourceStage.boardId().equals(targetStage.boardId())) {
            throw new IllegalArgumentException("target board id not match");
        }
        if(!sourceStage.containsTask(sourceTask)){
            throw new IllegalArgumentException("sourceTask is not part of source stage");
        }
        if(sourceStage.state().equals(Stage.StageState.DONE)) {
            return  false;
        } else if(sourceStage.equals(targetStage)){
            return sourceStage.placeTaskBetween(sourceTask,previousTask, nextTask);
        } else {
            sourceStage.removeTask(sourceTask);
            targetStage.placeTaskBetween(sourceTask, previousTask, nextTask);
            return true;
        }
    }
}
