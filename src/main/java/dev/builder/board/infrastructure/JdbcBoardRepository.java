package dev.builder.board.infrastructure;

import dev.builder.board.domain.model.Board;
import dev.builder.board.domain.port.out.BoardRepository;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.usermanagement.domain.port.out.UserRepository;


import java.util.List;
import java.util.Optional;

@Bean
public class JdbcBoardRepository implements BoardRepository {

    @Inject
    private UserRepository userRepository;

    @Override
    public Board save(Board aggregateRoot) {
        return null;
    }

    @Override
    public boolean delete(Board aggregateRoot) {
        return false;
    }

    @Override
    public boolean deleteById(Board.Id id) {
        return false;
    }

    @Override
    public List<Board> findAll() {
        return List.of();
    }

    @Override
    public Optional<Board> findById(Board.Id id) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(Board.Id id) {
        return false;
    }

    /*

    // Este metodo es para buscar un tablero por Id
    @Override
    public Optional<Board> findById(Board.Id id) {
        String query = "SELECT * FROM board WHERE mnr_email = ?";
        try (Connection conn = DefaultConnectionManager.getConnection();
        PreparedStatement ps = conn.prepareStatement(query)){

            ps.setString(1, id.uuid().toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()){
                Set<Stage .Id> stages = getStagesForBoard(id, conn);
                return Optional.of(new Board(id, stages));
            }
        } catch (SQLException e){
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public List<Board> findAll() {

        List<Board> boards = new ArrayList<>();
        String query = "SELECT * FROM board";

        try (Connection conn = DefaultConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Board.Id id = new Board.Id(UUID.fromString(rs.getString("mnr_email"))); // Ajusta si es necesario
                Set<Stage.Id> stages = getStagesForBoard(id, conn);
                boards.add(new Board(id, stages));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return boards;

    }

    private Set<Stage.Id> getStagesForBoard(Board.Id id, Connection conn) throws SQLException {
        Set<Stage.Id> stages = new HashSet<>();
        String query = "SELECT sae_NAME FROM board_stage WHERE bad_email = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, id.uuid().toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                stages.add(new Stage.Id(rs.getString("sae_NAME")));
            }
        }
        return stages;
    }

    // Este metodo elimina un tablero usando el ojeto
    @Override
    public void delete(Board aggregateRoot) {
        deleteById(aggregateRoot.id());

    }

    // Este metodo elimina un tablero por id
    @Override
    public void deleteById(Board.Id id) {
        String deleteStages = "DELETE FROM board_stage WHERE bad_email = ?";
        String deleteBoard = "DELETE FROM board WHERE mnr_email = ?";

        try (Connection conn = DefaultConnectionManager.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psStages = conn.prepareStatement(deleteStages);
                 PreparedStatement psBoard = conn.prepareStatement(deleteBoard)) {

                psStages.setString(1, id.uuid().toString());
                psStages.executeUpdate();

                psBoard.setString(1, id.uuid().toString());
                psBoard.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    // Este metodo crea o actualiza un tablero
    @Override
    public Board save(Board aggregateRoot) {
        if (existsById(aggregateRoot.id())) {
            return updateBoard(aggregateRoot);
        } else {
            return createBoard(aggregateRoot);
        }
    }

    //TODO: insertar columnas nuevas tambien
    private Board createBoard(Board aggregateRoot) {
        String insertBoard = "INSERT INTO board (mnr_email) VALUES (?)";
        try (Connection conn = DefaultConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertBoard)) {

            ps.setString(1, aggregateRoot.id().uuid().toString());
            ps.executeUpdate();
            return aggregateRoot;

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //TODO: Crear logica de actualizacion
    private Board updateBoard(Board aggregateRoot) {

    }

    // Este metodo verifica si existe un tablero con ese Id
    @Override
    public boolean existsById(Board.Id id) {

        String query = "SELECT 1 FROM board WHERE mnr_email = ?";
        try (Connection conn = DefaultConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, id.uuid().toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // true si encontró algo
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;

    }

 */
}
