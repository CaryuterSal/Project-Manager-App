package dev.builder.board.domain.model;

import dev.builder.core.domain.AggregateRoot;
import dev.builder.core.domain.ValueObject;

import java.util.*;

/**
 * Representa una etapa (Stage) dentro de un tablero kanban
 *
 * Esta clase actúa como un {@link AggregateRoot} que agrupa un conjunto de tareas (Task).
 *
 * Cada Stage está identificado por un ID compuesto que incluye el ID del tablero
 * al que pertenece y el estado actual de la etapa.
 */
public class Stage extends AggregateRoot<Stage.Id> {

    private final TreeSet<Task> tasks = new TreeSet<>();

    private static final double taskOrderStep = 1000.0;
    private static final double reindexEpsilonTrigger = 0.001;

    /**
     * Crea una nueva instancia de Stage con un identificador único.
     *
     * @param id Identificador único de la etapa.
     * @throws NullPointerException Si el id es null.
     */
    public Stage(Stage.Id id) {
        super(id);
    }

    /**
     * Crea una nueva instancia de Stage con un identificador y un conjunto
     * inicial de tareas.
     *
     * @param id Identificador único de la etapa.
     * @param tasks Conjunto de tareas asociadas a la etapa (no puede ser null ni contener null).
     * @throws NullPointerException Si id o tasks es null o si tasks contiene elementos null.
     */
    public Stage(Stage.Id id, Set<Task> tasks) {
        this(id);
        Objects.requireNonNull(tasks, "tasks must not be null");
        tasks.forEach(Objects::requireNonNull);
        this.tasks.addAll(tasks);
    }

    /**
     * Devuelve un {@link TreeSet} inmodificable y ordenado de las tareas asociadas a esta etapa.
     *
     * @return Conjunto inmodificable de tareas.
     */
    public Set<Task> tasks() {
        return Collections.unmodifiableSet(tasks);
    }

    /**
     * Devuelve el estado del stage
     * @return enum del estado
     */
    public StageState state(){
        return id.state;
    }

    /**
     * Devuelve el ID del Board (tablero) al que pertenece el Stage
     * @return el ID del tablero
     */
    public Board.Id boardId(){
        return id.boardId;
    }

    public boolean containsTask(Task task){
        return tasks.contains(task);
    }

    /**
     * Añade una tarea al stage al final de la lista
     * @param task Tarea a agregar (no puede ser null).
     * @return true si la tarea fue añadida, false si ya existía.
     * @throws NullPointerException Si la tarea es null.
     */
    public boolean pushTask(Task task) {
        Objects.requireNonNull(task);
        double lastOrderValue = tasks.last().order().get().value();
        double newOrderValue = lastOrderValue + taskOrderStep;
        task.changeOrder(new Order(newOrderValue));
        return tasks.add(task);
    }

    /**
     * Añade una tarea al stage al inicio de la lista
     * @param task Tarea a agregar (no puede ser null).
     * @return true si la tarea fue añadida, false si ya existía.
     * @throws NullPointerException Si la tarea es null.
     */
    public boolean shiftTask(Task task) {
        Objects.requireNonNull(task);
        double firstOrderValue = tasks.first().order().get().value();
        double newOrderValue = firstOrderValue / 2;
        task.changeOrder(new Order(newOrderValue));
        return tasks.add(task);
    }

    /**
     * Remueve una tarea de la etapa.
     *
     * @param task Tarea a eliminar (no puede ser null).
     * @return true si la tarea fue removida, false si no existía.
     * @throws NullPointerException Si la tarea es null.
     */
    public boolean removeTask(Task task) {
        return tasks.remove(Objects.requireNonNull(task));
    }

    /**
     * Remueve una tarea de la etapa según su identificador.
     *
     * @param taskId Identificador de la tarea a eliminar (no puede ser null).
     * @return true si una tarea fue removida, false si no se encontró ninguna con ese id.
     * @throws NullPointerException Si taskId es null.
     */
    public boolean removeTask(Task.Id taskId) {
        Objects.requireNonNull(taskId, "taskId must not be null");
        return tasks.removeIf(el -> el.id().equals(taskId));
    }

    /**
     * Intercambia el orden entre dos tareas pertenecientes a este Stage.
     * @throws NullPointerException Si la tarea es null.
     * @throws IllegalStateException si algunas de las tareas no existen en
     * @throws IllegalArgumentException si la tarea previa está después de la tarea siguiente
     */
    public void swapTaskOrder(Task source, Task target) {
        validateTasksInStage(source, target);
        Order originalSourceOrder = source.order().get();
        source.changeOrder(target.order().get());
        target.changeOrder(originalSourceOrder);
    }

