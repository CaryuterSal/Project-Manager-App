-- app_user
MERGE INTO app_user u
USING (SELECT 'admin@example.com' AS email FROM dual) src
ON (u.email = src.email)
WHEN NOT MATCHED THEN INSERT (email, password, type)
                      VALUES ('admin@example.com', 'adminpasshash', 'admin');

MERGE INTO app_user u
USING (SELECT 'manager1@example.com' AS email FROM dual) src
ON (u.email = src.email)
WHEN NOT MATCHED THEN INSERT (email, password, type)
                      VALUES ('manager1@example.com', 'managerpasshash', 'manager');

MERGE INTO app_user u
USING (SELECT 'student1@example.com' AS email FROM dual) src
ON (u.email = src.email)
WHEN NOT MATCHED THEN INSERT (email, password, type)
                      VALUES ('student1@example.com', 'studentpasshash', 'student');

MERGE INTO app_user u
USING (SELECT 'student2@example.com' AS email FROM dual) src
ON (u.email = src.email)
WHEN NOT MATCHED THEN INSERT (email, password, type)
                      VALUES ('student2@example.com', 'studentpasshash', 'student');

-- admin
MERGE INTO admin a
USING (SELECT 'admin@example.com' AS email FROM dual) src
ON (a.email = src.email)
WHEN NOT MATCHED THEN INSERT (email)
                      VALUES ('admin@example.com');

-- manager
MERGE INTO manager m
USING (SELECT 'manager1@example.com' AS email FROM dual) src
ON (m.email = src.email)
WHEN NOT MATCHED THEN INSERT (email, created_by)
                      VALUES ('manager1@example.com', 'admin@example.com');

-- academic_quarter
MERGE INTO academic_quarter aq
USING (SELECT 1 AS "number" FROM dual) src
ON (aq."number" = src."number")
WHEN NOT MATCHED THEN
    INSERT ("number") VALUES (src."number");

MERGE INTO academic_quarter aq
USING (SELECT 2 AS "number" FROM dual) src
ON (aq."number" = src."number")
WHEN NOT MATCHED THEN
    INSERT ("number") VALUES (src."number");

MERGE INTO academic_quarter aq
USING (SELECT 3 AS "number" FROM dual) src
ON (aq."number" = src."number")
WHEN NOT MATCHED THEN
    INSERT ("number") VALUES (src."number");

MERGE INTO academic_quarter aq
USING (SELECT 4 AS "number" FROM dual) src
ON (aq."number" = src."number")
WHEN NOT MATCHED THEN
    INSERT ("number") VALUES (src."number");


-- academic_group
MERGE INTO academic_group ag
USING (SELECT 'A' AS name FROM dual) src
ON (ag.name = src.name)
WHEN NOT MATCHED THEN INSERT (name) VALUES ('A');

MERGE INTO academic_group ag
USING (SELECT 'B' AS name FROM dual) src
ON (ag.name = src.name)
WHEN NOT MATCHED THEN INSERT (name) VALUES ('B');

MERGE INTO academic_group ag
USING (SELECT 'C' AS name FROM dual) src
ON (ag.name = src.name)
WHEN NOT MATCHED THEN INSERT (name) VALUES ('C');


-- quarter_group
MERGE INTO quarter_group qg
USING (SELECT 'A' AS agp_name, 1 AS aqr_number FROM dual) src
ON (qg.agp_name = src.agp_name AND qg.aqr_number = src.aqr_number)
WHEN NOT MATCHED THEN INSERT (agp_name, aqr_number) VALUES ('A', 1);

MERGE INTO quarter_group qg
USING (SELECT 'A' AS agp_name, 2 AS aqr_number FROM dual) src
ON (qg.agp_name = src.agp_name AND qg.aqr_number = src.aqr_number)
WHEN NOT MATCHED THEN INSERT (agp_name, aqr_number) VALUES ('A', 2);

MERGE INTO quarter_group qg
USING (SELECT 'B' AS agp_name, 3 AS aqr_number FROM dual) src
ON (qg.agp_name = src.agp_name AND qg.aqr_number = src.aqr_number)
WHEN NOT MATCHED THEN INSERT (agp_name, aqr_number) VALUES ('B', 3);

MERGE INTO quarter_group qg
USING (SELECT 'C' AS agp_name, 4 AS aqr_number FROM dual) src
ON (qg.agp_name = src.agp_name AND qg.aqr_number = src.aqr_number)
WHEN NOT MATCHED THEN INSERT (agp_name, aqr_number) VALUES ('C', 4);

