BEGIN;

-- ethesis.users
INSERT INTO ethesis.users (id, created_at, last_modified_at, email, first_name, last_name)
VALUES ('c11b8162-898e-42f4-8a43-41e9b275a507', CURRENT_TIMESTAMP, NULL, 'alice.student@example.com', 'Alice',
        'Wonderland'),
       ('d22c9273-9a9f-4305-9b54-52f0c386b618', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - interval '1 day',
        'bob.student@example.com', 'Bob', 'Builder'),
       ('e33da384-ab00-4416-ac65-6301d497c729', CURRENT_TIMESTAMP, NULL, 'carlos.student@example.com', 'Carlos',
        'Santana'),
       ('f44eb495-bc11-4527-bd76-7412e5a8d83a', CURRENT_TIMESTAMP, NULL, 'carol.teacher@example.com', 'Carol',
        'Danvers'),
       ('a55fc5a6-cd22-4638-ce87-8523f6b9e94b', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - interval '2 days',
        'dave.teacher@example.com', 'David', 'Banner'),
       ('b660d6b7-de33-4749-df98-963407ca0a5c', CURRENT_TIMESTAMP, NULL, 'eva.teacher@example.com', 'Eva', 'Peron'),
       ('c771e7c8-ef44-485a-e0a9-a74518db1b6d', CURRENT_TIMESTAMP, NULL, 'frank.admin@example.com', 'Frank', 'Castle');

-- ethesis.departments
INSERT INTO ethesis.departments (id, created_at, last_modified_at, description, name)
VALUES ('1a82f8d9-0055-496b-f1ba-b85629ec2c7e', CURRENT_TIMESTAMP, NULL,
        'Department of Computer Science and Engineering', 'Computer Science'),
       ('2b9309ea-1166-4a7c-02cb-c9673ae03d8f', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - interval '5 hours',
        'Department of Mathematics and Statistics', 'Mathematics'),
       ('3ca41afb-2277-4b8d-13dc-da784bf14e90', CURRENT_TIMESTAMP, NULL, 'Department of Physics', 'Physics');

-- ethesis.students (depends on users)
-- Adjusted university_id to match CHECK constraint: '^f[0-9]{6}$'
INSERT INTO ethesis.students (id, created_at, last_modified_at, student_type, university_id)
VALUES ('c11b8162-898e-42f4-8a43-41e9b275a507', CURRENT_TIMESTAMP, NULL, 'LOCAL', 'f123456'),
       ('d22c9273-9a9f-4305-9b54-52f0c386b618', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - interval '1 day', 'ERASMUS',
        'f234567'),
       ('e33da384-ab00-4416-ac65-6301d497c729', CURRENT_TIMESTAMP, NULL, 'FOREIGN', 'f345678');

-- ethesis.teachers (depends on users)
INSERT INTO ethesis.teachers (id, created_at, last_modified_at, "position")
VALUES ('f44eb495-bc11-4527-bd76-7412e5a8d83a', CURRENT_TIMESTAMP, NULL, 'PROFESSOR'),
       ('a55fc5a6-cd22-4638-ce87-8523f6b9e94b', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - interval '2 days',
        'ASSOCIATE_PROFESSOR'),
       ('b660d6b7-de33-4749-df98-963407ca0a5c', CURRENT_TIMESTAMP, NULL, 'ASSISTANT_PROFESSOR');

-- ethesis.thesis_proposals (depends on students, teachers)
INSERT INTO ethesis.thesis_proposals (id, created_at, last_modified_at, goal, objectives, status, technology, title,
                                      student_id, teacher_id)
