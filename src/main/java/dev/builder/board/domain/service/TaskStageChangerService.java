package dev.builder.board.domain.service;

import dev.builder.board.domain.model.Stage;
import dev.builder.board.domain.model.Task;
import dev.builder.core.domain.DomainService;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.properties.MessageLocalizer;

public class TaskStageChangerService implements DomainService {

    public static boolean moveTask(Stage sourceStage, Stage targetStage, Task sourceTask, Task previousTask, Task nextTask) {
        if(!sourceStage.containsTask(sourceTask)){
            throw new IllegalArgumentException("La tarea a mover no pertenece a dicha etapa");
        }

        boolean updated;
        if(sourceTask.hasFinished()) {
            return  false;
        } else if(sourceStage.equals(targetStage)){
             updated = sourceStage.placeTaskBetween(sourceTask,previousTask, nextTask);
        } else {
            sourceStage.removeTask(sourceTask);
            targetStage.placeTaskBetween(sourceTask, previousTask, nextTask);
            updated = true;
        }
        if(updated && !targetStage.state().equals(Stage.StageState.TO_DO) && !sourceTask.hasStarted()){
            sourceTask.start();
        }
        if(updated && targetStage.state().isFinal()){
            sourceTask.finish();
        }
        return updated;
    }

}
