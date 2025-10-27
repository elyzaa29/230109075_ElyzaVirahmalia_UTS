package com.siakad.service;


import com.siakad.exception.*;
import com.siakad.model.Course;
import com.siakad.model.Enrollment;
import com.siakad.model.Student;
import com.siakad.repository.CourseRepository;
import com.siakad.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EnrollmentServiceTest {

    private StudentRepository studentRepository;
    private CourseRepository courseRepository;
    private NotificationService notificationService;
    private GradeCalculator gradeCalculator;

    private EnrollmentService enrollmentService;

    @BeforeEach
    void setUp() {
        // Membuat mock dari tiap dependency
        studentRepository = mock(StudentRepository.class);
        courseRepository = mock(CourseRepository.class);
        notificationService = mock(NotificationService.class);
        gradeCalculator = mock(GradeCalculator.class);

        // Membuat objek EnrollmentService dengan dependency mock
        enrollmentService = new EnrollmentService(
                studentRepository,
                courseRepository,
                notificationService,
                gradeCalculator
        );
    }

    // Menambahkan test untuk konstruktor depenmdensi
    @Test
    void testConstructorInjection() {
        // Pastikan semua dependency sudah tersimpan (tidak null)
        assertNotNull(enrollmentService, "EnrollmentService seharusnya terinisialisasi");
        assertNotNull(studentRepository, "StudentRepository seharusnya tidak null");
        assertNotNull(courseRepository, "CourseRepository seharusnya tidak null");
        assertNotNull(notificationService, "NotificationService seharusnya tidak null");
        assertNotNull(gradeCalculator, "GradeCalculator seharusnya tidak null");
    }

// Mendaftarkan mahasiswa ke mata kuliah (mock)

    // validasi pelajar
    @Test
    @DisplayName("Validate Student: Mahasiswa tidak ditemukan → lempar StudentNotFoundException")
    void testValidateStudent_NotFound() {
        when(studentRepository.findById("S001")).thenReturn(null);

        assertThrows(StudentNotFoundException.class,
                () -> enrollmentService.enrollCourse("S001", "CS101"));

        verify(studentRepository).findById("S001");
    }

    // cek status akademik
    @Test
    @DisplayName(" Check Academic Status: Mahasiswa SUSPENDED → lempar EnrollmentException")
    void testCheckAcademicStatus_Suspended() {
        Student student = mock(Student.class);
        when(studentRepository.findById("S001")).thenReturn(student);
        when(student.getAcademicStatus()).thenReturn("SUSPENDED");

        assertThrows(EnrollmentException.class,
                () -> enrollmentService.enrollCourse("S001", "CS101"));
    }

    // validasi mata pelajaran
    @Test
    @DisplayName("Validate Course: Course tidak ditemukan → lempar CourseNotFoundException")
    void testValidateCourse_NotFound() {
        Student student = mock(Student.class);
        when(studentRepository.findById("S001")).thenReturn(student);
        when(student.getAcademicStatus()).thenReturn("ACTIVE");
        when(courseRepository.findByCourseCode("CS101")).thenReturn(null);

        assertThrows(CourseNotFoundException.class,
                () -> enrollmentService.enrollCourse("S001", "CS101"));
    }

    // cek kapasitas
    @Test
    @DisplayName("Check Capacity: Kuota penuh → lempar CourseFullException")
    void testCheckCapacity_Full() {
        Student student = mock(Student.class);
        Course course = mock(Course.class);

        when(studentRepository.findById("S001")).thenReturn(student);
        when(student.getAcademicStatus()).thenReturn("ACTIVE");

        when(courseRepository.findByCourseCode("CS101")).thenReturn(course);
        when(course.getCapacity()).thenReturn(30);
        when(course.getEnrolledCount()).thenReturn(30);

        assertThrows(CourseFullException.class,
                () -> enrollmentService.enrollCourse("S001", "CS101"));
    }

    // cek prerequisite
    @Test
    @DisplayName("Check Prerequisite: Belum memenuhi → lempar PrerequisiteNotMetException")
    void testCheckPrerequisite_NotMet() {
        Student student = mock(Student.class);
        Course course = mock(Course.class);

        when(studentRepository.findById("S001")).thenReturn(student);
        when(student.getAcademicStatus()).thenReturn("ACTIVE");

        when(courseRepository.findByCourseCode("CS101")).thenReturn(course);
        when(course.getCapacity()).thenReturn(40);
        when(course.getEnrolledCount()).thenReturn(10);
        when(courseRepository.isPrerequisiteMet("S001", "CS101")).thenReturn(false);

        assertThrows(PrerequisiteNotMetException.class,
                () -> enrollmentService.enrollCourse("S001", "CS101"));
    }

    // kesnario berhasil
    @Test
    @DisplayName("Semua valid → Enrollment sukses dan email terkirim")
    void testEnrollCourse_Success() {
        Student student = mock(Student.class);
        Course course = mock(Course.class);

        when(studentRepository.findById("S001")).thenReturn(student);
        when(student.getAcademicStatus()).thenReturn("ACTIVE");
        when(courseRepository.findByCourseCode("CS101")).thenReturn(course);

        when(course.getCapacity()).thenReturn(40);
        when(course.getEnrolledCount()).thenReturn(10);
        when(courseRepository.isPrerequisiteMet("S001", "CS101")).thenReturn(true);
        when(course.getCourseName()).thenReturn("Algoritma");

        Enrollment enrollment = enrollmentService.enrollCourse("S001", "CS101");

        assertNotNull(enrollment);
        assertEquals("APPROVED", enrollment.getStatus());

        verify(courseRepository).update(course);
        verify(notificationService).sendEmail(eq(null), anyString(), contains("Algoritma"));
    }
// ============================= VALIDASI BATAS SKS YANG BOLAEH DIAMBIL MAKASISWA======================================
    // implementasi sederhana untuk studentrepository

    // Stub unuk StudentRepository
    static class StudentRepositoryStub implements StudentRepository {
        private final Map<String, Student> students = new HashMap<>();

        public StudentRepositoryStub() {
            students.put("S001", new Student("S001", "Park Sungho", "park@uni.ac.id", "Informatika", 5, 3.8, "ACTIVE"));
            students.put("S002", new Student("S002", "Lee Sanghyeok", "lee@uni.ac.id", "Informatika", 4, 2.6, "ACTIVE"));
            students.put("S003", new Student("S003", "Myung Jaehyun", "jaehyun@uni.ac.id", "Sistem Informasi", 4, 2.3, "ACTIVE"));
            students.put("S004", new Student("S004", "Han Taesan", "taesan@uni.ac.id", "Informatika", 3, 1.9, "ACTIVE"));
        }

        @Override
        public Student findById(String studentId) {
            return students.get(studentId);
        }

        @Override public void save(Student student) {}
        @Override public void update(Student student) {}
        @Override public List<Course> getCompletedCourses(String studentId) { return List.of(); }
        @Override public void delete(String studentId) {}
    }


    // Stub untuk rentang nilai IPK di GradeCalculator
    static class GradeCalculatorStub extends GradeCalculator {
        @Override
        public int calculateMaxCredits(double gpa) {
            if (gpa >= 3.0) return 24;
            else if (gpa >= 2.5) return 21;
            else if (gpa >= 2.0) return 18;
            else return 15;
        }
    }

    // unit test unruk validasi batas sks mahasiswa sesuai sistem IPK
    @Test
    @DisplayName("Validasi batas SKS mahasiswa - sesuai aturan IPK (pakai STUB)")
    void testValidateCreditLimit_UsingStub() {
        StudentRepositoryStub studentRepository = new StudentRepositoryStub();
        GradeCalculatorStub gradeCalculator = new GradeCalculatorStub();

        EnrollmentService service = new EnrollmentService(
                studentRepository,
                null,
                null,
                gradeCalculator
        );

        assertTrue(service.validateCreditLimit("S001", 22),
                "Park Sungho seharusnya boleh ambil 22 SKS");
        assertFalse(service.validateCreditLimit("S002", 22),
                "Lee Sanghyeok seharusnya tidak boleh ambil 22 SKS");
        assertTrue(service.validateCreditLimit("S003", 18),
                "Myung Jaehyun seharusnya boleh ambil 18 SKS");
        assertFalse(service.validateCreditLimit("S004", 16),
                "Han Taesan seharusnya tidak boleh ambil 16 SKS");
    }


    @Test
    @DisplayName("Validasi batas SKS mahasiswa - student tidak ditemukan")
    void testValidateCreditLimit_StudentNotFound() {
        StudentRepository studentRepository = new StudentRepositoryStub();
        GradeCalculator gradeCalculator = new GradeCalculatorStub();
        EnrollmentService enrollmentService =
                new EnrollmentService(studentRepository, null, null, gradeCalculator);

        assertThrows(StudentNotFoundException.class,
                () -> enrollmentService.validateCreditLimit("S999", 20),
                "Mahasiswa tidak ditemukan seharusnya lempar exception");
    }

    // Menghapsu matakuliah yang udah didaftarkan
    // stub untuk course repository
    static class CourseRepositoryStub implements CourseRepository {
        private final Map<String, Course> courses = new HashMap<>();

        public CourseRepositoryStub() {
            Course c1 = new Course();
            c1.setCourseCode("CS101");
            c1.setCourseName("Algoritma");
            c1.setCapacity(40);
            c1.setEnrolledCount(10);
            courses.put("CS101", c1);
        }

        @Override
        public Course findByCourseCode(String courseCode) {
            return courses.get(courseCode);
        }

        @Override
        public void save(Course course) { }

        @Override
        public void update(Course course) {
            courses.put(course.getCourseCode(), course);
        }

        @Override
        public boolean isPrerequisiteMet(String studentId, String courseCode) { return true; }
    }

    // Unit test membatalkan mata kuliah.
    @Test
    @DisplayName("Drop Course: Mahasiswa membatalkan mata kuliah")
    void testDropCourse_UsingStub() {
        StudentRepository studentRepository = new StudentRepositoryStub();
        CourseRepository courseRepository = new CourseRepositoryStub();

        // NotificationService stub sederhana
        EnrollmentService enrollmentService = getEnrollmentService(studentRepository, courseRepository);

        // hapus course
        enrollmentService.dropCourse("S001", "CS101");

        // Ambil course untuk dicek jumlah terdaftar
        Course course = courseRepository.findByCourseCode("CS101");
        assertEquals(9, course.getEnrolledCount(), "Enrolled count harus berkurang 1");

        // Drop course mahasiswa yang tidak ada → harus lempar exception
        assertThrows(StudentNotFoundException.class, () ->
                enrollmentService.dropCourse("S999", "CS101"));

        // Drop course yang tidak ada → harus lempar exception
        assertThrows(CourseNotFoundException.class, () ->
                enrollmentService.dropCourse("S001", "CS999"));
    }

    private static EnrollmentService getEnrollmentService(StudentRepository studentRepository, CourseRepository courseRepository) {
        NotificationService notificationService = new NotificationService() {
            @Override
            public void sendEmail(String to, String subject, String body) {
                // Tidak perlu mengirim email sebenarnya
            }

            @Override
            public void sendSMS(String phone, String message) {

            }
        };

        return new EnrollmentService(studentRepository, courseRepository, notificationService, null);
    }
}


