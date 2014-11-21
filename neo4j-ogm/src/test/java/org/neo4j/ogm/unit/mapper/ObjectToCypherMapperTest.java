package org.neo4j.ogm.unit.mapper;

import org.junit.*;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.domain.education.Course;
import org.neo4j.ogm.domain.education.School;
import org.neo4j.ogm.domain.education.Student;
import org.neo4j.ogm.domain.education.Teacher;
import org.neo4j.ogm.mapper.MetaDataDrivenObjectToCypherMapper;
import org.neo4j.ogm.mapper.ObjectToCypherMapper;
import org.neo4j.ogm.mapper.cypher.ParameterisedStatement;
import org.neo4j.ogm.mapper.cypher.ParameterisedStatements;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.session.MappedRelationship;
import org.neo4j.ogm.session.MappedRelationshipCache;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.*;

import static com.graphaware.test.unit.GraphUnit.assertSameGraph;
import static org.junit.Assert.*;

public class ObjectToCypherMapperTest {

    private ObjectToCypherMapper mapper;

    private static GraphDatabaseService graphDatabase;
    private static ExecutionEngine executionEngine;
    private static MetaData mappingMetadata;

    /** Simulates the loaded relationships that are managed by the session */
    protected static List<MappedRelationship> mockLoadedRelationships = new ArrayList<>();

    @BeforeClass
    public static void setUpTestDatabase() {
        graphDatabase = new TestGraphDatabaseFactory().newImpermanentDatabase();
        executionEngine = new ExecutionEngine(graphDatabase);
        mappingMetadata = new MetaData("org.neo4j.ogm.domain.education");
    }

    @AfterClass
    public static void shutDownDatabase() {
        graphDatabase.shutdown();
    }

    @Before
    public void setUpMapper() {
        this.mapper = new MetaDataDrivenObjectToCypherMapper(mappingMetadata, new MappedRelationshipCache() {
            @Override
            public Iterator<MappedRelationship> iterator() {
                return ObjectToCypherMapperTest.mockLoadedRelationships.iterator();
            }
        });
    }

