package com.luv2code.springmvc;

import com.luv2code.springmvc.models.CollegeStudent;
import com.luv2code.springmvc.models.GradebookCollegeStudent;
import com.luv2code.springmvc.models.HistoryGrade;
import com.luv2code.springmvc.models.MathGrade;
import com.luv2code.springmvc.models.ScienceGrade;
import com.luv2code.springmvc.repository.HistoryGradesDao;
import com.luv2code.springmvc.repository.MathGradesDao;
import com.luv2code.springmvc.repository.ScienceGradesDao;
import com.luv2code.springmvc.repository.StudentDao;
import com.luv2code.springmvc.service.StudentAndGradeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource("/application-test.properties")
@SpringBootTest
public class StudentAndGradeServiceTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private StudentAndGradeService studentService;

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private MathGradesDao mathGradeDao;
    
    @Autowired
    private ScienceGradesDao scienceGradeDao;
    
    @Autowired
    private HistoryGradesDao historyGradeDao;
    
	@Value("${sql.script.create.student}")
	private String sqlAddStudent;
	
	@Value("${sql.script.delete.student}")
	private String sqlDeleteStudent;
	
	@Value("${sql.script.create.math.grade}")
	private String sqlAddMathGrade;
	
	@Value("${sql.script.delete.math.grade}")
	private String sqlDeleteMathGrade;
	
	@Value("${sql.script.create.science.grade}")
	private String sqlAddScienceGrade;
	
	@Value("${sql.script.delete.science.grade}")
	private String sqlDeleteScienceGrade;
	
	@Value("${sql.script.create.history.grade}")
	private String sqlAddHistoryGrade;
	
	@Value("${sql.script.delete.history.grade}")
	private String sqlDeleteHistoryGrade;

    @BeforeEach
    public void setupDatabase() {
        jdbc.execute(sqlAddStudent);
        jdbc.execute(sqlAddMathGrade);
        jdbc.execute(sqlAddScienceGrade);
        jdbc.execute(sqlAddHistoryGrade);
    }
    @Test
    public void createStudentService() {

        studentService.createStudent("Lilo", "Lilo",
                "lilo@babinkuk.com");

         CollegeStudent student = studentDao.findByEmailAddress("lilo@babinkuk.com");

         assertEquals("lilo@babinkuk.com", student.getEmailAddress(), "find by email");
    }

    @Test
    public void isStudentNullCheck() {

        assertTrue(studentService.checkIfStudentIsNull(1));

        assertFalse(studentService.checkIfStudentIsNull(0));
    }

    @Test
    public void deleteStudentService() {
		
    	// check for student and grades 
		Optional<CollegeStudent> deletedCollegeStudent = studentDao.findById(1);
		Optional<MathGrade> deletedMathGrade = mathGradeDao.findById(1);
		Optional<ScienceGrade> deletedScienceGrade = scienceGradeDao.findById(1);
		Optional<HistoryGrade> deletedHistoryGrade = historyGradeDao.findById(1);
		
		// should exist
		assertTrue(deletedCollegeStudent.isPresent(), "Return True deletedStudent");
		assertTrue(deletedMathGrade.isPresent(), "Return True deletedMathGrade");
		assertTrue(deletedScienceGrade.isPresent(), "Return True deletedScienceGrade");
		assertTrue(deletedHistoryGrade.isPresent(), "Return True deletedHistoryGrade");
		
		// delete student
		studentService.deleteStudent(1);
		
		// check for student and grades 
		deletedCollegeStudent = studentDao.findById(1);
		deletedMathGrade = mathGradeDao.findById(1);
		deletedScienceGrade = scienceGradeDao.findById(1);
		deletedHistoryGrade = historyGradeDao.findById(1);
		
		// should be deleted
		assertFalse(deletedCollegeStudent.isPresent(), "Return False");
		assertFalse(deletedMathGrade.isPresent(), "Return False deletedMathGrade");
		assertFalse(deletedScienceGrade.isPresent(), "Return False deletedScienceGrade");
		assertFalse(deletedHistoryGrade.isPresent(), "Return False deletedHistoryGrade");
    }

    @Sql("/insertData.sql")
    @Test
    public void getGradebookService() {

		Iterable<CollegeStudent> iterableCollegeStudents = studentService.getGradebook();
		
		List<CollegeStudent> collegeStudents = new ArrayList<>();
		
		for (CollegeStudent collegeStudent : iterableCollegeStudents) {
		    collegeStudents.add(collegeStudent);
		}
		
		assertEquals(5, collegeStudents.size());
    }
    
	@Test
	public void createGradeService() {
		
		// create grade (grade, id, type)
		assertTrue(studentService.createGrade(80.50, 1, "math"));
		assertTrue(studentService.createGrade(80.50, 1, "science"));
		assertTrue(studentService.createGrade(80.50, 1, "history"));
		
		// get all grades with studentid
		Iterable<MathGrade> mathGrades = mathGradeDao.findGradeByStudentId(1);
		Iterable<ScienceGrade> scienceGrades = scienceGradeDao.findGradeByStudentId(1);
		Iterable<HistoryGrade> historyGrades = historyGradeDao.findGradeByStudentId(1);
		
		// verify there are grades (version 1)
		assertTrue(mathGrades.iterator().hasNext(), "Student has math grades");
		assertTrue(scienceGrades.iterator().hasNext(), "Student has science grades");
		assertTrue(historyGrades.iterator().hasNext(), "Student has history grades");
		
		// verify there are grades (version 2)
		// size=2, 1st in beforeeach and 2nd in this method
		assertTrue(((Collection<MathGrade>) mathGrades).size() == 2, "Student has math grades");
		assertTrue(((Collection<ScienceGrade>) scienceGrades).size() == 2, "Student has science grades");
		assertTrue(((Collection<HistoryGrade>) historyGrades).size() == 2, "Student has history grades");
	}
	
	@Test
	public void createGradeServiceReturnFalse() {
		
		// invalid grade (outside 0-100)
		assertFalse(studentService.createGrade(105.00, 1, "math"));
		assertFalse(studentService.createGrade(-5.00, 1, "science"));
		
		// invalid studentid
		assertFalse(studentService.createGrade(15.00, 99, "math"));
		
		// invalid gradetype
		assertFalse(studentService.createGrade(95.00, 1, "biology"));
	}
	
	@Test
	public void deleteGradeService() {
		
		// if delete is ok, return studentid
		assertEquals(1, studentService.deleteGrade(1, "math"), "Return student after grade delete");
		assertEquals(1, studentService.deleteGrade(1, "science"), "Return student after grade delete");
		assertEquals(1, studentService.deleteGrade(1, "history"), "Return student after grade delete");
	}
	
	@Test
	public void deleteGradeServiceReturnFalse() {
		
		// invalid grade id
		assertEquals(0, studentService.deleteGrade(0, "math"), "Invalid grade id math");
				
		// invalid gradetype
		assertEquals(0, studentService.deleteGrade(1, "chemistry"), "Invalid grade type chemistry");
		
	}
	
	@Test
	public void studentInformation() {
		
		// get student name, email and grades
		GradebookCollegeStudent gradebookCollegeStudent = studentService.studentInformation(1);
		
		// assert data values (1, 'Samba', 'Rumba', 'samba.rumba@babinkuk.com')");
        assertEquals(1, gradebookCollegeStudent.getId());
		assertEquals("Samba", gradebookCollegeStudent.getFirstname());
		assertEquals("Rumba", gradebookCollegeStudent.getLastname());
		assertEquals("samba.rumba@babinkuk.com", gradebookCollegeStudent.getEmailAddress());
		
		// assert studet grades
		assertTrue(gradebookCollegeStudent.getStudentGrades().getMathGradeResults().size() == 1, "Math grades");
		assertTrue(gradebookCollegeStudent.getStudentGrades().getScienceGradeResults().size() == 1, "Science grades");
		assertTrue(gradebookCollegeStudent.getStudentGrades().getHistoryGradeResults().size() == 1, "History grades");
	}
	
	@Test
	public void studentInformationReturnFalse() {
		
		// get student name, email and grades
		// student des not exist
		GradebookCollegeStudent gradebookCollegeStudent = studentService.studentInformation(99);
		
		// assert
        assertNull(gradebookCollegeStudent, "Should return null");
	}
	
    @AfterEach
    public void setupAfterTransaction() {
    	jdbc.execute(sqlDeleteStudent);
    	jdbc.execute(sqlDeleteMathGrade);
    	jdbc.execute(sqlDeleteScienceGrade);
    	jdbc.execute(sqlDeleteHistoryGrade);
    }
}
