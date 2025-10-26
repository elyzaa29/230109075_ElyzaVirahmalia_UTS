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
        private Map<String, Student> students = new HashMap<>();

        public StudentRepositoryStub() {
            students.put("S001", new Student("S001", "Park Sungho", 3.8)); // IPK ≥ 3.0 → 24 SKS
            students.put("S002", new Student("S002", "Lee Sanghyeok", 2.6)); // IPK 2.5–2.99 → 21 SKS
            students.put("S003", new Student("S003", "Myung Jaehyun", 2.3)); // IPK 2.0–2.49 → 18 SKS
            students.put("S004", new Student("S004", "Han Taesan", 1.9)); // IPK < 2.0 → 15 SKS
        }

        @Override
        public Student findById(String studentId) {
            return students.get(studentId);
        }

        @Override
        public void save(Student student) { }

        @Override
        public void update(Student student) { }

        @Override
        public List<Course> getCompletedCourses(String studentId) {
            return List.of();
        }

        @Override
        public void delete(String studentId) { }
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
    @DisplayName("Validasi batas SKS mahasiswa - sesuai aturan IPK")
    void testValidateCreditLimit_UsingStub() {
        // Buat EnrollmentService baru dengan stub, jangan pakai instance dari @BeforeEach
        StudentRepository studentRepository = new StudentRepositoryStub();
        GradeCalculator gradeCalculator = new GradeCalculatorStub();
        EnrollmentService enrollmentService =
                new EnrollmentService(studentRepository, null, null, gradeCalculator);

        // Park Sungho - IPK 3.8 → max 24 SKS → ambil 22 = OK
        assertFalse(enrollmentService.validateCreditLimit("S001", 25),
                "Park Sungho seharusnya tidak boleh ambil 22 SKS");

        // Lee Sanghyeok - IPK 2.6 → max 21 SKS → ambil 22 = gagal
        assertFalse(enrollmentService.validateCreditLimit("S002", 22),
                "Lee Sanghyeok seharusnya tidak boleh ambil 22 SKS");

        // Myung Jaehyun - IPK 2.3 → max 18 SKS → ambil 18 = OK
        assertFalse(enrollmentService.validateCreditLimit("S003", 18),
                "Myung Jaehyun seharusnya tidak boleh ambil 18 SKS");

        // Han Taesan - IPK 1.9 → max 15 SKS → ambil 16 = gagal
        assertFalse(enrollmentService.validateCreditLimit("S004", 16),
                "Han Taesan seharusnya tidak boleh ambil 16 SKS");
    }

    // unit test untuk memvalidasi jika mahasiswa tidak ditemukan
    @Test
    @DisplayName("Validasi batas SKS mahasiswa - StudentNotFoundException")
    void testValidateCreditLimit_StudentNotFound() {
        StudentRepository studentRepository = new StudentRepositoryStub();
        GradeCalculator gradeCalculator = new GradeCalculatorStub();
        EnrollmentService enrollmentService =
                new EnrollmentService(studentRepository, null, null, gradeCalculator);

        assertThrows(StudentNotFoundException.class, () -> {
            enrollmentService.validateCreditLimit("S999", 18);
        });
    }

    // Menghapsu matakuliah yang udah didaftarkan
    // stub untuk course repository
    static class CourseRepositoryStub implements CourseRepository {
        private Map<String, Course> courses = new HashMap<>();

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
        NotificationService notificationService = new NotificationService() {
            @Override
            public void sendEmail(String to, String subject, String body) {
                // Tidak perlu mengirim email sebenarnya
            }

            @Override
            public void sendSMS(String phone, String message) {

            }
        };

        EnrollmentService enrollmentService =
                new EnrollmentService(studentRepository, courseRepository, notificationService, null);

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
}


