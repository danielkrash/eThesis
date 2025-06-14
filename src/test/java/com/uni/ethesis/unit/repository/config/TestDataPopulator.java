package com.uni.ethesis.unit.repository.config;

import com.uni.ethesis.data.entities.*;
import com.uni.ethesis.enums.*;
import jakarta.persistence.EntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


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

    public User getUserAlice() {
        return userAlice;
    }

    public void setUserAlice(User userAlice) {
        this.userAlice = userAlice;
    }

    public User getUserBob() {
        return userBob;
    }

    public void setUserBob(User userBob) {
        this.userBob = userBob;
    }

    public User getUserCarlos() {
        return userCarlos;
    }

    public void setUserCarlos(User userCarlos) {
        this.userCarlos = userCarlos;
    }

    public User getUserCarol() {
        return userCarol;
    }

    public void setUserCarol(User userCarol) {
        this.userCarol = userCarol;
    }

    public User getUserDave() {
        return userDave;
    }

    public void setUserDave(User userDave) {
        this.userDave = userDave;
    }

    public User getUserEva() {
        return userEva;
    }

    public void setUserEva(User userEva) {
        this.userEva = userEva;
    }

    public User getUserFrank() {
        return userFrank;
    }

    public void setUserFrank(User userFrank) {
        this.userFrank = userFrank;
    }

    public Department getCs() {
        return cs;
    }

    public void setCs(Department cs) {
        this.cs = cs;
    }

    public Department getMath() {
        return math;
    }

    public void setMath(Department math) {
        this.math = math;
    }

    public Department getPhysics() {
        return physics;
    }

    public void setPhysics(Department physics) {
        this.physics = physics;
    }

    public Student getStudentAlice() {
        return studentAlice;
    }

    public void setStudentAlice(Student studentAlice) {
        this.studentAlice = studentAlice;
    }

    public Student getStudentBob() {
        return studentBob;
    }

    public void setStudentBob(Student studentBob) {
        this.studentBob = studentBob;
    }

    public Student getStudentCarlos() {
        return studentCarlos;
    }

    public void setStudentCarlos(Student studentCarlos) {
        this.studentCarlos = studentCarlos;
    }

    public Teacher getTeacherCarol() {
        return teacherCarol;
    }

    public void setTeacherCarol(Teacher teacherCarol) {
        this.teacherCarol = teacherCarol;
    }

    public Teacher getTeacherDave() {
        return teacherDave;
    }

    public void setTeacherDave(Teacher teacherDave) {
        this.teacherDave = teacherDave;
    }

    public Teacher getTeacherEva() {
        return teacherEva;
    }

    public void setTeacherEva(Teacher teacherEva) {
        this.teacherEva = teacherEva;
    }

    public ThesisProposal getThesisProposal1() {
        return thesisProposal1;
    }

    public void setThesisProposal1(ThesisProposal thesisProposal1) {
        this.thesisProposal1 = thesisProposal1;
    }

    public ThesisProposal getThesisProposal2() {
        return thesisProposal2;
    }

    public void setThesisProposal2(ThesisProposal thesisProposal2) {
        this.thesisProposal2 = thesisProposal2;
    }

    public ThesisProposal getThesisProposal3() {
        return thesisProposal3;
    }

    public void setThesisProposal3(ThesisProposal thesisProposal3) {
        this.thesisProposal3 = thesisProposal3;
    }

    public ThesisProposal getThesisProposal4() {
        return thesisProposal4;
    }

    public void setThesisProposal4(ThesisProposal thesisProposal4) {
        this.thesisProposal4 = thesisProposal4;
    }

    public Thesis getThesis1() {
        return thesis1;
    }

    public void setThesis1(Thesis thesis1) {
        this.thesis1 = thesis1;
    }

    public Thesis getThesis2() {
        return thesis2;
    }

    public void setThesis2(Thesis thesis2) {
        this.thesis2 = thesis2;
    }

    public Thesis getThesis3() {
        return thesis3;
    }

    public void setThesis3(Thesis thesis3) {
        this.thesis3 = thesis3;
    }

    public Review getReview1() {
        return review1;
    }

    public void setReview1(Review review1) {
        this.review1 = review1;
    }

    public Review getReview2() {
        return review2;
    }

    public void setReview2(Review review2) {
        this.review2 = review2;
    }

    public Review getReview3() {
        return review3;
    }

    public void setReview3(Review review3) {
        this.review3 = review3;
    }

    public Defense getDefense1() {
        return defense1;
    }

    public void setDefense1(Defense defense1) {
        this.defense1 = defense1;
    }

    public Defense getDefense2() {
        return defense2;
    }

    public void setDefense2(Defense defense2) {
        this.defense2 = defense2;
    }

    public Defense getDefense3() {
        return defense3;
    }

    public void setDefense3(Defense defense3) {
        this.defense3 = defense3;
    }

    public DefenseSession getDefenseSession1() {
        return defenseSession1;
    }

    public void setDefenseSession1(DefenseSession defenseSession1) {
        this.defenseSession1 = defenseSession1;
    }

    public DefenseSession getDefenseSession2() {
        return defenseSession2;
    }

    public void setDefenseSession2(DefenseSession defenseSession2) {
        this.defenseSession2 = defenseSession2;
    }

    public DefenseSession getDefenseSession3() {
        return defenseSession3;
    }

    public void setDefenseSession3(DefenseSession defenseSession3) {
        this.defenseSession3 = defenseSession3;
    }

    public DefenseSessionProfessor getDefenseSessionProfessor1() {
        return defenseSessionProfessor1;
    }

    public void setDefenseSessionProfessor1(DefenseSessionProfessor defenseSessionProfessor1) {
        this.defenseSessionProfessor1 = defenseSessionProfessor1;
    }

    public DefenseSessionProfessor getDefenseSessionProfessor2() {
        return defenseSessionProfessor2;
    }

    public void setDefenseSessionProfessor2(DefenseSessionProfessor defenseSessionProfessor2) {
        this.defenseSessionProfessor2 = defenseSessionProfessor2;
    }

    public DefenseSessionProfessor getDefenseSessionProfessor3() {
        return defenseSessionProfessor3;
    }

    public void setDefenseSessionProfessor3(DefenseSessionProfessor defenseSessionProfessor3) {
        this.defenseSessionProfessor3 = defenseSessionProfessor3;
    }

    public DefenseSessionProfessor getDefenseSessionProfessor4() {
        return defenseSessionProfessor4;
    }

    public void setDefenseSessionProfessor4(DefenseSessionProfessor defenseSessionProfessor4) {
        this.defenseSessionProfessor4 = defenseSessionProfessor4;
    }

    public DefenseSessionProfessor getDefenseSessionProfessor5() {
        return defenseSessionProfessor5;
    }

    public void setDefenseSessionProfessor5(DefenseSessionProfessor defenseSessionProfessor5) {
        this.defenseSessionProfessor5 = defenseSessionProfessor5;
    }

    public Comment getCom1() {
        return com1;
    }

    public void setCom1(Comment com1) {
        this.com1 = com1;
    }

    public Comment getCom2() {
        return com2;
    }

    public void setCom2(Comment com2) {
        this.com2 = com2;
    }

    public Comment getCom3() {
        return com3;
    }

    public void setCom3(Comment com3) {
        this.com3 = com3;
    }

    public DepartmentAppointment getApp1() {
        return app1;
    }

    public void setApp1(DepartmentAppointment app1) {
        this.app1 = app1;
    }

    public DepartmentAppointment getApp2() {
        return app2;
    }

    public void setApp2(DepartmentAppointment app2) {
        this.app2 = app2;
    }

    public DepartmentAppointment getApp3() {
        return app3;
    }

    public void setApp3(DepartmentAppointment app3) {
        this.app3 = app3;
    }

    public DepartmentAppointment getApp4() {
        return app4;
    }

    public void setApp4(DepartmentAppointment app4) {
        this.app4 = app4;
    }

    public DepartmentDefense getDhd1() {
        return dhd1;
    }

    public void setDhd1(DepartmentDefense dhd1) {
        this.dhd1 = dhd1;
    }

    public DepartmentDefense getDhd2() {
        return dhd2;
    }

    public void setDhd2(DepartmentDefense dhd2) {
        this.dhd2 = dhd2;
    }

    public DepartmentDefense getDhd3() {
        return dhd3;
    }

    public void setDhd3(DepartmentDefense dhd3) {
        this.dhd3 = dhd3;
    }

    public DepartmentDefense getDhd4() {
        return dhd4;
    }

    public void setDhd4(DepartmentDefense dhd4) {
        this.dhd4 = dhd4;
    }

    public DepartmentDefense getDhd5() {
        return dhd5;
    }

    public void setDhd5(DepartmentDefense dhd5) {
        this.dhd5 = dhd5;
    }

    public UserInDepartment getUid1() {
        return uid1;
    }

    public void setUid1(UserInDepartment uid1) {
        this.uid1 = uid1;
    }

    public UserInDepartment getUid2() {
        return uid2;
    }

    public void setUid2(UserInDepartment uid2) {
        this.uid2 = uid2;
    }

    public UserInDepartment getUid3() {
        return uid3;
    }

    public void setUid3(UserInDepartment uid3) {
        this.uid3 = uid3;
    }

    public UserInDepartment getUid4() {
        return uid4;
    }

    public void setUid4(UserInDepartment uid4) {
        this.uid4 = uid4;
    }

    public UserInDepartment getUid5() {
        return uid5;
    }

    public void setUid5(UserInDepartment uid5) {
        this.uid5 = uid5;
    }

    public UserInDepartment getUid6() {
        return uid6;
    }

    public void setUid6(UserInDepartment uid6) {
        this.uid6 = uid6;
    }

    public UserInDepartment getUid7() {
        return uid7;
    }

    public void setUid7(UserInDepartment uid7) {
        this.uid7 = uid7;
    }

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
        userAlice = User.builder().createdAt(NOW).lastModifiedAt(null)
                .email("alice.student@example.com").firstName("Alice").lastName("Wonderland").build();
        userBob = User.builder().createdAt(NOW).lastModifiedAt(NOW.minusDays(1))
                .email("bob.student@example.com").firstName("Bob").lastName("Builder").build();
        userCarlos = User.builder().createdAt(NOW).lastModifiedAt(null)
                .email("carlos.student@example.com").firstName("Carlos").lastName("Santana").build();
        userCarol = User.builder().createdAt(NOW).lastModifiedAt(null)
                .email("carol.teacher@example.com").firstName("Carol").lastName("Danvers").build();
        userDave = User.builder().createdAt(NOW).lastModifiedAt(NOW.minusDays(2))
                .email("dave.teacher@example.com").firstName("David").lastName("Banner").build();
        userEva = User.builder().createdAt(NOW).lastModifiedAt(null)
                .email("eva.teacher@example.com").firstName("Eva").lastName("Peron").build();
        userFrank = User.builder().createdAt(NOW).lastModifiedAt(null)
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
                .user(userCarlos) // Link to User
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
                .teacher(teacherCarol).build();
        review2 = Review.builder().createdAt(NOW).lastModifiedAt(NOW.minusHours(1))
                .conclusion(ReviewConclusion.ACCEPTED).content("The literature review is comprehensive, but the proposed scheme requires more rigorous analysis regarding potential vulnerabilities. However, it meets the criteria for defense.")
                .teacher(teacherDave).build();
        review3 = Review.builder().createdAt(NOW).lastModifiedAt(null)
                .conclusion(ReviewConclusion.REJECTED).content("The initial submission lacks sufficient theoretical background and the implementation is incomplete. Significant revisions are required.")
                .teacher(teacherEva).build();

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
                .department(cs).user(userCarlos).build();
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














