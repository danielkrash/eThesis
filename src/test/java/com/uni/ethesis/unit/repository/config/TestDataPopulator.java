package com.uni.ethesis.unit.repository.config;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.uni.ethesis.data.entities.Comment;
import com.uni.ethesis.data.entities.Defense;
import com.uni.ethesis.data.entities.DefenseSession;
import com.uni.ethesis.data.entities.DefenseSessionProfessor;
import com.uni.ethesis.data.entities.Department;
import com.uni.ethesis.data.entities.DepartmentAppointment;
import com.uni.ethesis.data.entities.DepartmentDefense;
import com.uni.ethesis.data.entities.Review;
import com.uni.ethesis.data.entities.Student;
import com.uni.ethesis.data.entities.Teacher;
import com.uni.ethesis.data.entities.Thesis;
import com.uni.ethesis.data.entities.ThesisProposal;
import com.uni.ethesis.data.entities.User;
import com.uni.ethesis.data.entities.UserInDepartment;
import com.uni.ethesis.enums.ReviewConclusion;
import com.uni.ethesis.enums.StudentType;
import com.uni.ethesis.enums.TeacherPosition;
import com.uni.ethesis.enums.ThesisProposalStatus;
import com.uni.ethesis.enums.ThesisStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestDataPopulator {
    private static final OffsetDateTime NOW = OffsetDateTime.now(ZoneOffset.UTC);
    private User userAlice, userBob, userCarlos, userCarol, userDave, userEva, userFrank;
    private Department cs , math , physics;
    private Student studentAlice, studentBob, studentCarlos;
    private Teacher teacherCarol, teacherDave, teacherEva;
    private ThesisProposal thesisProposal1, thesisProposal2, thesisProposal3, thesisProposal4;
    private Thesis thesis1, thesis2, thesis3;
    private Review review1, review2, review3;
    private Defense defense1, defense2, defense3;
    private DefenseSession defenseSession1, defenseSession2, defenseSession3;
    private DefenseSessionProfessor defenseSessionProfessor1, defenseSessionProfessor2, defenseSessionProfessor3,
            defenseSessionProfessor4, defenseSessionProfessor5;
    private Comment com1, com2, com3;
    private DepartmentAppointment app1, app2, app3, app4;
    private DepartmentDefense dhd1, dhd2, dhd3, dhd4 , dhd5;
    private UserInDepartment uid1, uid2, uid3, uid4, uid5, uid6 , uid7;

    public void populate(TestEntityManager entityManager) {
        createUsers(entityManager);
        createDepartments(entityManager);
        // Student and Teacher share User's ID, so create User first, then Student/Teacher
        createStudents(entityManager);
        createTeachers(entityManager);
        createThesisProposals(entityManager);
        createTheses(entityManager);
        createReviews(entityManager);
        createComments(entityManager);
        createDefenses(entityManager);
        createDefenseSessions(entityManager);
        createDefenseSessionProfessors(entityManager);
        createDepartmentAppointments(entityManager);
        createDepartmentHasDefenses(entityManager);
        createUsersInDepartments(entityManager);

        entityManager.flush();
    }

    private void createUsers(TestEntityManager em) {
        userAlice = User.builder().id(UUID.randomUUID()).createdAt(NOW).lastModifiedAt(null)
                .email("alice.student@example.com").firstName("Alice").lastName("Wonderland").build();
        userBob = User.builder().id(UUID.randomUUID()).createdAt(NOW).lastModifiedAt(NOW.minusDays(1))
                .email("bob.student@example.com").firstName("Bob").lastName("Builder").build();
        userCarlos = User.builder().id(UUID.randomUUID()).createdAt(NOW).lastModifiedAt(null)
                .email("carlos.student@example.com").firstName("Carlos").lastName("Santana").build();
        userCarol = User.builder().id(UUID.randomUUID()).createdAt(NOW).lastModifiedAt(null)
                .email("carol.teacher@example.com").firstName("Carol").lastName("Danvers").build();
        userDave = User.builder().id(UUID.randomUUID()).createdAt(NOW).lastModifiedAt(NOW.minusDays(2))
                .email("dave.teacher@example.com").firstName("David").lastName("Banner").build();
        userEva = User.builder().id(UUID.randomUUID()).createdAt(NOW).lastModifiedAt(null)
                .email("eva.teacher@example.com").firstName("Eva").lastName("Peron").build();
        userFrank = User.builder().id(UUID.randomUUID()).createdAt(NOW).lastModifiedAt(null)
                .email("frank.admin@example.com").firstName("Frank").lastName("Castle").build();

        em.persist(userAlice);
        em.persist(userBob);
        em.persist(userCarlos);
        em.persist(userCarol);
        em.persist(userDave);
        em.persist(userEva);
        em.persist(userFrank);
    }

    private void createDepartments(TestEntityManager em) {
        cs = Department.builder().createdAt(NOW).lastModifiedAt(null)
                .description("Department of Computer Science and Engineering").name("Computer Science").build();
        math = Department.builder().createdAt(NOW).lastModifiedAt(NOW.minusHours(5))
                .description("Department of Mathematics and Statistics").name("Mathematics").build();
        physics = Department.builder()
                .createdAt(NOW).lastModifiedAt(null)
                .description("Department of Physics").name("Physics").build();

        em.persist(cs);
        em.persist(math);
        em.persist(physics);
    }

    private void createStudents(TestEntityManager em) {
        studentAlice = Student.builder()
                .user(userAlice) // Link to User
                .createdAt(NOW).lastModifiedAt(null)
                .studentType(StudentType.LOCAL)
                .universityId("f103503") // Note: Does not match '^f[0-9]{6}$' regex from schema
                .build();
        studentBob = Student.builder()
                .user(userBob)
                .createdAt(NOW).lastModifiedAt(NOW.minusDays(1))
                .studentType(StudentType.ERASMUS)
                .universityId("f105502") // Note: Does not match '^f[0-9]{6}$' regex from schema
                .build();
        studentCarlos = Student.builder()
                .user(userCarlos)
                .createdAt(NOW).lastModifiedAt(null)
                .studentType(StudentType.FOREIGN)
                .universityId("f106503") // Note: Does not match '^f[0-9]{6}$' regex from schema
                .build();

        em.persist(studentAlice);
        em.persist(studentBob);
        em.persist(studentCarlos);
    }

    private void createTeachers(TestEntityManager em) {
        teacherCarol = Teacher.builder()
                .user(userCarol) // Fixed: Link to userCarol instead of userCarlos
                .createdAt(NOW).lastModifiedAt(null)
                .position(TeacherPosition.PROFESSOR)
                .build();
        teacherDave = Teacher.builder()
                .user(userDave)
                .createdAt(NOW).lastModifiedAt(NOW.minusDays(2))
                .position(TeacherPosition.ASSOCIATE_PROFESSOR)
                .build();
        teacherEva = Teacher.builder()
                .user(userEva)
                .createdAt(NOW).lastModifiedAt(null)
                .position(TeacherPosition.ASSISTANT_PROFESSOR)
                .build();

        em.persist(teacherCarol);
        em.persist(teacherDave);
        em.persist(teacherEva);
    }

    private void createThesisProposals(TestEntityManager em) {

        thesisProposal1 = ThesisProposal.builder()
                .createdAt(NOW).lastModifiedAt(null)
                .goal("Develop an AI for automated grading.").objectives("1. Research existing models. 2. Implement a prototype. 3. Test and evaluate.")
                .status(ThesisProposalStatus.APPROVED).technology("Python, TensorFlow, NLP").title("AI-Driven Automated Grading System")
                .student(studentAlice).teacher(teacherCarol).build();
        thesisProposal2 = ThesisProposal.builder()
                .createdAt(NOW).lastModifiedAt(NOW.minusDays(10))
                .goal("Explore new cryptographic algorithms.").objectives("1. Study post-quantum crypto. 2. Propose a new scheme.")
                .status(ThesisProposalStatus.APPROVED).technology("LaTeX, SageMath").title("Post-Quantum Cryptography Analysis")
                .student(studentBob).teacher(teacherDave).build();
        thesisProposal3 = ThesisProposal.builder()
                .createdAt(NOW).lastModifiedAt(NOW.minusDays(5))
                .goal("Statistical analysis of climate data.").objectives("1. Collect data. 2. Apply models. 3. Interpret results.")
                .status(ThesisProposalStatus.REJECTED).technology("R, Python").title("Climate Change Impact Study")
                .student(studentAlice).teacher(teacherDave).build();
        thesisProposal4 = ThesisProposal.builder()
                .createdAt(NOW).lastModifiedAt(null)
                .goal("Build a simple quantum circuit simulator.").objectives("1. Learn Qiskit. 2. Implement basic gates. 3. Simulate a small algorithm.")
                .status(ThesisProposalStatus.PENDING).technology("Python, Qiskit").title("Quantum Circuit Simulation")
                .student(studentCarlos).teacher(teacherEva).build();

        em.persist(thesisProposal1);
        em.persist(thesisProposal2);
        em.persist(thesisProposal3);
        em.persist(thesisProposal4);
    }

    private void createTheses(TestEntityManager em) {

        thesis1 = Thesis.builder()
                .createdAt(NOW).lastModifiedAt(NOW.minusWeeks(1))
                .finalGrade(new BigDecimal("4.5")).pdfPath("/theses/alice_ai_grading.pdf") // final_grade is numeric(2,1)
                .status(ThesisStatus.DEFENDED).proposal(thesisProposal1).build();
        thesis2 = Thesis.builder()
                .createdAt(NOW).lastModifiedAt(NOW.minusDays(3))
                .finalGrade(BigDecimal.ZERO).pdfPath("/theses/bob_crypto_analysis.pdf")
                .status(ThesisStatus.WAITING_FOR_DEFENSE).proposal(thesisProposal2).build();
        thesis3 = Thesis.builder()
                .createdAt(NOW).lastModifiedAt(null)
                .finalGrade(BigDecimal.ZERO).pdfPath("/theses/carlos_qcomp_sim.pdf")
                .status(ThesisStatus.WAITING_FOR_REVIEW).proposal(thesisProposal4).build();

        em.persist(thesis1);
        em.persist(thesis2);
        em.persist(thesis3);
    }

    private void createReviews(TestEntityManager em) {

        review1 = Review.builder().createdAt(NOW).lastModifiedAt(null)
                .conclusion(ReviewConclusion.ACCEPTED).content("The student demonstrated a strong understanding of AI principles and applied them effectively. The prototype is promising. Thesis is accepted for defense.")
                .teacher(teacherCarol).thesis(thesis1).build(); // Link to thesis1
        review2 = Review.builder().createdAt(NOW).lastModifiedAt(NOW.minusHours(1))
                .conclusion(ReviewConclusion.ACCEPTED).content("The literature review is comprehensive, but the proposed scheme requires more rigorous analysis regarding potential vulnerabilities. However, it meets the criteria for defense.")
                .teacher(teacherDave).thesis(thesis2).build(); // Link to thesis2
        review3 = Review.builder().createdAt(NOW).lastModifiedAt(null)
                .conclusion(ReviewConclusion.REJECTED).content("The initial submission lacks sufficient theoretical background and the implementation is incomplete. Significant revisions are required.")
                .teacher(teacherEva).thesis(thesis3).build(); // Link to thesis3

        em.persist(review1);
        em.persist(review2);
        em.persist(review3);
    }

    private void createComments(TestEntityManager em) {

        com1 = Comment.builder().createdAt(NOW).lastModifiedAt(null)
                .content("I agree with the assessment. The results are quite impressive for Alice's thesis.")
                .review(review1).user(userDave).build();
        com2 = Comment.builder().createdAt(NOW).lastModifiedAt(NOW.minusMinutes(30))
                .content("Thank you for the feedback on the crypto review, Professor!")
                .review(review2).user(userBob).build();
        com3 = Comment.builder().createdAt(NOW).lastModifiedAt(null)
                .content("I will work on the revisions for the quantum computing thesis as per the review.")
                .review(review3).user(userCarlos).build();
        em.persist(com1); em.persist(com2); em.persist(com3);
    }

    private void createDefenses(TestEntityManager em) {
        defense1 = Defense.builder().createdAt(NOW).lastModifiedAt(null)
                .date(Date.valueOf(LocalDate.parse("2025-09-15"))).location("Room C101, CS Building").build();
        defense2 = Defense.builder().createdAt(NOW).lastModifiedAt(NOW.minusDays(2))
                .date(Date.valueOf(LocalDate.parse("2025-09-01"))).location("Virtual Meeting Room 3").build();
        defense3 = Defense.builder().createdAt(NOW).lastModifiedAt(null)
                .date(Date.valueOf(LocalDate.parse("2025-09-10"))).location("Room P205, Physics Building").build();
        em.persist(defense1);
        em.persist(defense2);
        em.persist(defense3);
    }

    private void createDefenseSessions(TestEntityManager em) {

        // DateTimeFormatter for "YYYY-MM-DD HH:mm:ssX" style input if needed, or build OffsetDateTime directly
        // The SQL '2024-07-15 10:00:00+02' format suggests an explicit offset.
        DateTimeFormatter dtfWithOffset = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX");

        defenseSession1 = DefenseSession.builder().createdAt(NOW).lastModifiedAt(null)
                .dateAndTime(OffsetDateTime.parse("2025-09-15T10:00:00+02:00")) // ISO format is better
                .notes("Defense for Alice Wonderland thesis (AI).").defense(defense1).thesis(thesis1).build();
        defenseSession2 = DefenseSession.builder().createdAt(NOW).lastModifiedAt(NOW.minusDays(1))
                .dateAndTime(OffsetDateTime.parse("2025-09-01T14:00:00+02:00"))
                .notes("Defense for Bob Builder thesis (Crypto).").defense(defense2).thesis(thesis2).build();
        defenseSession3 = DefenseSession.builder().createdAt(NOW).lastModifiedAt(null)
                .dateAndTime(OffsetDateTime.parse("2025-09-10T09:00:00+02:00"))
                .notes("Scheduled for Carlos Santana thesis (QComp), pending review acceptance.").defense(defense3).thesis(thesis3).build();
        em.persist(defenseSession1);
        em.persist(defenseSession2);
        em.persist(defenseSession3);
    }

    private void createDefenseSessionProfessors(TestEntityManager em) {

        defenseSessionProfessor1 = DefenseSessionProfessor.builder().createdAt(NOW).lastModifiedAt(null).grade(92)
                .thoughts("Excellent presentation and Q&A handling for AI thesis. Strong grasp of the subject.")
                .defenseSession(defenseSession1).professor(teacherCarol).build();
        defenseSessionProfessor2 = DefenseSessionProfessor.builder().createdAt(NOW).lastModifiedAt(NOW.minusMinutes(5)).grade(88)
                .thoughts("Very good defense for AI thesis. Some minor points on future work could be expanded.")
                .defenseSession(defenseSession1).professor(teacherDave).build();
        defenseSessionProfessor3 = DefenseSessionProfessor.builder().createdAt(NOW).lastModifiedAt(null).grade(0)
                .thoughts("Awaiting defense.").defenseSession(defenseSession2).professor(teacherDave).build();
        defenseSessionProfessor4 = DefenseSessionProfessor.builder().createdAt(NOW).lastModifiedAt(null).grade(0)
                .thoughts("Awaiting defense, committee member.").defenseSession(defenseSession2).professor(teacherCarol).build();
        defenseSessionProfessor5 = DefenseSessionProfessor.builder().createdAt(NOW).lastModifiedAt(null).grade(0)
                .thoughts("Awaiting defense, thesis supervisor.").defenseSession(defenseSession3).professor(teacherEva).build();
        em.persist(defenseSessionProfessor1);
        em.persist(defenseSessionProfessor2);
        em.persist(defenseSessionProfessor3);
        em.persist(defenseSessionProfessor4);
        em.persist(defenseSessionProfessor5);
    }

    private void createDepartmentAppointments(TestEntityManager em) {
        // SQL 'YYYY-MM-DD HH:MM:SS+00' implies UTC.
        app1 = DepartmentAppointment.builder().createdAt(NOW).lastModifiedAt(null)
                .endDate(OffsetDateTime.parse("2025-08-31T00:00:00Z")).startDate(OffsetDateTime.parse("2020-09-01T00:00:00Z"))
                .department(cs).user(userCarlos).build(); // Reverted: userCarlos (student) has CS appointment
        app2 = DepartmentAppointment.builder().createdAt(NOW).lastModifiedAt(null)
                .endDate(OffsetDateTime.parse("2026-08-31T00:00:00Z")).startDate(OffsetDateTime.parse("2021-09-01T00:00:00Z"))
                .department(math).user(userDave).build();
        app3 = DepartmentAppointment.builder().createdAt(NOW).lastModifiedAt(null)
                .endDate(null).startDate(OffsetDateTime.parse("2022-09-01T00:00:00Z"))
                .department(physics).user(userEva).build();
        app4 = DepartmentAppointment.builder().createdAt(NOW).lastModifiedAt(null)
                .endDate(null).startDate(OffsetDateTime.parse("2023-09-01T00:00:00Z"))
                .department(cs).user(userFrank).build();
        em.persist(app1); em.persist(app2); em.persist(app3); em.persist(app4);
    }

    private void createDepartmentHasDefenses(TestEntityManager em) {

        dhd1 = DepartmentDefense.builder().createdAt(NOW).lastModifiedAt(null).defense(defense1).department(cs).build();
        dhd2 = DepartmentDefense.builder().createdAt(NOW).lastModifiedAt(null).defense(defense2).department(cs).build();
        dhd3 = DepartmentDefense.builder().createdAt(NOW).lastModifiedAt(null).defense(defense2).department(math).build();
        dhd4 = DepartmentDefense.builder().createdAt(NOW).lastModifiedAt(null).defense(defense3).department(physics).build();
        dhd5 = DepartmentDefense.builder().createdAt(NOW).lastModifiedAt(null).defense(defense3).department(cs).build();
        em.persist(dhd1); em.persist(dhd2); em.persist(dhd3); em.persist(dhd4); em.persist(dhd5);
    }

    private void createUsersInDepartments(TestEntityManager em) {
        uid1 = UserInDepartment.builder().createdAt(NOW).lastModifiedAt(null).department(cs).user(userAlice).build();
        uid2 = UserInDepartment.builder().createdAt(NOW).lastModifiedAt(null).department(cs).user(userBob).build();
        uid3 = UserInDepartment.builder().createdAt(NOW).lastModifiedAt(null).department(physics).user(userCarlos).build();
        uid4 = UserInDepartment.builder().createdAt(NOW).lastModifiedAt(null).department(cs).user(userCarol).build();
        uid5 = UserInDepartment.builder().createdAt(NOW).lastModifiedAt(null).department(math).user(userDave).build();
        uid6 = UserInDepartment.builder().createdAt(NOW).lastModifiedAt(null).department(physics).user(userDave).build();
        uid7 = UserInDepartment.builder().createdAt(NOW).lastModifiedAt(null).department(cs).user(userFrank).build();
        em.persist(uid1); em.persist(uid2); em.persist(uid3); em.persist(uid4);
        em.persist(uid5); em.persist(uid6); em.persist(uid7);
    }
}














