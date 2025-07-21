package dev.builder.usermanagement.infrastructure;

import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.usermanagement.domain.model.Student;
import dev.builder.usermanagement.domain.port.out.StudentRepository;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

@Bean
public class JdbcStudentRepository implements StudentRepository {


    @Override
    public Optional<Student> findById(Student.Id id) {
        return Optional.empty();
    }

    @Override
    public Optional<Student> findById(Student.Id id, Connection connection) {
        return Optional.empty();
    }

    @Override
    public List<Student> findAll() {
        return List.of();
    }

    @Override
    public List<Student> findAll(Connection connection) {
        return List.of();
    }

    @Override
    public boolean delete(Student aggregateRoot) {
        return false;
    }

    @Override
    public boolean delete(Student aggregateRoot, Connection connection) {
        return false;
    }

    @Override
    public boolean deleteById(Student.Id id) {
        return false;
    }

    @Override
    public boolean deleteById(Student.Id id, Connection connection) {
        return false;
    }

    @Override
    public Student save(Student aggregateRoot) {
        return null;
    }

    @Override
    public Student save(Student aggregateRoot, Connection connection) {
        return null;
    }

    @Override
    public boolean existsById(Student.Id id) {
        return false;
    }

    @Override
    public boolean existsById(Student.Id id, Connection connection) {
        return false;
    }
}