VALUES ('4db52b0c-3388-4c9e-24ed-eb895c025fa1', CURRENT_TIMESTAMP, NULL, 'Develop an AI for automated grading.',
        '1. Research existing models. 2. Implement a prototype. 3. Test and evaluate.', 'APPROVED',
        'Python, TensorFlow, NLP', 'AI-Driven Automated Grading System', 'c11b8162-898e-42f4-8a43-41e9b275a507',
        'f44eb495-bc11-4527-bd76-7412e5a8d83a'),
       ('5ec63c1d-4499-4daf-35fe-fc9a6d1360b2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - interval '10 days',
        'Explore new cryptographic algorithms.', '1. Study post-quantum crypto. 2. Propose a new scheme.', 'APPROVED',
        'LaTeX, SageMath', 'Post-Quantum Cryptography Analysis', 'd22c9273-9a9f-4305-9b54-52f0c386b618',
        'a55fc5a6-cd22-4638-ce87-8523f6b9e94b'),
       ('6fd74d2e-55aa-4eb0-460f-0da07e2471c3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - interval '5 days',
        'Statistical analysis of climate data.', '1. Collect data. 2. Apply models. 3. Interpret results.', 'REJECTED',
        'R, Python', 'Climate Change Impact Study', 'c11b8162-898e-42f4-8a43-41e9b275a507',
        'a55fc5a6-cd22-4638-ce87-8523f6b9e94b'),
       ('70e85e3f-66bb-4fc1-5710-1eb18f3582d4', CURRENT_TIMESTAMP, NULL, 'Build a simple quantum circuit simulator.',
        '1. Learn Qiskit. 2. Implement basic gates. 3. Simulate a small algorithm.', 'PENDING', 'Python, Qiskit',
        'Quantum Circuit Simulation', 'e33da384-ab00-4416-ac65-6301d497c729', 'b660d6b7-de33-4749-df98-963407ca0a5c');

-- ethesis.theses (depends on thesis_proposals)
INSERT INTO ethesis.theses (id, created_at, last_modified_at, final_grade, pdf_path, status, proposal_id)
VALUES ('81f96f40-77cc-40d2-6821-2fc2904693e5', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - interval '1 week', 4.5,
        '/theses/alice_ai_grading.pdf', 'DEFENDED', '4db52b0c-3388-4c9e-24ed-eb895c025fa1'),
       ('920a7051-88dd-41e3-7932-30d3a157a4f6', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - interval '3 days', NULL,
        '/theses/bob_crypto_analysis.pdf', 'WAITING_FOR_DEFENSE', '5ec63c1d-4499-4daf-35fe-fc9a6d1360b2'),
       ('a31b8162-99ee-42f4-8a43-41e4b268b507', CURRENT_TIMESTAMP, NULL, NULL, '/theses/carlos_qcomp_sim.pdf',
        'WAITING_FOR_REVIEW', '70e85e3f-66bb-4fc1-5710-1eb18f3582d4');

-- ethesis.reviews (depends on teachers)
INSERT INTO ethesis.reviews (id, created_at, last_modified_at, conclusion, content, teacher_id)
VALUES ('b42c9273-aa00-4305-9b54-52f5c379c618', CURRENT_TIMESTAMP, NULL, 'ACCEPTED',
        'The student demonstrated a strong understanding of AI principles and applied them effectively. The prototype is promising. Thesis is accepted for defense.',
        'f44eb495-bc11-4527-bd76-7412e5a8d83a'),
       ('c53da384-bb11-4416-ac65-6306d48ad729', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - interval '1 hour', 'ACCEPTED',
        'The literature review is comprehensive, but the proposed scheme requires more rigorous analysis regarding potential vulnerabilities. However, it meets the criteria for defense.',
        'a55fc5a6-cd22-4638-ce87-8523f6b9e94b'),
       ('d64eb495-cc22-4527-bd76-7417e59be83a', CURRENT_TIMESTAMP, NULL, 'REJECTED',
        'The initial submission lacks sufficient theoretical background and the implementation is incomplete. Significant revisions are required.',
        'b660d6b7-de33-4749-df98-963407ca0a5c');

-- ethesis.comments (depends on reviews, users)
INSERT INTO ethesis.comments (id, created_at, last_modified_at, content, review_id, user_id)
VALUES ('4db52b0c-3399-4c9e-24ed-eb8e5c025fa1', CURRENT_TIMESTAMP, NULL,
        'I agree with the assessment. The results are quite impressive for Alice''s thesis.',
        'b42c9273-aa00-4305-9b54-52f5c379c618', 'a55fc5a6-cd22-4638-ce87-8523f6b9e94b'),
       ('5ec63c1d-44aa-4daf-35fe-fc9f6d1360b2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - interval '30 minutes',
        'Thank you for the feedback on the crypto review, Professor!', 'c53da384-bb11-4416-ac65-6306d48ad729',
        'd22c9273-9a9f-4305-9b54-52f0c386b618'),
       ('6fd74d2e-55bb-4eb0-460f-0da07e2471c3', CURRENT_TIMESTAMP, NULL,
        'I will work on the revisions for the quantum computing thesis as per the review.',
        'd64eb495-cc22-4527-bd76-7417e59be83a', 'e33da384-ab00-4416-ac65-6301d497c729');

