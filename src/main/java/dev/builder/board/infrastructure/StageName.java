package dev.builder.board.infrastructure;

import dev.builder.board.domain.model.Stage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public enum StageName {
    TO_DO("To Do"),
    IN_PROGRESS("In Progress"),
    DONE("Done");
    private final String dbValue;
    StageName(String dbValue) {
        this.dbValue = dbValue;
    }
    public String getDbValue() {return dbValue;}
    public static @NotNull StageName fromDbValue(String dbValue) {
        for (StageName stageName : StageName.values()) {
            if (stageName.dbValue.equals(dbValue)) {
                return stageName;
            }
        }
        throw new IllegalArgumentException("Stage name " + dbValue + " not found");
    }
    @Contract(pure = true)
    public static StageName fromDomain(Stage.StageState stageState) {
        return switch (stageState){
            case TO_DO -> TO_DO;
            case IN_PROGRESS -> IN_PROGRESS;
            case DONE -> DONE;
        };
    }
    public Stage.StageState asDomain(){
        return switch (this){
            case TO_DO -> Stage.StageState.TO_DO;
            case IN_PROGRESS -> Stage.StageState.IN_PROGRESS;
            case DONE -> Stage.StageState.DONE;
        };
    }
}