    @After
    public void cleanGraph() {
        executionEngine.execute("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE r, n");
        mockLoadedRelationships.clear();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionOnAttemptToMapNullObjectToCypherQuery() {
        this.mapper.mapToCypher(null);
    }

    @Test
    public void shouldProduceCypherForCreatingNewSimpleObject() {

        Student newStudent = new Student("Gary");
        ParameterisedStatements cypher = new ParameterisedStatements(this.mapper.mapToCypher(newStudent).getStatements());

        assertNull(newStudent.getId());
        assertEquals("CREATE (_0:`Student`:`DomainObject`{_0_props}) RETURN id(_0) AS _0", cypher.getStatements().get(0).getStatement());

        executeStatementsAndAssertSameGraph(cypher, "CREATE (:Student:DomainObject {name:\"Gary\"})");
    }

    @Test
    public void shouldProduceCypherForUpdatingExistingSimpleObject() {
        ExecutionResult executionResult = executionEngine.execute("CREATE (s:Student {name:'Sheila Smythe'}) RETURN id(s) AS id");
        Long existingNodeId = Long.valueOf(executionResult.iterator().next().get("id").toString());

        Student newStudent = new Student();
        newStudent.setId(existingNodeId);
        newStudent.setName("Sheila Smythe-Jones");

        ParameterisedStatements cypher = new ParameterisedStatements(this.mapper.mapToCypher(newStudent).getStatements());
        executeStatementsAndAssertSameGraph(cypher, "CREATE (:Student:DomainObject {name:'Sheila Smythe-Jones'})");
    }

    @Test
    public void shouldProduceCypherToAddNewNodeIntoSmallExistingGraph() {
        // set up one student on a course to begin with and add a new student to it
        ExecutionResult executionResult = executionEngine.execute(
                "CREATE (c:Course {name:'BSc Computer Science'})-[:STUDENTS]->(s:Student:DomainObject {name:'Gianfranco'}) " +
                        "RETURN id(s) AS student_id, id(c) AS course_id");
        Map<String, Object> resultSetRow = executionResult.iterator().next();
        Long studentId = Long.valueOf(resultSetRow.get("student_id").toString());
        Long courseId = Long.valueOf(resultSetRow.get("course_id").toString());

        Student persistentStudent = new Student();
        persistentStudent.setId(studentId);
        persistentStudent.setName("Gianfranco");
        Student transientStudent = new Student();
        transientStudent.setName("Lakshmipathy");
        Course existingCourse = new Course();
        existingCourse.setId(courseId);
        existingCourse.setName("BSc Computer Science");
        existingCourse.setStudents(Arrays.asList(transientStudent, persistentStudent));

        // XXX: NB: currently using a dodgy relationship type because of simple strategy read/write inconsistency
        ParameterisedStatements cypher = new ParameterisedStatements(this.mapper.mapToCypher(existingCourse).getStatements());

        executeStatementsAndAssertSameGraph(cypher, "CREATE (c:Course {name:'BSc Computer Science'}), " +
                "(x:Student:DomainObject {name:'Gianfranco'}), (y:Student:DomainObject {name:'Lakshmipathy'}) " +
                "WITH c, x, y MERGE (c)-[:STUDENTS]->(x) MERGE (c)-[:STUDENTS]->(y)");
    }

    @Test
    public void shouldNotGetIntoAnInfiniteLoopWhenSavingObjectsThatReferenceEachOther() {
        Teacher missJones = new Teacher();
        missJones.setName("Miss Jones");
        Teacher mrWhite = new Teacher();
        mrWhite.setName("Mr White");
        School school = new School("Hilly Fields");
        school.setTeachers(Arrays.asList(missJones, mrWhite));

        ParameterisedStatements cypher = new ParameterisedStatements(this.mapper.mapToCypher(school).getStatements());
        executeStatementsAndAssertSameGraph(cypher, "CREATE (j:Teacher {name:'Miss Jones'}), (w:Teacher {name:'Mr White'})," +
                " (s:School:DomainObject {name:'Hilly Fields'}), (s)-[:TEACHERS]->(j), (s)-[:TEACHERS]->(w)");
    }

    @Test
    public void shouldCorrectlyPersistObjectGraphsSeveralLevelsDeep() {
        Student sheila = new Student();
        sheila.setName("Sheila Smythe");
        Student gary = new Student();
        gary.setName("Gary Jones");
        Student winston = new Student();
        winston.setName("Winston Charles");

        Course physics = new Course();
        physics.setName("GCSE Physics");
        physics.setStudents(Arrays.asList(gary, sheila));
        Course maths = new Course();
        maths.setName("A-Level Mathematics");
        maths.setStudents(Arrays.asList(sheila, winston));

        Teacher teacher = new Teacher();
        teacher.setName("Mrs Kapoor");
        teacher.setCourses(Arrays.asList(physics, maths));

        ParameterisedStatements cypher = new ParameterisedStatements(this.mapper.mapToCypher(teacher).getStatements());
        executeStatementsAndAssertSameGraph(cypher, "CREATE (t:Teacher {name:'Mrs Kapoor'}), "
                + "(p:Course {name:'GCSE Physics'}), (m:Course {name:'A-Level Mathematics'}), "
                + "(s:Student:DomainObject {name:'Sheila Smythe'}), "
                + "(g:Student:DomainObject {name:'Gary Jones'}), "
                + "(w:Student:DomainObject {name:'Winston Charles'}), "
                + "(t)-[:COURSES]->(p)-[:STUDENTS]->(s), (t)-[:COURSES]->(m)-[:STUDENTS]->(s), "
                + "(p)-[:STUDENTS]->(g), (m)-[:STUDENTS]->(w)");
    }

    @Test
    public void shouldNotOverwriteExistingObjectPropertiesAndLabelsThatHaveNotBeenLoadedOnUpdate() {
        ExecutionResult result = executionEngine.execute("CREATE (n:Student:DomainObject:Person {student_id:'mr714'}) RETURN id(n) AS id");
        Long id = Long.valueOf(result.iterator().next().get("id").toString());

        Student student = new Student();
        student.setId(id);
        student.setName("Melanie");

        ParameterisedStatements cypherStatements = new ParameterisedStatements(this.mapper.mapToCypher(student).getStatements());
        executeStatementsAndAssertSameGraph(cypherStatements,
                "CREATE (:Student:DomainObject:Person {student_id:'mr714',name:'Melanie'})");
    }

    @Test
    public void shouldCorrectlyUpdateRelationshipsInGraphForItemAddedToExistingCollection() {
        ExecutionResult result = executionEngine.execute(
                "CREATE (t:Teacher {name:'Mr Gilbert'})-[:COURSES]->(c:Course {name:'A-Level German'}) " +
                "RETURN id(t) AS teacher_id, id(c) AS course_id");
        Map<String, ?> resultRow = result.iterator().next();
        Long teacherId = (Long) resultRow.get("teacher_id");
        Long courseId = (Long) resultRow.get("course_id");

        Teacher teacher = new Teacher();
        teacher.setId(teacherId);
        teacher.setName("Mr Gilbert");
        Course german = new Course();
        german.setId(courseId);
        german.setName("A-Level German");
        Course spanish = new Course();
        spanish.setName("A-Level Spanish");
        teacher.setCourses(Arrays.asList(german, spanish));

        ParameterisedStatements cypher = new ParameterisedStatements(this.mapper.mapToCypher(teacher).getStatements());
        executeStatementsAndAssertSameGraph(cypher, "CREATE (t:Teacher {name:'Mr Gilbert'}), "
                + "(g:Course {name:'A-Level German'}), (s:Course {name:'A-Level Spanish'}),"
                + "(t)-[:COURSES]->(g), (t)-[:COURSES]->(s)");
    }

    @Test
    public void shouldCorrectlyRemoveRelationshipWhenItemIsRemovedFromCollection() {
        // simple one course with three students
        ExecutionResult executionResult = executionEngine.execute("CREATE (c:Course {name:'GCSE Music'}), "
                + "(c)-[:STUDENTS]->(x:Student:DomainObject {name:'Xavier'}), "
                + "(c)-[:STUDENTS]->(y:Student:DomainObject {name:'Yvonne'}), "
                + "(c)-[:STUDENTS]->(z:Student:DomainObject {name:'Zack'}) "
                + "RETURN id(c) AS course_id, id(x) AS xid, id(y) AS yid, id(z) AS zid");
        Map<String, ?> next = executionResult.iterator().next();
        Long courseId = (Long) next.get("course_id");
        Long studentId = (Long) next.get("yid");

        mockLoadedRelationships.add(new MappedRelationship(courseId, "STUDENTS", (Long) next.get("xid")));
        mockLoadedRelationships.add(new MappedRelationship(courseId, "STUDENTS", studentId));
        mockLoadedRelationships.add(new MappedRelationship(courseId, "STUDENTS", (Long) next.get("zid")));

        Course course = new Course();
        course.setId(courseId);
        course.setName("GCSE Music");
        Student student = new Student();
        student.setId(studentId);
        student.setName("Yvonne");
        course.setStudents(Arrays.asList(student));

        // expect two of the students to have been removed from the course
        ParameterisedStatements cypher = new ParameterisedStatements(this.mapper.mapToCypher(course).getStatements());
        executeStatementsAndAssertSameGraph(cypher, "CREATE (:Student:DomainObject {name:'Xavier'}), "
                + "(:Student:DomainObject {name:'Zack'}), "
                + "(:Course {name:'GCSE Music'})-[:STUDENTS]->(:Student:DomainObject {name:'Yvonne'})");
    }

    @Test
    public void shouldCorrectlyRemoveRelationshipWhenItemIsMovedToDifferentCollection() {
        // start with one teacher teachers two courses, each with one student in
        ExecutionResult executionResult = executionEngine.execute("CREATE (t:Teacher {name:'Ms Thompson'}), " +
                "(bs:Course {name:'GNVQ Business Studies'})-[:STUDENTS]->(s:Student:DomainObject {name:'Shivani'}), " +
                "(dt:Course {name:'GCSE Design & Technology'})-[:STUDENTS]->(j:Student:DomainObject {name:'Jeff'}), " +
                "(t)-[:COURSES]->(bs), (t)-[:COURSES]->(dt) " +
                "RETURN id(t) AS teacher_id, id(bs) AS bs_id, id(dt) AS dt_id, id(s) AS s_id");
        Map<String, ?> next = executionResult.iterator().next();
        Long teacherId = (Long) next.get("teacher_id");
        Long businessStudiesCourseId = (Long) next.get("bs_id");
        Long designTechnologyCourseId = (Long) next.get("dt_id");
        Long studentId = (Long) next.get("s_id");

        // NB: this simulates the graph not being fully hydrated, so Jeff's enrolment on GCSE D+T should remain untouched
        mockLoadedRelationships.add(new MappedRelationship(teacherId, "COURSES", businessStudiesCourseId));
        mockLoadedRelationships.add(new MappedRelationship(teacherId, "COURSES", designTechnologyCourseId));
        mockLoadedRelationships.add(new MappedRelationship(businessStudiesCourseId, "STUDENTS", studentId));

        Course designTech = new Course();
        designTech.setId(designTechnologyCourseId);
        designTech.setName("GCSE Design & Technology");
        Course businessStudies = new Course();
        businessStudies.setId(businessStudiesCourseId);
        businessStudies.setName("GNVQ Business Studies");
        Teacher teacher = new Teacher();
        teacher.setId(teacherId);
        teacher.setName("Ms Thompson");
        teacher.setCourses(Arrays.asList(businessStudies, designTech));

        // move student from one course to the other
        Student student = new Student();
        student.setId(studentId);
        student.setName("Shivani");
        businessStudies.setStudents(Collections.<Student>emptyList());
        designTech.setStudents(Arrays.asList(student));

        ParameterisedStatements cypher = new ParameterisedStatements(this.mapper.mapToCypher(teacher).getStatements());
        executeStatementsAndAssertSameGraph(cypher, "CREATE (t:Teacher {name:'Ms Thompson'}), " +
                "(bs:Course {name:'GNVQ Business Studies'}), (dt:Course {name:'GCSE Design & Technology'}), " +
                "(dt)-[:STUDENTS]->(j:Student:DomainObject {name:'Jeff'}), " +
                "(dt)-[:STUDENTS]->(s:Student:DomainObject {name:'Shivani'}), " +
                "(t)-[:COURSES]->(bs), (t)-[:COURSES]->(dt)");
    }

    @Test
    public void shouldCorrectlyRemoveRelationshipWhenItemIsDisconnectedFromNonOwningSide() {
        ExecutionResult executionResult = executionEngine.execute("CREATE (s:School:DomainObject), "
                + "(s)-[:TEACHERS]->(j:Teacher {name:'Miss Jones'}), "
                + "(s)-[:TEACHERS]->(w:Teacher {name:'Mr White'}) "
                + "RETURN id(s) AS school_id, id(j) AS jones_id, id(w) AS white_id");
        Map<String, ?> next = executionResult.iterator().next();
        Long schoolId = (Long) next.get("school_id");
        Long whiteId = (Long) next.get("white_id");

        mockLoadedRelationships.add(new MappedRelationship(schoolId, "TEACHERS", whiteId));

        Teacher mrWhiteWithNullSchool = new Teacher();
        mrWhiteWithNullSchool.setId(whiteId);
        mrWhiteWithNullSchool.setName("Mr White");

        ParameterisedStatements cypher = new ParameterisedStatements(this.mapper.mapToCypher(mrWhiteWithNullSchool).getStatements());
        executeStatementsAndAssertSameGraph(cypher, "CREATE (w:Teacher {name:'Mr White'}), "
                + "(s:School:DomainObject)-[:TEACHERS]->(:Teacher {name:'Miss Jones'})");
    }


    @Test
    public void testVariablePersistenceToDepthZero() {

        Teacher claraOswald = new Teacher();
        Teacher dannyPink = new Teacher();
        School coalHillSchool = new School("Coal Hill");

        coalHillSchool.setTeachers(Arrays.asList(claraOswald, dannyPink));

        ParameterisedStatements cypher = new ParameterisedStatements(this.mapper.mapToCypher(coalHillSchool, 0).getStatements());

        // we don't expect the teachers to be persisted when persisting the school to depth 0
        executeStatementsAndAssertSameGraph(cypher, "CREATE (s:School:DomainObject {name:'Coal Hill'}) RETURN s");

    }


    @Test
    public void testVariablePersistenceToDepthOne() {

        School coalHillSchool = new School("Coal Hill");

        Teacher claraOswald = new Teacher("Clara Oswald");
        Teacher dannyPink = new Teacher("Danny Pink");

        Course english = new Course("English");
        Course maths = new Course("Maths");

        // do we need to set both sides?
        coalHillSchool.setTeachers(Arrays.asList(claraOswald, dannyPink));

        // do we need to set both sides?
        claraOswald.setCourses(Arrays.asList(english));
        dannyPink.setCourses(Arrays.asList(maths));

        ParameterisedStatements cypher = new ParameterisedStatements(this.mapper.mapToCypher(coalHillSchool, 1).getStatements());

        // we ONLY expect the school and its teachers to be persisted when persisting the school to depth 1
        executeStatementsAndAssertSameGraph(cypher, "CREATE " +
                "(s:School:DomainObject {name:'Coal Hill'}), " +
                "(c:Teacher {name:'Clara Oswald'}), " +
                "(d:Teacher {name:'Danny Pink'}), " +
                "(s)-[:TEACHERS]->(c), " +
                "(s)-[:TEACHERS]->(d)");

    }

    @Test
    public void testVariablePersistenceToDepthTwo() {

        School coalHillSchool = new School("Coal Hill");

        Teacher claraOswald = new Teacher("Clara Oswald");
        Teacher dannyPink = new Teacher("Danny Pink");

        Course english = new Course("English");
        Course maths = new Course("Maths");

        // do we need to set both sides?
        coalHillSchool.setTeachers(Arrays.asList(claraOswald, dannyPink));

        // do we need to set both sides?
        claraOswald.setCourses(Arrays.asList(english));
        dannyPink.setCourses(Arrays.asList(maths));

        ParameterisedStatements cypher = new ParameterisedStatements(this.mapper.mapToCypher(coalHillSchool, 2).getStatements());

        assertEquals("CREATE (_0:`School`:`DomainObject`{_0_props}) WITH _0 CREATE (_1:`Teacher`{_1_props}) WITH _0,_1 CREATE (_2:`Course`{_2_props}) WITH _0,_1,_2 CREATE (_3:`Teacher`{_3_props}) WITH _0,_1,_2,_3 CREATE (_4:`Course`{_4_props}) WITH _0,_1,_2,_3,_4 MERGE (_1)-[:COURSES]->(_2) MERGE (_0)-[:TEACHERS]->(_1) MERGE (_3)-[:COURSES]->(_4) MERGE (_0)-[:TEACHERS]->(_3) RETURN id(_0) AS _0, id(_1) AS _1, id(_2) AS _2, id(_3) AS _3, id(_4) AS _4", cypher.getStatements().get(0).getStatement());

        // we expect the school its teachers and the teachers courses to be persisted when persisting the school to depth 2
        executeStatementsAndAssertSameGraph(cypher, "CREATE " +
                "(school:School:DomainObject {name:'Coal Hill'}), " +
                "(clara:Teacher {name:'Clara Oswald'}), " +
                "(danny:Teacher {name:'Danny Pink'}), " +
                "(english:Course {name:'English'}), " +
                "(maths:Course {name:'Maths'}), " +
                "(school)-[:TEACHERS]->(clara), " +
                "(school)-[:TEACHERS]->(danny), " +
                "(danny)-[:COURSES]->(maths), " +
                "(clara)-[:COURSES]->(english)");


    }

    private void executeStatementsAndAssertSameGraph(ParameterisedStatements cypher, String sameGraphCypher) {
        assertNotNull("The resultant cypher statements shouldn't be null", cypher.getStatements());
        assertFalse("The resultant cypher statements shouldn't be empty", cypher.getStatements().isEmpty());

        for (ParameterisedStatement query : cypher.getStatements()) {
            executionEngine.execute(query.getStatement(), query.getParameters());
        }
        assertSameGraph(graphDatabase, sameGraphCypher);
    }

}
