package dev.builder.board.domain.model;

import dev.builder.core.domain.LocalEntity;
import dev.builder.usermanagement.domain.model.Student;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Representa una tarea dentro del dominio de la aplicación.
 *
 * Esta entidad mantiene información como título, descripción, color, orden,
 * fecha límite, periodo de ejecución, imagen de portada y adjuntos.
 *
 * Implementa Comparable para permitir ordenamiento basado en la propiedad {@code order}.
 *
 * La lógica de ordenamiento no es igual a la lógica de {@code equals}
 */
public class Task extends LocalEntity<Task.Id> implements Comparable<Task> {

    private Stage.Id stage;
    private Title title;
    private TaskDescription description;
    private Color color;
    private Order order;
    private LocalDateTime createdAt;
    private Deadline deadline;
    private ExecutionPeriod executionPeriod;
    private Image.Id coverImage;
    private final Set<Attachment.Id> attachments = new HashSet<>();
    private final Set<Student.Id>  assignedStudents = new HashSet<>();

    /**
     * Constructor con id explícito y atributos básicos.
     *
     * @param id Identificador único de la tarea
     * @param title Título de la tarea
     * @param description Descripción de la tarea
     * @param color Color asociado a la tarea
     * @param deadline Fecha límite para la tarea
     * @throws NullPointerException si alguno de los parámetros es nulo
     */
    private Task(Task.Id id, Stage.Id stage, Title title, TaskDescription description, Color color, Deadline deadline, Order order) {
        super(id);
        this.stage = Objects.requireNonNull(stage);
        this.title = Objects.requireNonNull(title);
        this.description = Objects.requireNonNull(description);
        this.color = Objects.requireNonNull(color);
        this.deadline = Objects.requireNonNull(deadline);
        this.createdAt = LocalDateTime.now();
        this.order = Objects.requireNonNull(order);
    }

    @Contract("_,_, _, _,_, _, _ -> new")
    static @NotNull Task createNew(Task.Id id, Stage.Id stage, Title title, TaskDescription description, Color color, Deadline deadline, Order order) {
        Task created = new Task(id, stage, title, description, color, deadline, order);
        if(!stage.state().equals(Stage.StageState.TO_DO)){
            created.start();
        }
        if(stage.state().isFinal()){
            created.finish();
        }
        return created;
    }
    /**
     * Constructor completo que incluye imagen de portada y adjuntos.
     *
     * @param id Identificador único de la tarea
     * @param stage El Id del tablero y etapa en la que se encuentra la tarea
     * @param title Título de la tarea
     * @param description Descripción de la tarea
     * @param order el órden de la tarea
     * @param color Color asociado a la tarea
     * @param deadline Fecha límite para la tarea
     * @param coverImage Imagen de portada
     * @param attachments Conjunto de archivos adjuntos
     * @throws NullPointerException si alguno de los parámetros es nulo
     */
    public Task(Task.Id id, Stage.Id stage, Title title, TaskDescription description, LocalDateTime createdAt, Order order, Color color, Deadline deadline, ExecutionPeriod executionPeriod, Image.Id coverImage, Set<Attachment.Id> attachments, Set<Student.Id> assignedStudents) {
        this(id, stage, title, description, color, deadline, order);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.coverImage = coverImage;
        this.executionPeriod = executionPeriod;
        Objects.requireNonNull(attachments);
        attachments.forEach(Objects::requireNonNull);
        this.attachments.addAll(attachments);
        Objects.requireNonNull(assignedStudents);
        assignedStudents.forEach(Objects::requireNonNull);
        this.assignedStudents.addAll(assignedStudents);
    }

    // Métodos para modificar atributos (con validación de no nulos)

    public void changeTitle(Title title) {
        this.title = Objects.requireNonNull(title);
    }

    public void changeDescription(TaskDescription description) {
        this.description = Objects.requireNonNull(description);
    }

    public void markWithColor(Color color) {
        this.color = Objects.requireNonNull(color);
    }

    public void changeDeadline(Deadline deadline) {
        this.deadline = Objects.requireNonNull(deadline);
    }

    public void changeCoverImage(Image.Id coverImage) {
        this.coverImage = Objects.requireNonNull(coverImage);
    }

    void changeOrder(Order order) {
        this.order = Objects.requireNonNull(order);
    }

    void changeStage(Stage.Id stage) {
        this.stage = Objects.requireNonNull(stage);
    }

    public Task hydratedWithAuditInfo(LocalDateTime createdAt){
        this.createdAt = Objects.requireNonNull(createdAt);
        return this;
    }
    /**
     * Añade un archivo adjunto a la tarea.
     *
     * @param attachment Archivo adjunto a agregar
     * @return true si fue agregado, false si ya existía.
     * @throws NullPointerException si el adjunto es nulo
     */
    public boolean addAttachment(Attachment.Id attachment) {
        return attachments.add(Objects.requireNonNull(attachment));
    }