    /**
     * agrega o cambia la tarea source para que quede entre previous y next.
     * previous o next pueden ser null si source es al principio o final.
     * @return si la tarea fue agregada exitosamente o cambió de órden, falso si nada de lo anterior sucedió
     * @throws NullPointerException Si la tarea es null.
     * @throws IllegalStateException si las tareas previa y/o posterior no existen en el Stage
     * @throws IllegalArgumentException si la tarea previa está después de la tarea siguiente
     */
    public boolean placeTaskBetween(Task source, Task previous, Task next) {
        validateTasksInStage(previous, next);

        double previousOrderValue =  previous.order().get().value();
        double nextOrderValue =  next.order().get().value();

        if(previousOrderValue >= nextOrderValue) throw new IllegalArgumentException("previous task must not be placed after next task");

        double range = nextOrderValue - previousOrderValue;
        double average = range / 2;
        double newOrderValue = average + previousOrderValue;
        if(average < reindexEpsilonTrigger){
            reindexOrder();
            return placeTaskBetween(source, previous, next);
        }

        double originalOrderValue = source.order().map(Order::value).orElse(Double.NaN);
        source.changeOrder(new Order(newOrderValue));
        return (!Double.isNaN(originalOrderValue) && Double.compare(originalOrderValue, newOrderValue) != 0) || tasks.add(source);
    }

    /**
     * Valida que las tareas especificadas están dentro de este Stage
     * @param tasks las tareas a verificar
     * @throws NullPointerException si alguna de las tareas es nula
     * @throws IllegalArgumentException si al menos una de las tareas no está dentro del Stage
     */
    public void validateTasksInStage(Task... tasks){
        Objects.requireNonNull(tasks, "tasks must not be null");
        Arrays.stream(tasks).forEach(Objects::requireNonNull);
        if(!areAllTasksInStage(tasks)){
            throw new IllegalStateException("one or more tasks are not in stage");
        }
    }

    /**
     * Checa si las tareas especificadas están dentro del Stage
     * @param tasks las tareas a verificar
     * @return true si todas las tares pertenecen a este Stage, falso de otra forma
     */
    public boolean areAllTasksInStage(Task... tasks){
        for (Task task : tasks) {
            if(!(this.tasks.contains(task))){
                return false;
            }
        }
        return true;
    }

    /**
     * Normaliza el orden de las tareas del Stage,
     * reasignando valores espaciados para el campo Order.
     */
    private void reindexOrder() {
        int i = 1;
        List<Task> taskList = new ArrayList<>(tasks);

        tasks.clear();

        for (Task task : taskList) {
            Order newOrder = new Order(i * taskOrderStep);
            task.changeOrder(newOrder);
            tasks.add(task);
            i++;
        }
    }

    /**
     * Identificador compuesto de la etapa, formado por el ID del tablero
     * al que pertenece y el estado actual de la etapa.
     */
    public record Id(Board.Id boardId, StageState state) implements ValueObject {

        /**
         * Crea un nuevo Id validando que los componentes no sean nulos.
         *
         * @param boardId Identificador del tablero padre (no puede ser null).
         * @param state Estado de la etapa (no puede ser null).
         * @throws NullPointerException Si boardId o state son null.
         * @throws IllegalArgumentException Si cualquiera de los valores no es válido.
         */
        public Id {
            validate(boardId, state);
        }

        public Id(String saeName) {
            this(new Board.Id("default"), StageState.valueOf(saeName));
        }

        /**
         * Valida que los parámetros no sean nulos y sean válidos.
         *
         * @param boardId Identificador del tablero.
         * @param state Estado de la etapa.
         * @throws NullPointerException Si alguno es null.
         * @throws IllegalArgumentException Si no son válidos.
         */
        public static void validate(Board.Id boardId, StageState state) {
            if (!isValid(Objects.requireNonNull(boardId), Objects.requireNonNull(state))) {
                throw new IllegalArgumentException("Stage id not valid");
            }
        }

        /**
         * Comprueba la validez de los valores del identificador.
         *
         * @param boardId Identificador del tablero.
         * @param state Estado de la etapa.
         * @return true si ambos son no nulos, false en caso contrario.
         */
        public static boolean isValid(Board.Id boardId, StageState state) {
            return boardId != null && state != null;
        }
    }

    /**
     * Enum que representa los posibles estados de una etapa.
     *
     * Cada estado indica si la etapa es final (completada).
     */
    public enum StageState implements ValueObject {

        TO_DO(false),
        IN_PROGRESS(false),
        DONE(true);

        private final boolean isFinal;

        StageState(boolean isFinal) {
            this.isFinal = isFinal;
        }

        /**
         * Indica si el estado representa una etapa finalizada.
         *
         * @return true si el estado es final, false en caso contrario.
         */
        public boolean isFinal() {
            return isFinal;
        }

        /**
         * Devuelve una representación en minúsculas del nombre del estado,
         * reemplazando guiones bajos por espacios.
         *
         * @return Cadena con el nombre del estado en formato amigable.
         */
        @Override
        public String toString() {
            return this.name().toLowerCase();
        }
    }
}