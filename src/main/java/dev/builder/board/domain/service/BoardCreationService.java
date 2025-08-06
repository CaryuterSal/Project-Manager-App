package dev.builder.board.domain.service;

import dev.builder.board.domain.model.Board;
import dev.builder.board.domain.model.Stage;
import dev.builder.board.domain.model.Title;
import dev.builder.core.domain.DomainService;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.properties.MessageLocalizer;
import dev.builder.usermanagement.domain.model.Manager;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

@Bean
/**
 * Servicio de dominio que crea un nuevo tablero junto con sus etapas respectivas
 */
public class BoardCreationService implements DomainService {

    private final MessageLocalizer messageLocalizer;

    public BoardCreationService(MessageLocalizer messageLocalizer) {
        this.messageLocalizer = messageLocalizer;
    }

    /**
     * Crea un nuevo tablero y sus etapas respectivas a partir de un nuevo manager recién creado
     * @param owner el Manager al que le va a pertenecer el tablero
     * @return un nuevo talero
     * @throws NullPointerException si el id es nulo
     */
    public @NotNull Board createBoardForOwner(Manager.Id owner){
        Board.Id boardId = new Board.Id(owner);
        Set<Stage.Id> stages = new HashSet<>();
        for(Stage.StageState state : Stage.StageState.values()){
            stages.add(new Stage.Id(boardId, state));
        }
        return Board.createBoardForOwner(
                boardId,
                new Title(messageLocalizer.getMessage("board.title.default")),
                stages);
    }
}