-- ethesis.defenses
INSERT INTO ethesis.defenses (id, created_at, last_modified_at, date, location)
VALUES ('e75fc5a6-dd33-4638-ce87-8528f6ace94b', CURRENT_TIMESTAMP, NULL, '2024-07-15', 'Room C101, CS Building'),
       ('f860d6b7-ee44-4749-df98-963907db0a5c', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - interval '2 days', '2024-08-01',
        'Virtual Meeting Room 3'),
       ('0971e7c8-ff55-485a-e0a9-a74a18dc1b6d', CURRENT_TIMESTAMP, NULL, '2024-09-10', 'Room P205, Physics Building');

-- ethesis.defense_sessions (depends on defenses, theses)
INSERT INTO ethesis.defense_sessions (id, created_at, last_modified_at, date_and_time, notes, defense_id, thesis_id)
VALUES ('1a82f8d9-0066-496b-f1ba-b85b29ed2c7e', CURRENT_TIMESTAMP, NULL, '2024-07-15 10:00:00+02',
        'Defense for Alice Wonderland thesis (AI).', 'e75fc5a6-dd33-4638-ce87-8528f6ace94b',
        '81f96f40-77cc-40d2-6821-2fc2904693e5'),
       ('2b9309ea-1177-4a7c-02cb-c96c3ae03d8f', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - interval '1 day',
        '2024-08-01 14:00:00+02', 'Defense for Bob Builder thesis (Crypto).', 'f860d6b7-ee44-4749-df98-963907db0a5c',
        '920a7051-88dd-41e3-7932-30d3a157a4f6'),
       ('3ca41afb-2288-4b8d-13dc-da7d4bf14e90', CURRENT_TIMESTAMP, NULL, '2024-09-10 09:00:00+02',
        'Scheduled for Carlos Santana thesis (QComp), pending review acceptance.',
        '0971e7c8-ff55-485a-e0a9-a74a18dc1b6d', 'a31b8162-99ee-42f4-8a43-41e4b268b507');

-- ethesis.defense_session_professors (depends on defense_sessions, teachers)
-- Grades are between 0 and 100
INSERT INTO ethesis.defense_session_professors (created_at, last_modified_at, grade, thoughts, defense_session_id,
                                                professor_id)
VALUES (CURRENT_TIMESTAMP, NULL, 92,
        'Excellent presentation and Q&A handling for AI thesis. Strong grasp of the subject.',
        '1a82f8d9-0066-496b-f1ba-b85b29ed2c7e', 'f44eb495-bc11-4527-bd76-7412e5a8d83a'),
       (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - interval '5 minutes', 88,
        'Very good defense for AI thesis. Some minor points on future work could be expanded.',
        '1a82f8d9-0066-496b-f1ba-b85b29ed2c7e', 'a55fc5a6-cd22-4638-ce87-8523f6b9e94b'),
       (CURRENT_TIMESTAMP, NULL, NULL, 'Awaiting defense.', '2b9309ea-1177-4a7c-02cb-c96c3ae03d8f',
        'a55fc5a6-cd22-4638-ce87-8523f6b9e94b'),
       (CURRENT_TIMESTAMP, NULL, NULL, 'Awaiting defense, committee member.', '2b9309ea-1177-4a7c-02cb-c96c3ae03d8f',
        'f44eb495-bc11-4527-bd76-7412e5a8d83a'),
       (CURRENT_TIMESTAMP, NULL, NULL, 'Awaiting defense, thesis supervisor.', '3ca41afb-2288-4b8d-13dc-da7d4bf14e90',
        'b660d6b7-de33-4749-df98-963407ca0a5c');

-- ethesis.department_appointments (depends on users, departments)
INSERT INTO ethesis.department_appointments (id, created_at, last_modified_at, end_date, start_date, department_id,
                                             user_id)
