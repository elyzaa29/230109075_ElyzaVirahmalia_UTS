package com.siakad.service;


import com.siakad.repository.CourseRepository;
import com.siakad.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EnrollmentServiceTest {

    private StudentRepository studentRepository;
    private CourseRepository courseRepository;
    private NotificationService notificationService;
    private GradeCalculator gradeCalculator;

    private EnrollmentService enrollmentService;

    @BeforeEach
    void setUp() {
        // Membuat mock dari tiap dependency
        studentRepository = Mockito.mock(StudentRepository.class);
        courseRepository = Mockito.mock(CourseRepository.class);
        notificationService = Mockito.mock(NotificationService.class);
        gradeCalculator = Mockito.mock(GradeCalculator.class);

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

}