-- students
MERGE INTO student s
USING (SELECT 'student1@example.com' AS email FROM dual) src
ON (s.email = src.email)
WHEN NOT MATCHED THEN INSERT (created_by, qgp_agp_name, qgp_aqr_number, email, first_name, last_name)
                      VALUES ('manager1@example.com', 'A', 1, 'student1@example.com', 'Juan', 'Perez');

MERGE INTO student s
USING (SELECT 'student2@example.com' AS email FROM dual) src
ON (s.email = src.email)
WHEN NOT MATCHED THEN INSERT (created_by, qgp_agp_name, qgp_aqr_number, email, first_name, last_name)
                      VALUES ('manager1@example.com', 'A', 2, 'student2@example.com', 'Maria', 'Gomez');

-- board
MERGE INTO board b
USING (SELECT 'manager1@example.com' AS mnr_email FROM dual) src
ON (b.mnr_email = src.mnr_email AND b.title = 'Project Alpha')
WHEN NOT MATCHED THEN INSERT (mnr_email, title)
                      VALUES ('manager1@example.com', 'Project Alpha');

-- board_stage
MERGE INTO board_stage bs
USING (SELECT 'manager1@example.com' AS bad_email, 'To Do' AS sae_name FROM dual) src
ON (bs.bad_email = src.bad_email AND bs.sae_name = src.sae_name)
WHEN NOT MATCHED THEN INSERT (bad_email, sae_name)
                      VALUES ('manager1@example.com', 'To Do');

MERGE INTO board_stage bs
USING (SELECT 'manager1@example.com' AS bad_email, 'In Progress' AS sae_name FROM dual) src
ON (bs.bad_email = src.bad_email AND bs.sae_name = src.sae_name)
WHEN NOT MATCHED THEN INSERT (bad_email, sae_name)
                      VALUES ('manager1@example.com', 'In Progress');

MERGE INTO board_stage bs
USING (SELECT 'manager1@example.com' AS bad_email, 'Done' AS sae_name FROM dual) src
ON (bs.bad_email = src.bad_email AND bs.sae_name = src.sae_name)
WHEN NOT MATCHED THEN INSERT (bad_email, sae_name)
                      VALUES ('manager1@example.com', 'Done');

-- ============================================
-- TASK
-- ============================================
MERGE INTO task t
USING (SELECT HEXTORAW('D1B2C3D4E5F60123456789ABCDEF0005') AS id FROM dual) src
ON (t.id = src.id)
WHEN NOT MATCHED THEN INSERT (id, title, description, clr_name, deadline)
                      VALUES (src.id, 'Setup repo', 'Initialize git repository and CI/CD', 'Blue', TO_DATE('2025-12-01 23:59:59', 'YYYY-MM-DD HH24:MI:SS'));

MERGE INTO task t
USING (SELECT HEXTORAW('D2B2C3D4E5F60123456789ABCDEF0006') AS id FROM dual) src
ON (t.id = src.id)
WHEN NOT MATCHED THEN INSERT (id, title, description, clr_name, deadline)
                      VALUES (src.id, 'Implement login', 'Create login module with JWT', 'Green', TO_DATE('2025-12-10 23:59:59', 'YYYY-MM-DD HH24:MI:SS'));

MERGE INTO task t
USING (SELECT HEXTORAW('D3B2C3D4E5F60123456789ABCDEF0007') AS id FROM dual) src
ON (t.id = src.id)
WHEN NOT MATCHED THEN INSERT (id, title, description, clr_name, deadline)
                      VALUES (src.id, 'Write tests', 'Unit tests for core modules', 'Yellow', TO_DATE('2025-12-15 23:59:59', 'YYYY-MM-DD HH24:MI:SS'));


-- ============================================
-- STAGE_TASK
-- ============================================
MERGE INTO stage_task st
USING (SELECT HEXTORAW('D1B2C3D4E5F60123456789ABCDEF0005') AS tsk_id FROM dual) src
ON (st.tsk_id = src.tsk_id AND st.bse_sae_name = 'To Do' AND st.bse_bad_email = 'manager1@example.com')
WHEN NOT MATCHED THEN INSERT (tsk_id, bse_sae_name, bse_bad_email, "order")
                      VALUES (src.tsk_id, 'To Do', 'manager1@example.com', 1);

MERGE INTO stage_task st
USING (SELECT HEXTORAW('D2B2C3D4E5F60123456789ABCDEF0006') AS tsk_id FROM dual) src
ON (st.tsk_id = src.tsk_id AND st.bse_sae_name = 'In Progress' AND st.bse_bad_email = 'manager1@example.com')
WHEN NOT MATCHED THEN INSERT (tsk_id, bse_sae_name, bse_bad_email, "order")
                      VALUES (src.tsk_id, 'In Progress', 'manager1@example.com', 2);