VALUES ('70e85e3f-66cc-4fc1-5710-1eb18f3582d4', CURRENT_TIMESTAMP, NULL, '2025-08-31 00:00:00+00',
        '2020-09-01 00:00:00+00', '1a82f8d9-0055-496b-f1ba-b85629ec2c7e', 'f44eb495-bc11-4527-bd76-7412e5a8d83a'),
       ('81f96f40-77dd-40d2-6821-2fc2904693e5', CURRENT_TIMESTAMP, NULL, '2026-08-31 00:00:00+00',
        '2021-09-01 00:00:00+00', '2b9309ea-1166-4a7c-02cb-c9673ae03d8f', 'a55fc5a6-cd22-4638-ce87-8523f6b9e94b'),
       ('920a7051-88ee-41e3-7932-30d3a157a4f6', CURRENT_TIMESTAMP, NULL, NULL, '2022-09-01 00:00:00+00',
        '3ca41afb-2277-4b8d-13dc-da784bf14e90', 'b660d6b7-de33-4749-df98-963407ca0a5c'),
       ('a31b8162-99ff-42f4-8a43-41e4b268b507', CURRENT_TIMESTAMP, NULL, NULL, '2023-09-01 00:00:00+00',
        '1a82f8d9-0055-496b-f1ba-b85629ec2c7e', 'c771e7c8-ef44-485a-e0a9-a74518db1b6d');

-- ethesis.department_has_defenses (depends on departments, defenses)
INSERT INTO ethesis.department_has_defenses (id, created_at, last_modified_at, defense_id, department_id)
VALUES ('b42c9273-aa11-4305-9b54-52f5c379c618', CURRENT_TIMESTAMP, NULL, 'e75fc5a6-dd33-4638-ce87-8528f6ace94b',
        '1a82f8d9-0055-496b-f1ba-b85629ec2c7e'),
       ('c53da384-bb22-4416-ac65-6306d48ad729', CURRENT_TIMESTAMP, NULL, 'f860d6b7-ee44-4749-df98-963907db0a5c',
        '1a82f8d9-0055-496b-f1ba-b85629ec2c7e'),
       ('d64eb495-cc33-4527-bd76-7417e59be83a', CURRENT_TIMESTAMP, NULL, 'f860d6b7-ee44-4749-df98-963907db0a5c',
        '2b9309ea-1166-4a7c-02cb-c9673ae03d8f'),
       ('e75fc5a6-dd44-4638-ce87-8528f6ace94b', CURRENT_TIMESTAMP, NULL, '0971e7c8-ff55-485a-e0a9-a74a18dc1b6d',
        '3ca41afb-2277-4b8d-13dc-da784bf14e90'),
       ('f860d6b7-ee55-4749-df98-963907db0a5c', CURRENT_TIMESTAMP, NULL, '0971e7c8-ff55-485a-e0a9-a74a18dc1b6d',
        '1a82f8d9-0055-496b-f1ba-b85629ec2c7e');

-- ethesis.users_in_departments (depends on users, departments)
INSERT INTO ethesis.users_in_departments (id, created_at, last_modified_at, department_id, user_id)
VALUES ('0971e7c8-ff66-485a-e0a9-a74a18dc1b6d', CURRENT_TIMESTAMP, NULL, '1a82f8d9-0055-496b-f1ba-b85629ec2c7e',
        'c11b8162-898e-42f4-8a43-41e9b275a507'),
       ('1a82f8d9-0077-496b-f1ba-b85b29ed2c7e', CURRENT_TIMESTAMP, NULL, '1a82f8d9-0055-496b-f1ba-b85629ec2c7e',
        'd22c9273-9a9f-4305-9b54-52f0c386b618'),
       ('2b9309ea-1188-4a7c-02cb-c96c3ae03d8f', CURRENT_TIMESTAMP, NULL, '3ca41afb-2277-4b8d-13dc-da784bf14e90',
        'e33da384-ab00-4416-ac65-6301d497c729'),
       ('3ca41afb-2299-4b8d-13dc-da7d4bf14e90', CURRENT_TIMESTAMP, NULL, '1a82f8d9-0055-496b-f1ba-b85629ec2c7e',
        'f44eb495-bc11-4527-bd76-7412e5a8d83a'),
       ('4db52b0c-33aa-4c9e-24ed-eb8e5c025fa1', CURRENT_TIMESTAMP, NULL, '2b9309ea-1166-4a7c-02cb-c9673ae03d8f',
        'a55fc5a6-cd22-4638-ce87-8523f6b9e94b'),
       ('5ec63c1d-44bb-4daf-35fe-fc9f6d1360b2', CURRENT_TIMESTAMP, NULL, '3ca41afb-2277-4b8d-13dc-da784bf14e90',
        'b660d6b7-de33-4749-df98-963407ca0a5c'),
       ('6fd74d2e-55cc-4eb0-460f-0da07e2471c3', CURRENT_TIMESTAMP, NULL, '1a82f8d9-0055-496b-f1ba-b85629ec2c7e',
        'c771e7c8-ef44-485a-e0a9-a74518db1b6d');

COMMIT;