-- Insert into app_user
INSERT INTO app_user (id, email, password, created_at, updated_at, active) VALUES
                                                                               (UUID_TO_BIN(UUID()), 'admin1@example.com', 'adminpass', NOW(), NOW(), 1),
                                                                               (UUID_TO_BIN(UUID()), 'manager1@example.com', 'managerpass', NOW(), NOW(), 1),
                                                                               (UUID_TO_BIN(UUID()), 'student1@example.com', 'studentpass', NOW(), NOW(), 1);

-- Reuse the inserted UUIDs
-- For demonstration, you may need to query their BIN values after insertion if needed for later inserts

-- Insert into email
INSERT INTO email (address) VALUES
                                ('admin1@example.com'),
                                ('manager1@example.com'),
                                ('student1@example.com');

-- Insert into admin
INSERT INTO admin (usr_id, email)
SELECT id, email FROM app_user WHERE email = 'admin1@example.com';

-- Insert into academic_quarter
INSERT INTO academic_quarter (number) VALUES (1), (2), (3), (4);

-- Insert into academic_group
INSERT INTO academic_group (name) VALUES ('A'), ('B');

-- Insert into quarter_group
INSERT INTO quarter_group (agp_name, aqr_number) VALUES
                                                     ('A', 1),
                                                     ('B', 2);

-- Insert into manager
INSERT INTO manager (usr_id, created_by, email)
SELECT
    m.id,
    a.id,
    m.email
FROM app_user m
         JOIN app_user a ON a.email = 'admin1@example.com'
WHERE m.email = 'manager1@example.com';

-- Insert into student
INSERT INTO student (usr_id, created_by, gpg_name, qgp_number, email, first_name, last_name)
SELECT
    s.id,
    m.usr_id,
    'A',
    1,
    s.email,
    'Alice',
    'Student'
FROM app_user s
         JOIN manager m ON m.email = 'manager1@example.com'
WHERE s.email = 'student1@example.com';

-- Insert into board
INSERT INTO board (mnr_id, title)
SELECT usr_id, 'Sprint 1 Board' FROM manager WHERE email = 'manager1@example.com';

-- Insert into board_stage (you must already have rows in stage, e.g. 'To Do', 'In Progress', 'Done')
INSERT INTO board_stage (bad_id, sge_name)
SELECT mnr_id, 'To Do' FROM board
UNION
SELECT mnr_id, 'In Progress' FROM board
UNION
SELECT mnr_id, 'Done' FROM board;

-- Insert into student_board
INSERT INTO student_board (brd_id, sdt_id)
SELECT b.mnr_id, s.usr_id
FROM board b
         JOIN student s ON s.email = 'student1@example.com';

-- Insert into task (requires color to be populated already)
INSERT INTO task (id, title, description, clr_name)
VALUES
    (UUID_TO_BIN(UUID()), 'Make report', 'Prepare the final report for project', 'Blue'),
    (UUID_TO_BIN(UUID()), 'Code review', 'Review student submission', 'Green');

-- Insert into task_asignee
INSERT INTO task_asignee (sbd_brd_id, sbd_sdt_id, tsk_id)
SELECT
    sb.brd_id,
    sb.sdt_id,
    t.id
FROM student_board sb
         JOIN task t ON t.title = 'Make report';

-- You may repeat or adjust task_asignee inserts as needed
