package org.neo4j.ogm.mapper;

public class ObjectToCypherMapperTest {

//    private ObjectToCypherMapper mapper;
//
//    @Before
//    public void setUpMapper() {
//        FieldDictionary fieldDictionary = new SimpleFieldDictionary(new DomainInfo("org.neo4j.ogm.mapper.domain.education"));
//        this.mapper = new ObjectGraphMapper(Object.class, null, new FieldEntityAccessFactory(fieldDictionary));
//    }
//
//    @Test(expected = NullPointerException.class)
//    public void shouldThrowExceptionOnAttemptToMapNullObjectToCypherQuery() {
//        this.mapper.mapToCypher(null);
//    }
//
//    @Test
//    public void shouldProduceCypherForCreatingNewSimpleObject() {
//        Student newStudent = new Student();
//        newStudent.setName("Gary");
//
//        assertNull(newStudent.getId());
//
//        List<String> cypher = this.mapper.mapToCypher(newStudent);
//        assertNotNull("The resultant cypher shouldn't be null", cypher);
//        assertFalse("The resultant list of cypher statements shouldn't be empty", cypher.isEmpty());
//        System.out.println(cypher);
//    }
//
//    @Test
//    public void shouldProduceCypherForUpdatingExistingSimpleObject() {
//        Student newStudent = new Student();
//        newStudent.setId(339L);
//        newStudent.setName("Sheila");
//
//        List<String> cypher = this.mapper.mapToCypher(newStudent);
//        assertNotNull("The resultant cypher shouldn't be null", cypher);
//        assertFalse("The resultant list of cypher statements shouldn't be empty", cypher.isEmpty());
//        System.out.println(cypher);
//    }
//
//    @Test
//    public void shouldProduceCypherForSmallGraphOfPersistentAndTransientObjects() {
//        Student transientStudent = new Student();
//        transientStudent.setName("Lakshmipathy");
//        Student persistentStudent = new Student();
//        persistentStudent.setId(103L);
//        persistentStudent.setName("Giuseppe");
//        Course existingCourse = new Course();
//        existingCourse.setId(49L);
//        existingCourse.setName("BSc Computer Science");
//        existingCourse.setStudents(Arrays.asList(transientStudent, persistentStudent));
//
//        List<String> cypher = this.mapper.mapToCypher(existingCourse);
//        assertNotNull("The resultant cypher shouldn't be null", cypher);
//        assertFalse("The resultant list of cypher statements shouldn't be empty", cypher.isEmpty());
//        System.out.println(cypher);
//    }

}
