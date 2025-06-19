USE kedu_project_manager;

-- Insertar emails
INSERT INTO email (address) VALUES
                                ('admin@example.com'),
                                ('manager1@example.com'),
                                ('student1@example.com'),
                                ('student2@example.com');

-- Insertar usuarios en app_user (id 16 bytes hex)
INSERT INTO app_user (id, email, password) VALUES
                                               (UNHEX(REPLACE('a1b2c3d4e5f60123456789abcdef0001', '-', '')), 'admin@example.com', 'adminpasshash'),
                                               (UNHEX(REPLACE('b1b2c3d4e5f60123456789abcdef0002', '-', '')), 'manager1@example.com', 'managerpasshash'),
                                               (UNHEX(REPLACE('c1b2c3d4e5f60123456789abcdef0003', '-', '')), 'student1@example.com', 'studentpasshash'),
                                               (UNHEX(REPLACE('c2b2c3d4e5f60123456789abcdef0004', '-', '')), 'student2@example.com', 'studentpasshash');

-- Insertar admin (usr_id y email deben existir en app_user y email)
INSERT INTO admin (usr_id, email) VALUES
    (UNHEX(REPLACE('a1b2c3d4e5f60123456789abcdef0001', '-', '')), 'admin@example.com');

-- Insertar manager (usr_id, created_by, email)
INSERT INTO manager (usr_id, created_by, email) VALUES
    (UNHEX(REPLACE('b1b2c3d4e5f60123456789abcdef0002', '-', '')), UNHEX(REPLACE('a1b2c3d4e5f60123456789abcdef0001', '-', '')), 'manager1@example.com');

-- Insertar academic_quarter
INSERT INTO academic_quarter (number) VALUES (1), (2), (3), (4);

-- Insertar academic_group
INSERT INTO academic_group (name) VALUES ('A'), ('B'), ('C');

-- Insertar quarter_group (relación académica)
INSERT INTO quarter_group (agp_name, aqr_number) VALUES
                                                     ('A', 1),
                                                     ('A', 2),
                                                     ('B', 3),
                                                     ('C', 4);

-- Insertar students
INSERT INTO student (usr_id, created_by, gpg_name, qgp_number, email, first_name, last_name) VALUES
                                                                                                 (UNHEX(REPLACE('c1b2c3d4e5f60123456789abcdef0003', '-', '')), UNHEX(REPLACE('b1b2c3d4e5f60123456789abcdef0002', '-', '')), 'A', 1, 'student1@example.com', 'Juan', 'Perez'),
                                                                                                 (UNHEX(REPLACE('c2b2c3d4e5f60123456789abcdef0004', '-', '')), UNHEX(REPLACE('b1b2c3d4e5f60123456789abcdef0002', '-', '')), 'A', 2, 'student2@example.com', 'Maria', 'Gomez');

-- Insertar board (mnr_id = manager usr_id)
INSERT INTO board (mnr_id, title) VALUES
    (UNHEX(REPLACE('b1b2c3d4e5f60123456789abcdef0002', '-', '')), 'Project Alpha');

-- Insertar board_stage (bad_id = board mnr_id)
INSERT INTO board_stage (bad_id, sge_name) VALUES
                                               (UNHEX(REPLACE('b1b2c3d4e5f60123456789abcdef0002', '-', '')), 'To Do'),
                                               (UNHEX(REPLACE('b1b2c3d4e5f60123456789abcdef0002', '-', '')), 'In Progress'),
                                               (UNHEX(REPLACE('b1b2c3d4e5f60123456789abcdef0002', '-', '')), 'Done');

-- Insertar tareas
INSERT INTO task (id, title, description, clr_name, deadline) VALUES
                                                                  (UNHEX(REPLACE('d1b2c3d4e5f60123456789abcdef0005', '-', '')), 'Setup repo', 'Initialize git repository and CI/CD', 'Blue', '2025-12-01 23:59:59'),
                                                                  (UNHEX(REPLACE('d2b2c3d4e5f60123456789abcdef0006', '-', '')), 'Implement login', 'Create login module with JWT', 'Green', '2025-12-10 23:59:59'),
                                                                  (UNHEX(REPLACE('d3b2c3d4e5f60123456789abcdef0007', '-', '')), 'Write tests', 'Unit tests for core modules', 'Yellow', '2025-12-15 23:59:59');

-- Insertar stage_task para asignar tareas a stages y board
INSERT INTO stage_task (tsk_id, bse_sge_name, bse_bad_id, `order`) VALUES
                                                                       (UNHEX(REPLACE('d1b2c3d4e5f60123456789abcdef0005', '-', '')), 'To Do', UNHEX(REPLACE('b1b2c3d4e5f60123456789abcdef0002', '-', '')), 1.0000),
                                                                       (UNHEX(REPLACE('d2b2c3d4e5f60123456789abcdef0006', '-', '')), 'In Progress', UNHEX(REPLACE('b1b2c3d4e5f60123456789abcdef0002', '-', '')), 2.0000),
                                                                       (UNHEX(REPLACE('d3b2c3d4e5f60123456789abcdef0007', '-', '')), 'To Do', UNHEX(REPLACE('b1b2c3d4e5f60123456789abcdef0002', '-', '')), 3.0000);

-- Insertar student_board (relación entre student y board)
INSERT INTO student_board (brd_id, sdt_id) VALUES
                                               (UNHEX(REPLACE('b1b2c3d4e5f60123456789abcdef0002', '-', '')), UNHEX(REPLACE('c1b2c3d4e5f60123456789abcdef0003', '-', ''))),
                                               (UNHEX(REPLACE('b1b2c3d4e5f60123456789abcdef0002', '-', '')), UNHEX(REPLACE('c2b2c3d4e5f60123456789abcdef0004', '-', '')));

-- Insertar task_asignee (asignar tareas a estudiantes en board)
INSERT INTO task_asignee (sbd_brd_id, sbd_sdt_id, tsk_id) VALUES
                                                              (UNHEX(REPLACE('b1b2c3d4e5f60123456789abcdef0002', '-', '')), UNHEX(REPLACE('c1b2c3d4e5f60123456789abcdef0003', '-', '')), UNHEX(REPLACE('d1b2c3d4e5f60123456789abcdef0005', '-', ''))),
                                                              (UNHEX(REPLACE('b1b2c3d4e5f60123456789abcdef0002', '-', '')), UNHEX(REPLACE('c2b2c3d4e5f60123456789abcdef0004', '-', '')), UNHEX(REPLACE('d2b2c3d4e5f60123456789abcdef0006', '-', '')));

-- Insertar archivos de ejemplo para task_cover y task_attachment
INSERT INTO file (id, name, mimetype) VALUES
                                          (UNHEX(REPLACE('e1b2c3d4e5f60123456789abcdef0008', '-', '')), 'diagram.png', 'image/png'),
                                          (UNHEX(REPLACE('e2b2c3d4e5f60123456789abcdef0009', '-', '')), 'requirements.pdf', 'application/pdf');

INSERT INTO task_cover (id, tsk_id) VALUES
    (UNHEX(REPLACE('e1b2c3d4e5f60123456789abcdef0008', '-', '')), UNHEX(REPLACE('d1b2c3d4e5f60123456789abcdef0005', '-', '')));

INSERT INTO task_attachment (id, tsk_id) VALUES
    (UNHEX(REPLACE('e2b2c3d4e5f60123456789abcdef0009', '-', '')), UNHEX(REPLACE('d2b2c3d4e5f60123456789abcdef0006', '-', '')));
