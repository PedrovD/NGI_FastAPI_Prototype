package com.han.pwac.pinguins.backend.services;

import com.han.pwac.pinguins.backend.domain.DTO.*;
import com.han.pwac.pinguins.backend.exceptions.NotFoundException;
import com.han.pwac.pinguins.backend.exceptions.ProjectInvalidBodyException;
import com.han.pwac.pinguins.backend.repository.RegistrationsDao;
import com.han.pwac.pinguins.backend.repository.SupervisorDao;
import com.han.pwac.pinguins.backend.repository.TaskDao;
import com.han.pwac.pinguins.backend.repository.contract.IBusinessDao;
import com.han.pwac.pinguins.backend.repository.contract.IProjectDao;
import com.han.pwac.pinguins.backend.repository.contract.ISkillDao;
import com.han.pwac.pinguins.backend.services.contract.IProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProjectService extends BaseService<ProjectDto> implements IProjectService {
    private final SupervisorDao supervisorDAO;
    private final TaskDao taskDAO;
    private final RegistrationsDao registrationsDao;

    private final IProjectDao projectDAO;
    private final IBusinessDao businessDAO;
    private final ISkillDao skillDAO;

    @Autowired
    public ProjectService(IProjectDao projectDAO, IBusinessDao businessDAO, ISkillDao skillDAO, SupervisorDao supervisorDAO, TaskDao taskDAO, RegistrationsDao registrationsDao) {
        super(projectDAO);
        this.projectDAO = projectDAO;
        this.businessDAO = businessDAO;
        this.skillDAO = skillDAO;
        this.supervisorDAO = supervisorDAO;
        this.taskDAO = taskDAO;
        this.registrationsDao = registrationsDao;
    }

    public List<BusinessProjectsWithTasksAndSkillsDto> getAllBusinessesWithProjectsAndTasks() {
        Collection<BusinessDto> allBusinesses = businessDAO.getAll();
        Collection<RegistrationDto> registrations = registrationsDao.getAll();

        List<BusinessProjectsWithTasksAndSkillsDto> businessDTOs = allBusinesses.parallelStream().map(business -> {
            List<ProjectDto> allProjects = projectDAO.getByBusinessId(business.businessId());

            List<ProjectWithTasksAndSkillsDto> projectDTOs = allProjects.stream()
                    .map(project -> getProjectWithDetails(project, registrations))
                    .toList();

            List<GetSkillDto> topSkills = skillDAO.getTopSkillsForBusiness(business.businessId(), 5);

            return new BusinessProjectsWithTasksAndSkillsDto(business, projectDTOs, topSkills);
        }).toList();

        return businessDTOs;
    }

    private Collection<GetProjectDto> mapProjects(Collection<ProjectDto> projectDtos) {
        return projectDtos.stream().map(projectDto -> new GetProjectDto(
                projectDto.id(),
                projectDto.title(),
                projectDto.description(),
                skillDAO.getTopByProjectId(projectDto.id()).stream().limit(5).collect(Collectors.toList()),
                businessDAO.getByProjectId(projectDto.id()).get(), // project should always have a business
                projectDto.photo())).collect(Collectors.toList());
    }

    @Override
    public Collection<GetProjectDto> getAllProjectsWithSkills() {
        return mapProjects(projectDAO.getAll());
    }

    @Override
    public Collection<GetProjectDto> getAllByBusinessId(int businessId) {
        return mapProjects(projectDAO.getAllByBusinessId(businessId));
    }

    public int createProject(CreateProjectDto project, int supervisorId) {
        if (project.getTitle().isEmpty() || project.getTitle().isEmpty()) {
            throw new ProjectInvalidBodyException("Titel mag niet leeg zijn");
        }

        if (project.getTitle().get().length() > 50) {
            throw new ProjectInvalidBodyException("Titel mag niet langer zijn dan 50 karakters");
        }

        if (project.getDescription().isEmpty() || project.getDescription().isEmpty()) {
            throw new ProjectInvalidBodyException("Beschrijving mag niet leeg zijn");
        }

        if (IValidate.stripMarkdownFromString(project.getDescription().get()).length() > 4000) {
            throw new ProjectInvalidBodyException("Beschrijving mag niet langer zijn dan 4000 karakters");
        }

        Optional<Boolean> isProjectNameTaken = projectDAO.checkIfProjectNameIsTaken(project.getTitle().get(), supervisorId);
        if (isProjectNameTaken.isPresent() && isProjectNameTaken.get()) {
            throw new ProjectInvalidBodyException("Projectnaam is al in gebruik");
        }

        return projectDAO.storeProject(project, supervisorId);
    }

    public GetProjectDto getProject(int projectId) {
        Optional<ProjectDto> project = projectDAO.findById(projectId);
        if (project.isEmpty()) {
            throw new NotFoundException("Project id niet gevonden");
        }

        Optional<BusinessDto> business = businessDAO.getByProjectId(projectId);
        assert business.isPresent() : "Business should be present here as project id was found and every project should have a business";

        Collection<GetSkillDto> skills = skillDAO.getTopByProjectId(projectId).stream().limit(5).toList();

        return new GetProjectDto(
                project.get().id(),
                project.get().title(),
                project.get().description(),
                skills,
                business.get(),
                project.get().photo()
        );
    }

    public TaskWithSkills getTaskWithSkills(int taskId) {
        return getTaskWithSkills(taskId, registrationsDao.getAll());
    }

    public TaskWithSkills getTaskWithSkills(int taskId, Collection<RegistrationDto> allRegistrations) {
        Optional<TaskDto> task = taskDAO.findById(taskId);
        if (task.isEmpty()) {
            throw new NotFoundException("Task not found with id: " + taskId);
        }

        return getTaskWithSkills(task.get(), allRegistrations);
    }

    public TaskWithSkills getTaskWithSkills(TaskDto task, Collection<RegistrationDto> allRegistrations) {
        List<GetSkillDto> skills = skillDAO.getAllForTask(task.getTaskId());

        int totalRegistered = 0;
        int totalAccepted = 0;

        for (RegistrationDto registration : allRegistrations) {
            if (registration.taskId() == task.getTaskId()) {
                if (registration.accepted().isEmpty()) {
                    totalRegistered++;
                } else if (registration.accepted().orElse(false)) {
                    totalAccepted++;
                }
            }
        }

        TaskWithSkills taskDTO = new TaskWithSkills(task, totalAccepted, totalRegistered, skills);

        return taskDTO;
    }

    public ProjectWithTasksAndSkillsDto getProjectWithDetails(int projectId) {
        return getProjectWithDetails(projectId, registrationsDao.getAll());
    }

    public ProjectWithTasksAndSkillsDto getProjectWithDetails(int projectId, Collection<RegistrationDto> registrations) {
        Optional<ProjectDto> projectDto = projectDAO.findById(projectId);
        if (projectDto.isEmpty()) {
            throw new NotFoundException("Project is niet gevonden");
        }
        return getProjectWithDetails(projectDto.get(), registrations);
    }

    public ProjectWithTasksAndSkillsDto getProjectWithDetails(ProjectDto project, Collection<RegistrationDto> registrations) {
        List<TaskDto> tasks = taskDAO.getByProjectId(project.id());
        List<TaskWithSkills> taskWithSkills = tasks.stream()
                .map(task -> getTaskWithSkills(task, registrations))
                .toList();

        ProjectWithTasksAndSkillsDto projectDTO = new ProjectWithTasksAndSkillsDto();
        projectDTO.setProjectId(project.id());
        projectDTO.setTitle(project.title());
        projectDTO.setDescription(project.description());
        projectDTO.setImage(project.photo());
        projectDTO.setTasks(taskWithSkills);

        return projectDTO;
    }
}
