package com.babinkuk.springmvc;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import com.babinkuk.springmvc.models.CollegeStudent;
import com.babinkuk.springmvc.models.MathGrade;
import com.babinkuk.springmvc.repository.MathGradesDao;
import com.babinkuk.springmvc.repository.StudentDao;
import com.babinkuk.springmvc.service.StudentAndGradeService;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

@TestPropertySource("/application-test.properties")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class GradebookControllerTest {
	
	private static MockHttpServletRequest request;
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Mock
	StudentAndGradeService studentCreateServiceMock;
	
	@Autowired
	private JdbcTemplate jdbc;
	
	@Autowired
	private StudentDao studentDao;
	
	@Autowired
	private MathGradesDao mathGradeDao;
	
	@Autowired
	private CollegeStudent student;
	
	@Autowired
	ObjectMapper objectMApper;
	
	@Autowired
	private MockMvc mockMvc;
	
	@Value("${sql.script.create.student}")
	private String sqlAddStudent;
	
	@Value("${sql.script.create.math.grade}")
	private String sqlAddMathGrade;
	
	@Value("${sql.script.create.science.grade}")
	private String sqlAddScienceGrade;
	
	@Value("${sql.script.create.history.grade}")
	private String sqlAddHistoryGrade;
	
	@Value("${sql.script.delete.student}")
	private String sqlDeleteStudent;
	
	@Value("${sql.script.delete.math.grade}")
	private String sqlDeleteMathGrade;
	
	@Value("${sql.script.delete.science.grade}")
	private String sqlDeleteScienceGrade;
	
	@Value("${sql.script.delete.history.grade}")
	private String sqlDeleteHistoryGrade;
	
	public static final MediaType APPLICATION_JSON_UTF8 = MediaType.APPLICATION_JSON;
	
	@BeforeAll
	public static void setup() {
		
		// init
		request = new MockHttpServletRequest();
		
		request.setParameter("firstname", "Samba");
		request.setParameter("lastname", "Rumba");
		request.setParameter("emailAddress", "samba.rumba@babinkuk.com");
	}
	
	@BeforeEach
	public void setupDatabase() {
		jdbc.execute(sqlAddStudent);
		jdbc.execute(sqlAddMathGrade);
		jdbc.execute(sqlAddScienceGrade);
		jdbc.execute(sqlAddHistoryGrade);
	}
	
	@AfterEach
	public void setupAfterTransaction() {
		jdbc.execute(sqlDeleteStudent);
		jdbc.execute(sqlDeleteMathGrade);
		jdbc.execute(sqlDeleteScienceGrade);
		jdbc.execute(sqlDeleteHistoryGrade);
	}
	
	@Test
	public void getStudentsHttpRequest() throws Exception {
		
		// get all students and grades
		mockMvc.perform(MockMvcRequestBuilders.get("/"))
			.andExpect(status().isOk())
			.andExpect(content().contentType(APPLICATION_JSON_UTF8))
			.andExpect(jsonPath("$", hasSize(1))); // verify that json root element $ is size 1
		
		// add another student
		student.setFirstname("Tito");
		student.setLastname("Tito");
		student.setEmailAddress("tito@babinkuk.com");
		entityManager.persist(student);
		entityManager.flush();
		
		// get all students and grades
		mockMvc.perform(MockMvcRequestBuilders.get("/"))
			.andExpect(status().isOk())
			.andExpect(content().contentType(APPLICATION_JSON_UTF8))
			.andExpect(jsonPath("$", hasSize(2))); // verify that json root element $ is now size 2
		
	}
	
	@Test
	public void createStudentsHttpRequest() throws Exception {
		
		// set student
		student.setFirstname("Tito");
		student.setLastname("Tito");
		student.setEmailAddress("tito@babinkuk.com");
		
		// create student
		mockMvc.perform(MockMvcRequestBuilders.post("/")
			.contentType(APPLICATION_JSON_UTF8)
			.content(objectMApper.writeValueAsString(student))) // generate json from java object
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(2))); // verify that json root element $ is now size 2
		
		// additional check
		CollegeStudent verifyStudent = studentDao.findByEmailAddress("tito@babinkuk.com");
		assertNotNull(verifyStudent, "Student should not be null");
	}
	
	@Test
	public void deleteStudentsHttpRequest() throws Exception {
		
		// check if studentid 1 exists
		assertTrue(studentDao.findById(1).isPresent());
		
		// delete student
		mockMvc.perform(MockMvcRequestBuilders.delete("/student/{id}", 1))
			.andExpect(status().isOk())
			.andExpect(content().contentType(APPLICATION_JSON_UTF8))
			.andExpect(jsonPath("$", hasSize(0))); // verify json root element $ is size 0
		
		// additional check
		assertFalse(studentDao.findById(1).isPresent());
	}
	
	@Test
	public void deleteStudentsHttpRequestErrorPage() throws Exception {
		
		// check if studentid 99 exists, invalid studentid
		assertFalse(studentDao.findById(99).isPresent());
		
		// delete student
		mockMvc.perform(MockMvcRequestBuilders.delete("/student/{id}", 99))
			.andExpect(status().is4xxClientError())
			.andExpect(jsonPath("$.status", is(404))) // verify json root element status $ is 404
			.andExpect(jsonPath("$.message", is("Student or Grade was not found"))); // verify json root element message
	}
	
	@Test
	public void studentInformationHttpRequest() throws Exception {
		
		// get student
		Optional<CollegeStudent> student = studentDao.findById(1);
		
		assertTrue(student.isPresent());
		
		// get student information
		mockMvc.perform(MockMvcRequestBuilders.get("/studentInformation/{id}", 1))
			.andExpect(status().isOk())
			.andExpect(content().contentType(APPLICATION_JSON_UTF8))
			.andExpect(jsonPath("$.id", is(1))) // verify json root element id is 1
			.andExpect(jsonPath("$.firstname", is("Eric"))) //verify json element firstname
			.andExpect(jsonPath("$.lastname", is("Roby"))) //verify json element lastname
			.andExpect(jsonPath("$.emailAddress", is("eric.roby@luv2code_school.com"))); //verify json element emailAddress
	}
	
	@Test
	public void studentInformationHttpRequestEmptyResponse() throws Exception {
		
		// get student, invalid student id
		Optional<CollegeStudent> student = studentDao.findById(99);
		
		assertFalse(student.isPresent());
		
		// get student information
		mockMvc.perform(MockMvcRequestBuilders.get("/studentInformation/{id}", 99))
			.andExpect(status().is4xxClientError())
			.andExpect(status().is4xxClientError())
			.andExpect(jsonPath("$.status", is(404))) // verify json root element status $ is 404
			.andExpect(jsonPath("$.message", is("Student or Grade was not found"))); // verify json root element message
	}
	
	@Test
	public void createValidGradeHttpRequest() throws Exception {
		
		// create grade
		mockMvc.perform(MockMvcRequestBuilders.post("/grades")
			.contentType(APPLICATION_JSON_UTF8)
			.param("grade", "85.00")
			.param("gradeType", "math")
			.param("studentId", "1"))
			.andExpect(status().isOk())
			.andExpect(content().contentType(APPLICATION_JSON_UTF8))
			.andExpect(jsonPath("$.id", is(1))) // verify json root element id is 1
			.andExpect(jsonPath("$.firstname", is("Eric"))) //verify json element firstname
			.andExpect(jsonPath("$.lastname", is("Roby"))) //verify json element lastname
			.andExpect(jsonPath("$.emailAddress", is("eric.roby@luv2code_school.com"))) //verify json element emailAddress
			.andExpect(jsonPath("$.studentGrades.mathGradeResults", hasSize(2))); // verify there are 2 math grades
	}
	
	@Test
	public void createValidGradeHttpRequestInavlidStudent() throws Exception {
		
		// create grade
		mockMvc.perform(MockMvcRequestBuilders.post("/grades")
			.contentType(APPLICATION_JSON_UTF8)
			.param("grade", "85.00")
			.param("gradeType", "math")
			.param("studentId", "99")) // invalid student id
			.andExpect(status().is4xxClientError())
			.andExpect(jsonPath("$.status", is(404))) // verify json root element status $ is 404
			.andExpect(jsonPath("$.message", is("Student or Grade was not found"))); // verify json root element message
	}
	
	@Test
	public void createInvalidGradeHttpRequest() throws Exception {
		
		// create grade
		mockMvc.perform(MockMvcRequestBuilders.post("/grades")
			.contentType(APPLICATION_JSON_UTF8)
			.param("grade", "185.00") // invalid grade
			.param("gradeType", "math")
			.param("studentId", "1"))
			.andExpect(status().is4xxClientError())
			.andExpect(jsonPath("$.status", is(404))) // verify json root element status $ is 404
			.andExpect(jsonPath("$.message", is("Student or Grade was not found"))); // verify json root element message
		
		// create grade
		mockMvc.perform(MockMvcRequestBuilders.post("/grades")
			.contentType(APPLICATION_JSON_UTF8)
			.param("grade", "185.00")
			.param("gradeType", "biology") // invalid gradeType
			.param("studentId", "1"))
			.andExpect(status().is4xxClientError())
			.andExpect(jsonPath("$.status", is(404))) // verify json root element status $ is 404
			.andExpect(jsonPath("$.message", is("Student or Grade was not found"))); // verify json root element message
	}
	
	@Test
	public void deleteGradeHttpRequest() throws Exception {
		
		// check if studentid 1 exists
		Optional<MathGrade> mathGrade = mathGradeDao.findById(1);
		
		assertTrue(mathGrade.isPresent());
		
		// delete grade
		mockMvc.perform(MockMvcRequestBuilders.delete("/grades/{id}/{gradeType}", 1, "math"))
			.andExpect(status().isOk())
			.andExpect(content().contentType(APPLICATION_JSON_UTF8))
			.andExpect(jsonPath("$.id", is(1))) // verify json root element id is 1
			.andExpect(jsonPath("$.firstname", is("Eric"))) //verify json element firstname
			.andExpect(jsonPath("$.lastname", is("Roby"))) //verify json element lastname
			.andExpect(jsonPath("$.emailAddress", is("eric.roby@luv2code_school.com"))) //verify json element emailAddress
			.andExpect(jsonPath("$.studentGrades.mathGradeResults", hasSize(0))); // verify there are no math grades
	}
	
	@Test
	public void deleteGradeHttpRequestGradeNotFound() throws Exception {
		
		// delete grade, invalid data
		mockMvc.perform(MockMvcRequestBuilders.delete("/grades/{id}/{gradeType}", 1, "biology"))
			.andExpect(status().is4xxClientError())
			.andExpect(jsonPath("$.status", is(404))) // verify json root element status $ is 404
			.andExpect(jsonPath("$.message", is("Student or Grade was not found"))); // verify json root element message
		
		// delete grade, invalid grade id
		mockMvc.perform(MockMvcRequestBuilders.delete("/grades/{id}/{gradeType}", 211, "math"))
			.andExpect(status().is4xxClientError())
			.andExpect(jsonPath("$.status", is(404))) // verify json root element status $ is 404
			.andExpect(jsonPath("$.message", is("Student or Grade was not found"))); // verify json root element message
		
	}
}