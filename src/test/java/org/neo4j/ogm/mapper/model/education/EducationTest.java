package org.neo4j.ogm.mapper.model.education;

import org.junit.Test;
import org.neo4j.ogm.CypherQueryProxy;
import org.neo4j.ogm.mapper.cypher.CypherQuery;
import org.neo4j.ogm.mapper.domain.education.Course;
import org.neo4j.ogm.mapper.domain.education.Student;
import org.neo4j.ogm.mapper.domain.education.Teacher;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

import java.util.*;

import static junit.framework.Assert.assertEquals;

public class EducationTest {

    private static final CypherQuery queryProxy = new CypherQueryProxy();
    private static final SessionFactory sessionFactory = new SessionFactory(queryProxy, "org.neo4j.ogm.mapper.domain.education");
    private static final Session session = sessionFactory.openSession();

    @Test
    public void testTeachers() throws Exception {

        Map<String, Teacher> teachers = loadTeachers();

        Teacher mrThomas = teachers.get("Mr Thomas");
        Teacher mrsRoberts = teachers.get("Mrs Roberts");
        Teacher missYoung = teachers.get("Miss Young");

        checkCourseNames(mrThomas, "Maths", "English", "Physics");
        checkCourseNames(mrsRoberts, "English", "History", "PE");
        checkCourseNames(missYoung, "History", "Geography", "Philosophy and Ethics");

    }

    @Test
    public void testFetchCoursesTaughtByAllTeachers() throws Exception {

        Map<String, Teacher> teachers = loadTeachers();

        Teacher mrThomas = teachers.get("Mr Thomas");
        Teacher mrsRoberts = teachers.get("Mrs Roberts");
        Teacher missYoung = teachers.get("Miss Young");

        checkCourseNames(mrThomas, "Maths", "English", "Physics");
        checkCourseNames(mrsRoberts, "English", "History", "PE");
        checkCourseNames(missYoung, "History", "Geography", "Philosophy and Ethics");

        // this response is for an imagined request: "match p = (c:COURSE)--(o) where id(c) in [....] RETURN p"
        // i.e. we have a set of partially loaded courses attached to our teachers which we now want to
        // hydrate by getting all their relationships
        hydrateCourses();


        Set<Course> courses = new HashSet();
        for (Teacher teacher : teachers.values()) {
            for (Course course : teacher.getCourses()) {
                if (!courses.contains(course)) {
                    List<Student> students = course.getStudents();
                    if (course.getName().equals("Maths")) checkMaths(students);
                    else if (course.getName().equals("Physics")) checkPhysics(students);
                    else if (course.getName().equals("Philosophy and Ethics")) checkPhilosophyAndEthics(students);
                    else if (course.getName().equals("PE")) checkPE(students);
                    else if (course.getName().equals("History")) checkHistory(students);
                    else if (course.getName().equals("Geography")) checkGeography(students);
                    else checkEnglish(students);
                    courses.add(course);
                }

            }
        }
    }

    private void test(long hash, List<Student> students) {
        for (Student student : students) {
            hash-= student.getId();
        }
        assertEquals(0, hash);
    }

    // all students study english
    private void checkEnglish(List<Student> students) {
        long hash = 0;
        for (int i = 101; i < 127; i++) {
            hash+=i;
        }
        test(hash, students);
    }


    // all students whose ids modulo 100 are prime study geography. 1 is not considered prime
    private void checkGeography(List<Student> students) {
        long hash = 102 + 103 + 105 + 107 + 111 + 113 + 117 + 119 + 123;

        test(hash, students);
    }

    // all students with even ids study history
    private void checkHistory(List<Student> students) {
        long hash = 0;
        for (int i = 102; i < 127; i+=2) {
            hash+=i;
        }
        test(hash, students);
    }

    // every 3rd student studies PE
    private void checkPE(List<Student> students) {
        long hash = 0;
        for (int i = 103; i < 127; i+=3) {
            hash+=i;
        }
        test(hash, students);
    }

    // all students are deep thinkers
    private void checkPhilosophyAndEthics(List<Student> students) {
        long hash = 0;
        for (int i = 101; i < 127; i++) {
            hash+=i;
        }
        test(hash, students);
    }

    // all students with odd ids study physics
    private void checkPhysics(List<Student> students) {
        long hash = 0;
        for (int i = 101; i < 127; i+=2) {
            hash+=i;
        }
        test(hash, students);
    }

    // all students study maths
    private void checkMaths(List<Student> students) {
        long hash = 0;
        for (int i = 101; i < 127; i++) {
            hash+=i;
        }
        test(hash, students);
    }

    private Map<String, Teacher> loadTeachers() throws Exception {

        Map<String, Teacher> teachers = new HashMap<>();

        queryProxy.setRequest(new TeacherRequest());
        Collection<Teacher> teacherList = session.load(Teacher.class);

        for (Teacher teacher : teacherList ) {
            teachers.put(teacher.getName(), teacher);
        }

        return teachers;
    }

    // when we hydrate a set of things that are previously loaded we don't need to create them afresh
    // - the object map of the existing objects is simply extended with new data.
    // TODO: there is NO TEST asserting this.
    private void hydrateCourses() throws Exception {

        queryProxy.setRequest(new CourseRequest());
        session.load(Course.class);
    }

    private void checkCourseNames(Teacher teacher, String... courseNames) {

        int n = courseNames.length;
        List<String> test = Arrays.asList(courseNames);

        System.out.println(teacher.getName() + " courses: ");
        for (Course course : teacher.getCourses()) {
            System.out.println(course.getName());
            if (test.contains(course.getName())) {
                n--;
            }
        }
        System.out.println("---------------------");
        assertEquals(0, n);
    }
}