MERGE INTO stage_task st
USING (SELECT HEXTORAW('D3B2C3D4E5F60123456789ABCDEF0007') AS tsk_id FROM dual) src
ON (st.tsk_id = src.tsk_id AND st.bse_sae_name = 'To Do' AND st.bse_bad_email = 'manager1@example.com')
WHEN NOT MATCHED THEN INSERT (tsk_id, bse_sae_name, bse_bad_email, "order")
                      VALUES (src.tsk_id, 'To Do', 'manager1@example.com', 3);


-- ============================================
-- STUDENT_BOARD
-- ============================================
MERGE INTO student_board sb
USING (SELECT 'manager1@example.com' AS bad_email, 'student1@example.com' AS sdt_email FROM dual) src
ON (sb.bad_email = src.bad_email AND sb.sdt_email = src.sdt_email)
WHEN NOT MATCHED THEN INSERT (bad_email, sdt_email)
                      VALUES ('manager1@example.com', 'student1@example.com');

MERGE INTO student_board sb
USING (SELECT 'manager1@example.com' AS bad_email, 'student2@example.com' AS sdt_email FROM dual) src
ON (sb.bad_email = src.bad_email AND sb.sdt_email = src.sdt_email)
WHEN NOT MATCHED THEN INSERT (bad_email, sdt_email)
                      VALUES ('manager1@example.com', 'student2@example.com');


-- ============================================
-- TASK_ASSIGNEE
-- ============================================
MERGE INTO task_assignee ta
USING (SELECT 'manager1@example.com' AS sbd_bad_email, 'student1@example.com' AS sbd_sdt_email, HEXTORAW('D1B2C3D4E5F60123456789ABCDEF0005') AS tsk_id FROM dual) src
ON (ta.sbd_bad_email = src.sbd_bad_email AND ta.sbd_sdt_email = src.sbd_sdt_email AND ta.tsk_id = src.tsk_id)
WHEN NOT MATCHED THEN INSERT (sbd_bad_email, sbd_sdt_email, tsk_id)
                      VALUES (src.sbd_bad_email, src.sbd_sdt_email, src.tsk_id);

MERGE INTO task_assignee ta
USING (SELECT 'manager1@example.com' AS sbd_bad_email, 'student2@example.com' AS sbd_sdt_email, HEXTORAW('D2B2C3D4E5F60123456789ABCDEF0006') AS tsk_id FROM dual) src
ON (ta.sbd_bad_email = src.sbd_bad_email AND ta.sbd_sdt_email = src.sbd_sdt_email AND ta.tsk_id = src.tsk_id)
WHEN NOT MATCHED THEN INSERT (sbd_bad_email, sbd_sdt_email, tsk_id)
                      VALUES (src.sbd_bad_email, src.sbd_sdt_email, src.tsk_id);


-- ============================================
-- FILE
-- ============================================
MERGE INTO "FILE" f
USING (SELECT HEXTORAW('E1B2C3D4E5F60123456789ABCDEF0008') AS id FROM dual) src
ON (f.id = src.id)
WHEN NOT MATCHED THEN INSERT (id, name, mimetype, purpose)
                      VALUES (src.id, 'diagram.png', 'image/png', 'cover');

MERGE INTO "FILE" f
USING (SELECT HEXTORAW('E2B2C3D4E5F60123456789ABCDEF0009') AS id FROM dual) src
ON (f.id = src.id)
WHEN NOT MATCHED THEN INSERT (id, name, mimetype, purpose)
                      VALUES (src.id, 'requirements.pdf', 'application/pdf', 'attachement');


-- ============================================
-- TASK_COVER
-- ============================================
MERGE INTO task_cover tc
USING (SELECT HEXTORAW('E1B2C3D4E5F60123456789ABCDEF0008') AS fle_id, HEXTORAW('D1B2C3D4E5F60123456789ABCDEF0005') AS tsk_id FROM dual) src
ON (tc.fle_id = src.fle_id AND tc.tsk_id = src.tsk_id)
WHEN NOT MATCHED THEN INSERT (fle_id, tsk_id)
                      VALUES (src.fle_id, src.tsk_id);


-- ============================================
-- TASK_ATTACHMENT
-- ============================================
MERGE INTO task_attachement ta
USING (SELECT HEXTORAW('E2B2C3D4E5F60123456789ABCDEF0009') AS fle_id, HEXTORAW('D2B2C3D4E5F60123456789ABCDEF0006') AS tsk_id FROM dual) src
ON (ta.fle_id = src.fle_id AND ta.tsk_id = src.tsk_id)
WHEN NOT MATCHED THEN INSERT (fle_id, tsk_id)
                      VALUES (src.fle_id, src.tsk_id);
COMMIT;