    /**
     * Elimina un archivo adjunto.
     *
     * @param attachment Archivo adjunto a eliminar
     * @return true si fue eliminado, false si no existía.
     * @throws NullPointerException si el adjunto es nulo
     */
    public boolean removeAttachment(Attachment attachment) {
        return removeAttachment(attachment.id());
    }

    /**
     * Elimina un archivo adjunto identificado por su id.
     *
     * @param id id del archivo adjunto a eliminar
     * @return true si fue eliminado, false si no existía.
     * @throws NullPointerException si el adjunto es nulo
     */
    public boolean removeAttachment(Attachment.Id id){
        Objects.requireNonNull(id);
        return attachments.remove(Objects.requireNonNull(id));
    }

    /**
     * Asigna un estudiante a la tarea
     * @param studentId El id del estudiante
     * @return verdadero si fue agregado con éxito
     * @throws NullPointerException si el adjunto es nulo
     */
    public boolean assignStudent(Student.Id studentId){
        return assignedStudents.add(Objects.requireNonNull(studentId));
    }


    /**
     * Revoca la asignación de estudiante a la tarea
     * @param studentId el id del estudiante
     * @return verdadero si fue eliminado con éxito
     * @throws NullPointerException si el adjunto es nulo
     */
    public boolean revokeAssignation(Student.Id studentId){
        return assignedStudents.remove(Objects.requireNonNull(studentId));
    }

    /**
     * Marca el inicio del periodo de ejecución de la tarea.
     *
     * @throws IllegalStateException si la tarea ya había iniciado.
     */
    public void start(){
        start(LocalDateTime.now());
    }

    /**
     * Marca el inicio del periodo de ejecución de la tarea con una fecha y hora en específico
     *
     * @throws IllegalStateException si la tarea ya había iniciado.
     */
    public void start(LocalDateTime point){
        if(hasStarted()) throw new IllegalStateException("Task already started");
        if(hasFinished()) throw new IllegalStateException("Task already finished");
        executionPeriod = new ExecutionPeriod(point);
    }

    /**
     * Marca el fin del periodo de ejecución de la tarea.
     *
     * @throws IllegalStateException si la tarea no había iniciado o ya había terminado.
     */
    public void finish(){
        finish(LocalDateTime.now());
    }

    /**
     * Marca el fin del periodo de ejecución de la tarea con una hora y fecho específico
     *
     * @throws IllegalStateException si la tarea no había iniciado o ya había terminado.
     */
    public void finish(LocalDateTime point){
        if(!hasStarted()) throw new IllegalStateException("Task has not started");
        if(hasFinished()) throw new IllegalStateException("Task has already ended");
        executionPeriod = executionPeriod.withEnd(point);
    }

    /**
     * Indica si la tarea ha iniciado (si tiene periodo de ejecución).
     *
     * @return true si ha iniciado, false en caso contrario.
     */
    public boolean hasStarted(){
        return executionPeriod != null;
    }

    /**
     * Indica si la tarea está en progreso (si tiene periodo de ejecución pero aun sin fecha de fin).
     *
     * @return true si ha iniciado, false en caso contrario.
     */
    public boolean isInProgress(){
        return hasStarted() && executionPeriod.end().isEmpty();
    }

    /**
     * Indica si la tarea ha finalizado (periodo de ejecución con fecha de fin).
     *
     * @return true si ha finalizado, false en caso contrario.
     */
    public boolean hasFinished(){
        return hasStarted() && executionPeriod.end().isPresent();
    }

    /**
     * Indica si la tarea terminó después de la fecha límite (está atrasada).
     *
     * @return true si la tarea está atrasada, false en caso contrario.
     */
    public boolean isOverdue(){
        return executionPeriod != null &&
                executionPeriod.end().isPresent() &&
                executionPeriod.end().get().isAfter(deadline.value());
    }

    /**
     * Compara esta tarea con otra para ordenarlas según la propiedad {@code order}.
     *
     * @param task Tarea con la cual comparar.
     * @return un entero negativo, cero, o positivo dependiendo del orden.
     */
    @Override
    public int compareTo(@NotNull Task task) {
        return Double.compare(order.value(), task.order.value());
    }

    // Getters

    public Title title() {
        return title;
    }

    public Order order() {
        return order;
    }

    public TaskDescription description() {
        return description;
    }

    public Color color() {
        return color;
    }

    public Deadline deadline() {
        return deadline;
    }

    public Optional<ExecutionPeriod> executionPeriod() {
        return Optional.ofNullable(executionPeriod);
    }

    public Optional<Image.Id> coverImage() {
        return Optional.ofNullable(coverImage);
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    public Set<Attachment.Id> attachments() {
        return Collections.unmodifiableSet(attachments);
    }

    public Set<Student.Id> assignedStudents() {return Collections.unmodifiableSet(assignedStudents);}

    public Stage.Id stage() {
        return stage;
    }

    /**
     * Identificador único de la tarea, basado en UUID.
     */
    public record Id(UUID value){
        public Id{
            Objects.requireNonNull(value);
        }
    }
}
