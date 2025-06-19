package dev.builder.board.domain.model;

import dev.builder.core.domain.LocalEntity;
import dev.builder.usermanagement.domain.GlobalIdentityManager;
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

    private TaskTitle title;
    private TaskDescription description;
    private Color color;
    private Order order;
    private final LocalDateTime createdAt;
    private Deadline deadline;
    private ExecutionPeriod executionPeriod;
    private Image coverImage;
    private final Set<Attachment> attachments = new HashSet<>();

    /**
     * Constructor para crear una tarea con título, descripción, color y fecha límite.
     * El id se genera automáticamente.
     *
     * @param title Título de la tarea
     * @param description Descripción de la tarea
     * @param color Color asociado a la tarea
     * @param deadline Fecha límite para la tarea
     * @throws NullPointerException si alguno de los parámetros es nulo
     */
    public Task(TaskTitle title, TaskDescription description, Color color, Deadline deadline) {
        this(new Task.Id(GlobalIdentityManager.generateUUID()), title, description, color, deadline);
    }

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
    public Task(Task.Id id, TaskTitle title, TaskDescription description, Color color, Deadline deadline) {
        super(id);
        this.title = Objects.requireNonNull(title);
        this.description = Objects.requireNonNull(description);
        this.color = Objects.requireNonNull(color);
        this.deadline = Objects.requireNonNull(deadline);
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Constructor completo que incluye imagen de portada y adjuntos.
     *
     * @param id Identificador único de la tarea
     * @param title Título de la tarea
     * @param description Descripción de la tarea
     * @param order el órden de la tarea
     * @param color Color asociado a la tarea
     * @param deadline Fecha límite para la tarea
     * @param coverImage Imagen de portada
     * @param attachments Conjunto de archivos adjuntos
     * @throws NullPointerException si alguno de los parámetros es nulo
     */
    public Task(Task.Id id, TaskTitle title, TaskDescription description, Order order, Color color, Deadline deadline, Image coverImage, Set<Attachment> attachments) {
        this(id, title, description, color, deadline);
        this.order = Objects.requireNonNull(order);
        this.coverImage = Objects.requireNonNull(coverImage);
        Objects.requireNonNull(attachments);
        attachments.forEach(Objects::requireNonNull);
        this.attachments.addAll(attachments);
    }

    // Métodos para modificar atributos (con validación de no nulos)

    public void changeTitle(TaskTitle title) {
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

    public void changeCoverImage(Image coverImage) {
        this.coverImage = Objects.requireNonNull(coverImage);
    }

    public void changeOrder(Order order) {
        this.order = Objects.requireNonNull(order);
    }

    /**
     * Añade un archivo adjunto a la tarea.
     *
     * @param attachment Archivo adjunto a agregar
     * @return true si fue agregado, false si ya existía.
     * @throws NullPointerException si el adjunto es nulo
     */
    public boolean addAttachment(Attachment attachment) {
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
        return attachments.remove(Objects.requireNonNull(attachment));
    }

    /**
     * Elimina un archivo adjunto identificado por su nombre.
     *
     * @param filename Nombre del archivo adjunto a eliminar
     * @return true si fue eliminado, false si no existía.
     * @throws NullPointerException si el adjunto es nulo
     */
    public boolean removeAttachment(StoredFile.Filename filename){
        Objects.requireNonNull(filename);
        return attachments.removeIf(el -> el.name().equals(filename));
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
        // Considerar que cast a int puede causar truncamiento si el valor es muy grande
        return (int)(order.value() - task.order.value());
    }

    // Getters

    public Task.Id id() {
        return id;
    }

    public TaskTitle title() {
        return title;
    }

    public Optional<Order> order() {
        return Optional.ofNullable(order);
    }

    public TaskDescription description() {
        return description;
    }

    public Optional<Color> color() {
        return Optional.ofNullable(color);
    }

    public Deadline deadline() {
        return deadline;
    }

    public Optional<ExecutionPeriod> executionPeriod() {
        return Optional.ofNullable(executionPeriod);
    }

    public Optional<Image> coverImage() {
        return Optional.ofNullable(coverImage);
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    public Set<StoredFile> attachments() {
        return Collections.unmodifiableSet(attachments);
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
