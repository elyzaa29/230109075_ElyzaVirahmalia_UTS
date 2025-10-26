package com.siakad.repository;

import com.siakad.model.Course;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test untuk CourseRepository menggunakan Mockito
 */
public class CourseRepositoryTest {

    private CourseRepository courseRepository; // mock
    private Course dummyCourse;

    @BeforeEach
    void setUp() {
        // Buat mock untuk interface
        courseRepository = Mockito.mock(CourseRepository.class);

        // Siapkan dummy data
        dummyCourse = new Course();
        dummyCourse.setCourseCode("IF101");
        dummyCourse.setCourseName("Pemrograman Dasar");

        // Definisikan perilaku mock
        when(courseRepository.findByCourseCode("IF101")).thenReturn(dummyCourse);
        when(courseRepository.findByCourseCode("XX999")).thenReturn(null);

        doNothing().when(courseRepository).update(any(Course.class));

        when(courseRepository.isPrerequisiteMet("S001", "IF101")).thenReturn(true);
        when(courseRepository.isPrerequisiteMet("S002", "IF102")).thenReturn(false);
    }

    @Test
    void testFindByCourseCode_Found() {
        Course result = courseRepository.findByCourseCode("IF101");
        assertNotNull(result);
        assertEquals("Pemrograman Dasar", result.getCourseName());
    }

    @Test
    void testFindByCourseCode_NotFound() {
        Course result = courseRepository.findByCourseCode("XX999");
        assertNull(result);
    }

    @Test
    void testUpdateCourse_NoExceptionThrown() {
        assertDoesNotThrow(() -> courseRepository.update(dummyCourse));
        verify(courseRepository, times(1)).update(dummyCourse);
    }

    @Test
    void testIsPrerequisiteMet_True() {
        boolean result = courseRepository.isPrerequisiteMet("S001", "IF101");
        assertTrue(result);
    }

    @Test
    void testIsPrerequisiteMet_False() {
        boolean result = courseRepository.isPrerequisiteMet("S002", "IF102");
        assertFalse(result);
    }
}
