package mn.edu.must.sqat;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseRegistrationServiceTest {

    @Mock StudentRepository repository;
    @Mock EmailSender emailSender;

    CourseRegistrationService service;

    @BeforeEach
    void setUp() {
        service = new CourseRegistrationService(repository, emailSender);
    }

    private Student activeStudent() {
        return new Student("S1", "s1@must.edu.mn", true, List.of("CS101"));
    }

    // A1
    @Test
    void register_activeStudent_returnsOkSavesAndSendsEmail() {
        when(repository.findStudent("S1")).thenReturn(activeStudent());
        when(repository.courseExists("CS201")).thenReturn(true);
        when(repository.prerequisites("CS201")).thenReturn(List.of("CS101"));

        String result = service.register("S1", "CS201");

        assertEquals("OK", result);
        verify(repository).saveRegistration("S1", "CS201");
        verify(emailSender).send(eq("s1@must.edu.mn"), anyString(), anyString());
    }

    // A2
    @Test
    void register_unknownStudent_returnsNoStudentAndDoesNotSaveOrEmail() {
        when(repository.findStudent("X9")).thenReturn(null);

        String result = service.register("X9", "CS201");

        assertEquals("ERROR_NO_STUDENT", result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verify(emailSender, never()).send(anyString(), anyString(), anyString());
    }

    // A3
    @Test
    void register_inactiveStudent_returnsInactiveAndDoesNotTouchEmail() {
        Student inactive = new Student("S2", "s2@must.edu.mn", false, List.of("CS101"));
        when(repository.findStudent("S2")).thenReturn(inactive);

        String result = service.register("S2", "CS201");

        assertEquals("ERROR_INACTIVE_STUDENT", result);
        verifyNoInteractions(emailSender);
    }

    // A4
    @Test
    void register_missingPrerequisites_returnsPrerequisitesErrorAndDoesNotSave() {
        when(repository.findStudent("S1")).thenReturn(activeStudent());
        when(repository.courseExists("CS301")).thenReturn(true);
        when(repository.prerequisites("CS301")).thenReturn(List.of("CS101", "MATH101"));

        String result = service.register("S1", "CS301");

        assertEquals("ERROR_PREREQUISITES", result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
    }

    // A5
    @Test
    void register_emailSenderThrows_stillReturnsOkAndSaves() {
        when(repository.findStudent("S1")).thenReturn(activeStudent());
        when(repository.courseExists("CS201")).thenReturn(true);
        when(repository.prerequisites("CS201")).thenReturn(List.of("CS101"));
        doThrow(new RuntimeException("SMTP down"))
                .when(emailSender).send(anyString(), anyString(), anyString());

        String result = service.register("S1", "CS201");

        assertEquals("OK", result);
        verify(repository).saveRegistration("S1", "CS201");
    }

    // A6
    @Test
    void register_nullArgument_throwsIllegalArgumentAndDoesNotTouchDependencies() {
        assertThrows(IllegalArgumentException.class, () -> service.register(null, "CS201"));
        assertThrows(IllegalArgumentException.class, () -> service.register("S1", null));

        verifyNoInteractions(repository, emailSender);
    }

    // A7
    @Test
    void register_success_emailBodyContainsCourseCode() {
        when(repository.findStudent("S1")).thenReturn(activeStudent());
        when(repository.courseExists("CS201")).thenReturn(true);
        when(repository.prerequisites("CS201")).thenReturn(List.of("CS101"));
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        service.register("S1", "CS201");

        verify(emailSender).send(eq("s1@must.edu.mn"), anyString(), bodyCaptor.capture());
        assertTrue(bodyCaptor.getValue().contains("CS201"));
    }

    // A8
    @ParameterizedTest
    @CsvSource({
            "false, false, false, ERROR_NO_STUDENT",
            "true,  false, false, ERROR_INACTIVE_STUDENT",
            "true,  true,  false, ERROR_PREREQUISITES"
    })
    void register_errorScenarios_returnExpectedErrorCode(
            boolean studentExists, boolean active, boolean prerequisitesMet, String expected) {
        Student student = studentExists
                ? new Student("S1", "s1@must.edu.mn", active, List.of("CS101"))
                : null;
        lenient().when(repository.findStudent("S1")).thenReturn(student);
        lenient().when(repository.courseExists("CS201")).thenReturn(true);
        lenient().when(repository.prerequisites("CS201"))
                .thenReturn(prerequisitesMet ? List.of("CS101") : List.of("CS101", "MATH101"));

        assertEquals(expected, service.register("S1", "CS201"));
    }
}
