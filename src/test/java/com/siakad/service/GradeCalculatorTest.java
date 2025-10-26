package com.siakad.service;

import com.siakad.model.CourseGrade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GradeCalculatorTest {

    private GradeCalculator gradeCalculator;

    @BeforeEach
    void setUp() {
        gradeCalculator = new GradeCalculator();
    }

    // Unit test unttuk menghitung calculate GPA nya
    @Test
    @DisplayName("Menghitung GPA dengan daftar nilaii valid")
    void testCalculateGPA_withValidGrades() {
        List<CourseGrade> grades = new ArrayList<>();
        grades.add(new CourseGrade("Math", 3, 4.0));
        grades.add(new CourseGrade("Physics", 3, 3.0));

        double gpa = gradeCalculator.calculateGPA(grades);
        // (4*3 + 3*3) / (3+3) = 3.5
        assertEquals(3.5, gpa);
    }
    @Test
    @DisplayName("Menghitung GPA dengan daftar kosong menghasilkan 0")
    void testCalculateGPA_emptyList_returnsZero() {
        List<CourseGrade> grades = new ArrayList<>();
        assertEquals(0.0, gradeCalculator.calculateGPA(grades), "GPA harus 0.0 untuk daftar kosong");
    }

    @Test
    @DisplayName("Menghitung GPA dengan daftar null menghasilkan 0")
    void testCalculateGPA_nullList_returnsZero() {
        assertEquals(0.0, gradeCalculator.calculateGPA(null), "GPA harus 0.0 untuk daftar null");
    }

    @Test
    @DisplayName("GPA dengan grade invalid harus melempar exception")
    void testCalculateGPA_invalidGrade_throwsException() {
        List<CourseGrade> grades = new ArrayList<>();
        grades.add(new CourseGrade("Math", 3, 4.5)); // Invalid grade > 4.0

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                gradeCalculator.calculateGPA(grades)
        );

        assertTrue(exception.getMessage().contains("Invalid grade point"));

    }

    @Test
    @DisplayName("calculateGPA dengan grade point < 0 atau > 4 harus melempar exception")
    void testCalculateGPA_invalidGradePoints() {
        List<CourseGrade> grades = new ArrayList<>();
        grades.add(new CourseGrade("Math", 3, -1.0)); // < 0
        grades.add(new CourseGrade("Physics", 3, 5.0)); // > 4.0

        for (CourseGrade grade : grades) {
            Exception exception = assertThrows(IllegalArgumentException.class, () ->
                    gradeCalculator.calculateGPA(List.of(grade))
            );
            assertTrue(exception.getMessage().contains("Invalid grade point"));
        }
    }

    @Test
    @DisplayName("calculateGPA dengan semua SKS = 0 harus mengembalikan 0.0")
    void testCalculateGPA_totalCreditsZero() {
        List<CourseGrade> grades = new ArrayList<>();
        grades.add(new CourseGrade("Math", 0, 4.0));
        grades.add(new CourseGrade("Physics", 0, 3.0));

        double gpa = gradeCalculator.calculateGPA(grades);
        assertEquals(0.0, gpa, "GPA harus 0.0 jika total SKS = 0");
    }


    // Unit Test untuk determinasi status akademi
    @Test
    @DisplayName("Semester 1-2 dengan GPA >= 2.0 harus ACTIVE")
    void testDetermineAcademicStatus_semester1_active() {
        assertEquals("ACTIVE", gradeCalculator.determineAcademicStatus(2.0, 1));
        assertEquals("ACTIVE", gradeCalculator.determineAcademicStatus(3.5, 2));
    }

    @Test
    @DisplayName("Semester 1-2 dengan GPA < 2.0 harus PROBATION")
    void testDetermineAcademicStatus_semester1_probation() {
        assertEquals("PROBATION", gradeCalculator.determineAcademicStatus(1.9, 2));
    }

    @Test
    @DisplayName("Semester 3-4 dengan GPA >= 2.25 harus ACTIVE")
    void testDetermineAcademicStatus_semester3_active() {
        assertEquals("ACTIVE", gradeCalculator.determineAcademicStatus(2.25, 3));
        assertEquals("ACTIVE", gradeCalculator.determineAcademicStatus(3.0, 4));
    }

    @Test
    @DisplayName("Semester 3-4 dengan GPA 2.0-2.24 harus PROBATION")
    void testDetermineAcademicStatus_semester3_probation() {
        assertEquals("PROBATION", gradeCalculator.determineAcademicStatus(2.1, 3));
    }

    @Test
    @DisplayName("Semester 3-4 dengan GPA < 2.0 harus SUSPENDED")
    void testDetermineAcademicStatus_semester3_suspended() {
        assertEquals("SUSPENDED", gradeCalculator.determineAcademicStatus(1.9, 4));
    }

    @Test
    @DisplayName("Semester 5+ dengan GPA >= 2.5 harus ACTIVE")
    void testDetermineAcademicStatus_semester5_active() {
        assertEquals("ACTIVE", gradeCalculator.determineAcademicStatus(2.5, 5));
    }

    @Test
    @DisplayName("Semester 5+ dengan GPA 2.0-2.49 harus PROBATION")
    void testDetermineAcademicStatus_semester5_probation() {
        assertEquals("PROBATION", gradeCalculator.determineAcademicStatus(2.2, 6));
    }

    @Test
    @DisplayName("Semester 5+ dengan GPA < 2.0 harus SUSPENDED")
    void testDetermineAcademicStatus_semester5_suspended() {
        assertEquals("SUSPENDED", gradeCalculator.determineAcademicStatus(1.5, 7));
    }

    @Test
    @DisplayName("GPA invalid di determineAcademicStatus harus melempar exception")
    void testDetermineAcademicStatus_invalidGPA_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                gradeCalculator.determineAcademicStatus(-1.0, 1)
        );
        assertThrows(IllegalArgumentException.class, () ->
                gradeCalculator.determineAcademicStatus(4.5, 1)
        );
    }

    @Test
    @DisplayName("Semester invalid di determineAcademicStatus harus melempar exception")
    void testDetermineAcademicStatus_invalidSemester_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                gradeCalculator.determineAcademicStatus(3.0, 0)
        );
    }

    // test untuk menghitung maksimal mata kuliah
    @Test
    @DisplayName("Menghitung maksimal SKS berdasarkan GPA")
    void testCalculateMaxCredits_variousGPA() {
        assertEquals(24, gradeCalculator.calculateMaxCredits(3.5));
        assertEquals(21, gradeCalculator.calculateMaxCredits(2.7));
        assertEquals(18, gradeCalculator.calculateMaxCredits(2.3));
        assertEquals(15, gradeCalculator.calculateMaxCredits(1.5));
    }

    @Test
    @DisplayName("GPA invalid di calculateMaxCredits harus melempar exception")
    void testCalculateMaxCredits_invalidGPA_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> gradeCalculator.calculateMaxCredits(-0.1));
        assertThrows(IllegalArgumentException.class, () -> gradeCalculator.calculateMaxCredits(4.1));
    }
}
