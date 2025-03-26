package com.han.pwac.pinguins.backend.services;

import com.han.pwac.pinguins.backend.domain.DTO.FileDto;
import com.han.pwac.pinguins.backend.domain.DTO.GetSkillDto;
import com.han.pwac.pinguins.backend.domain.DTO.GetSkillWithDescriptionDto;
import com.han.pwac.pinguins.backend.domain.DTO.StudentRepositoryDto;
import com.han.pwac.pinguins.backend.domain.EmailPart;
import com.han.pwac.pinguins.backend.exceptions.DuplicateValueException;
import com.han.pwac.pinguins.backend.exceptions.InvalidDataException;
import com.han.pwac.pinguins.backend.exceptions.NotFoundException;
import com.han.pwac.pinguins.backend.repository.SkillDao;
import com.han.pwac.pinguins.backend.repository.contract.ICronJobDao;
import com.han.pwac.pinguins.backend.services.contract.IBaseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Timestamp;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SkillServiceTest {

    @InjectMocks
    private SkillService sut; // System Under Test
    @Mock
    private IBaseService<StudentRepositoryDto> studentService;
    @Mock
    private ICronJobDao cronJobDao;

    @Mock
    private SkillDao skillDao;

    @Test
    public void getAllSkills_HappyPath() {
        // Arrange
        when(skillDao.getAll()).thenReturn(List.of(
                new GetSkillDto(1, "Skill1", true),
                new GetSkillDto(2, "Skill2", false),
                new GetSkillDto(3, "Skill3", true)
        ));

        // Act
        Collection<GetSkillDto> result = sut.getAllSkills();

        // Assert
        verify(skillDao, times(1)).getAll();
        verifyNoMoreInteractions(skillDao);
        assertEquals(result.size(), 3);
    }

    @Test
    public void getAllSkills_NoSkills() {
        // Arrange
        when(skillDao.getAll()).thenReturn(Collections.emptyList());

        // Act
        Collection<GetSkillDto> result = sut.getAllSkills();

        // Assert
        verify(skillDao, times(1)).getAll();
        verifyNoMoreInteractions(skillDao);
        assertEquals(result, Collections.emptyList());
    }

    @Test
    void addSkillToTask_HappyPath() {
        // Arrange
        Integer taskId = 1;
        List<Integer> newSkills = Arrays.asList(101, 102, 103);

        // Mock `existsTaskSkill` to return `false` for all skills
        when(skillDao.existsTaskSkill(taskId, 101)).thenReturn(false);
        when(skillDao.existsTaskSkill(taskId, 102)).thenReturn(false);
        when(skillDao.existsTaskSkill(taskId, 103)).thenReturn(false);

        // Act
        sut.addSkillToTask(newSkills, taskId);

        // Assert
        verify(skillDao, times(1)).removeSkillsFromTask(taskId);
        verify(skillDao, times(1)).addTaskSkill(taskId, 101);
        verify(skillDao, times(1)).addTaskSkill(taskId, 102);
        verify(skillDao, times(1)).addTaskSkill(taskId, 103);
        verifyNoMoreInteractions(skillDao);
    }

    @Test
    void addSkillToTask_SkillAlreadyExists() {
        // Arrange
        Integer taskId = 1;
        List<Integer> newSkills = Arrays.asList(101, 102);

        // Act
        when(skillDao.existsTaskSkill(taskId, 101)).thenReturn(true);
        when(skillDao.existsTaskSkill(taskId, 102)).thenReturn(false);
        sut.addSkillToTask(newSkills, taskId);

        // Assert
        verify(skillDao, times(1)).removeSkillsFromTask(taskId);
        verify(skillDao, never()).addTaskSkill(taskId, 101);
        verify(skillDao, times(1)).addTaskSkill(taskId, 102);
        verifyNoMoreInteractions(skillDao);
    }

    @Test
    void addSkillToTask_NullSkills_ThrowsException() {
        // Act & Assert
        assertThrows(NotFoundException.class, () -> sut.addSkillToTask(null, 1), "geen skill gekozen");
        verifyNoInteractions(skillDao);
    }

    @Test
    void addSkillToTask_EmptySkills_ThrowsException() {
        // Act & Assert
        assertThrows(NotFoundException.class, () -> sut.addSkillToTask(null, 1), "geen skill gekozen");
        verifyNoInteractions(skillDao);
    }

    @Test
    void addSkillToTask_NullTaskId_ThrowsException() {
        // Arrange
        List<Integer> newSkills = Arrays.asList(101, 102);

        // Act & Assert
        assertThrows(NotFoundException.class, () -> sut.addSkillToTask(newSkills, null), "geen task gevonden");
        verifyNoInteractions(skillDao);
    }

    @Test
    void createSkill_HappyPath() {
        // Arrange
        String skillName = "skillName";
        when(skillDao.getAll()).thenReturn(Collections.emptyList());
        when(skillDao.getLastInsertedId()).thenReturn(1);

        // Act
        GetSkillDto result = sut.createSkill(skillName);

        // Assert
        verify(skillDao, times(1)).getAll();
        verify(skillDao, times(1)).add(new GetSkillDto(0, skillName, true));
        verify(skillDao, times(1)).getLastInsertedId();
        verifyNoMoreInteractions(skillDao);

        assertEquals(1, result.skillId());
        assertEquals(skillName, result.name());
        assertTrue(result.isPending());
    }

    @Test
    public void createSkill_SkillNameGetsFormatted() {
        // Arrange
        String skillName = "   skill  name      with spaces  ";
        when(skillDao.getAll()).thenReturn(Collections.emptyList());
        when(skillDao.getLastInsertedId()).thenReturn(1);

        // Act
        sut.createSkill(skillName);

        // Assert
        verify(skillDao, times(1)).add(new GetSkillDto(0, "skill name with spaces", true));
    }

    @Test
    public void createSkill_SkillAlreadyExists() {
        // Arrange
        String skillName = "skillName";
        when(skillDao.getAll()).thenReturn(Collections.singletonList(new GetSkillDto(1, skillName, false)));

        // Act & Assert
        assertThrows(DuplicateValueException.class, () -> sut.createSkill(skillName), "Deze skill bestaat al");
    }

    @Test
    public void createSkill_selectLastInsertedIdReturnsEmpty() {
        // Arrange
        String skillName = "skillName";
        when(skillDao.getAll()).thenReturn(Collections.emptyList());
        when(skillDao.getLastInsertedId()).thenReturn(0);

        // Act & Assert
        assertThrows(NotFoundException.class, () -> sut.createSkill(skillName), "Er ging iets mis bij het aanmaken van de skill");
    }

    @Test
    public void createSkill_existingSkillsWithDifferentName() {
        // Arrange
        String skillName = "skillName";
        when(skillDao.getAll()).thenReturn(Collections.singletonList(new GetSkillDto(1, "differentName", false)));
        when(skillDao.getLastInsertedId()).thenReturn(1);

        // Act
        sut.createSkill(skillName);

        // Assert
        verify(skillDao, times(1)).getAll();
        verify(skillDao, times(1)).add(new GetSkillDto(0, skillName, true));
        verify(skillDao, times(1)).getLastInsertedId();
        verifyNoMoreInteractions(skillDao);
    }

    @Test
    public void updateSkillName_HappyPath() {
        // Arrange
        int skillId = 1;
        String skillName = "Updated Skill";
        when(skillDao.findById(skillId)).thenReturn(Optional.of(new GetSkillDto(1, "Old Skill", false)));

        // Act
        sut.updateSkillName(skillId, skillName);

        // Assert
        verify(skillDao, times(1)).findById(skillId);
        verify(skillDao, times(1)).getAll();
        verify(skillDao, times(1)).updateSkillName(skillId, skillName);
        verifyNoMoreInteractions(skillDao);
    }

    @Test
    public void updateSkillName_existingSkillsWithDifferentName() {
        // Arrange
        int skillId = 1;
        String skillName = "Updated Skill";
        when(skillDao.findById(skillId)).thenReturn(Optional.of(new GetSkillDto(1, "Old Skill", false)));
        when(skillDao.getAll()).thenReturn(Collections.singletonList(new GetSkillDto(2, "Not Updated Skill", true)));

        // Act
        sut.updateSkillName(skillId, skillName);

        // Assert
        verify(skillDao, times(1)).findById(skillId);
        verify(skillDao, times(1)).getAll();
        verify(skillDao, times(1)).updateSkillName(skillId, skillName);
    }

    @Test
    public void updateSkillName_ZeroSkillId() {
        // Arrange
        int skillId = 0;
        String skillName = "Updated Skill";

        // Act & Assert
        assertThrows(InvalidDataException.class, () -> sut.updateSkillName(skillId, skillName), "Ongeldige skillId");
        verifyNoInteractions(skillDao);
    }

    @Test
    public void updateSkillName_NegativeSkillId() {
        // Arrange
        int skillId = -1;
        String skillName = "Updated Skill";

        // Act & Assert
        assertThrows(InvalidDataException.class, () -> sut.updateSkillName(skillId, skillName), "Ongeldige skillId");
        verifyNoInteractions(skillDao);
    }

    @Test
    public void updateSkillName_SkillNotFound() {
        // Arrange
        int skillId = 1;
        String skillName = "Updated Skill";
        when(skillDao.findById(skillId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> sut.updateSkillName(skillId, skillName), "De geselecteerde skill is niet gevonden");
        verify(skillDao, times(1)).findById(skillId);
        verifyNoMoreInteractions(skillDao);
    }

    @Test
    public void updateSkillName_SkillNameTooLong() {
        // Arrange
        int skillId = 1;
        String skillName = "12345678901234567890123456789012345678901234567890!";
        when(skillDao.findById(skillId)).thenReturn(Optional.of(new GetSkillDto(1, "Old Skill", false)));

        // Act & Assert
        assertThrows(InvalidDataException.class, () -> sut.updateSkillName(skillId, skillName), "De naam van de skill mag maximaal 50 karakters bevatten");
        verify(skillDao, times(1)).findById(skillId);
        verifyNoMoreInteractions(skillDao);
    }

    @Test
    public void updateSkillName_SkillNameFiftyCharacters() {
        // Arrange
        int skillId = 1;
        String skillName = "12345678901234567890123456789012345678901234567890";
        when(skillDao.findById(skillId)).thenReturn(Optional.of(new GetSkillDto(1, "Old Skill", false)));

        // Act
        sut.updateSkillName(skillId, skillName);

        // Assert
        verify(skillDao, times(1)).findById(skillId);
        verify(skillDao, times(1)).getAll();
        verify(skillDao, times(1)).updateSkillName(skillId, skillName);
        verifyNoMoreInteractions(skillDao);
    }

    @Test
    public void updateSkillName_SkillNameAlreadyExists() {
        // Arrange
        int skillId = 1;
        String skillName = "Updated Skill";
        when(skillDao.findById(skillId)).thenReturn(Optional.of(new GetSkillDto(1, "Old Skill", false)));
        when(skillDao.getAll()).thenReturn(Collections.singletonList(new GetSkillDto(2, "Updated Skill", true)));

        // Act & Assert
        assertThrows(DuplicateValueException.class, () -> sut.updateSkillName(skillId, skillName), "Er bestaat al een andere skill met deze naam");
        verify(skillDao, times(1)).findById(skillId);
        verify(skillDao, times(1)).getAll();
        verifyNoMoreInteractions(skillDao);
    }

    @Test
    public void updateSkillAcceptance_HappyPath_Accepted() {
        // Arrange
        int skillId = 1;
        when(skillDao.findById(skillId)).thenReturn(Optional.of(new GetSkillDto(1, "Skill", false)));

        // Act
        sut.updateSkillAcceptance(skillId, true);

        // Assert
        verify(skillDao, times(1)).findById(skillId);
        verify(skillDao, times(1)).acceptSkill(skillId);
        verifyNoMoreInteractions(skillDao);
    }

    @Test
    public void updateSkillAcceptance_HappyPath_Declined() {
        // Arrange
        int skillId = 1;
        when(skillDao.findById(skillId)).thenReturn(Optional.of(new GetSkillDto(1, "Skill", true)));

        // Act
        sut.updateSkillAcceptance(skillId, false);

        // Assert
        verify(skillDao, times(1)).findById(skillId);
        verify(skillDao, times(1)).deleteSkillFromAllTasks(skillId);
        verify(skillDao, times(1)).deleteSkillFromAllStudents(skillId);
        verify(skillDao, times(1)).deleteSkill(skillId);
        verifyNoMoreInteractions(skillDao);
    }

    @Test
    public void updateSkillAcceptance_InvalidSkillId() {
        // Arrange
        int skillId = 0;

        // Act & Assert
        assertThrows(InvalidDataException.class, () -> sut.updateSkillAcceptance(skillId, true), "Ongeldige skillId");
        verifyNoInteractions(skillDao);
    }

    @Test
    public void updateSkillAcceptance_SkillNotFound() {
        // Arrange
        int skillId = 1;
        when(skillDao.findById(skillId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> sut.updateSkillAcceptance(skillId, true), "De geselecteerde skill is niet gevonden");
        verify(skillDao, times(1)).findById(skillId);
        verifyNoMoreInteractions(skillDao);
    }

    @Test
    public void test_getBatchedMails_valid() {
        // Arrange
        Timestamp d = new Timestamp(142192930);
        ArgumentCaptor<Timestamp> timestampArgumentCaptor = ArgumentCaptor.forClass(Timestamp.class);

        when(cronJobDao.getPreviousRunDate()).thenReturn(d);
        when(skillDao.getAllSkillsCreatedAfter(timestampArgumentCaptor.capture())).thenReturn(List.of(new GetSkillDto(1, "SKILL NAME", true)));

        when(studentService.getAll()).thenReturn(List.of(new StudentRepositoryDto(1, "id", "name", "description", new FileDto(Optional.empty()), new FileDto(Optional.empty()), "email"), new StudentRepositoryDto(2, "id", "name2", "description", new FileDto(Optional.empty()), new FileDto(Optional.empty()), "email"), new StudentRepositoryDto(3, "id", "name3", "description", new FileDto(Optional.empty()), new FileDto(Optional.empty()), "email")));

        // Act
        Map<Integer, EmailPart> parts = sut.getBatchedMails();

        // Assert

        assertEquals(3, parts.size());

        EmailPart part = parts.get(1);
        assertNotNull(part);
        assertTrue(part.toString().contains("Nieuwe skills die zijn toegevoegd:"));
        assertTrue(part.toString().contains("SKILL NAME"));

        EmailPart part2 = parts.get(2);
        assertNotNull(part2);

        EmailPart part3 = parts.get(3);
        assertNotNull(part3);

        assertEquals(d, timestampArgumentCaptor.getValue());
    }

    @Test
    public void test_getBatchedMails_empty() {
        // Arrange
        Timestamp d = new Timestamp(142192930);
        ArgumentCaptor<Timestamp> timestampArgumentCaptor = ArgumentCaptor.forClass(Timestamp.class);

        when(cronJobDao.getPreviousRunDate()).thenReturn(d);
        when(skillDao.getAllSkillsCreatedAfter(timestampArgumentCaptor.capture())).thenReturn(Collections.emptyList());

        // Act
        Map<Integer, EmailPart> parts = sut.getBatchedMails();

        // Assert

        assertEquals(0, parts.size());

        assertEquals(d, timestampArgumentCaptor.getValue());
    }
}