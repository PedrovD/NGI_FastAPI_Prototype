package com.han.pwac.pinguins.backend.controller;

import com.han.pwac.pinguins.backend.annotations.OAuth2;
import com.han.pwac.pinguins.backend.annotations.Sanitation;
import com.han.pwac.pinguins.backend.domain.DTO.TaskDto;
import com.han.pwac.pinguins.backend.domain.DTO.TaskWithSkills;
import com.han.pwac.pinguins.backend.domain.DTO.VerificationDto;
import com.han.pwac.pinguins.backend.domain.UserInfo;
import com.han.pwac.pinguins.backend.services.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;
    private final UserTokenService userTokenService;
    private final ProjectService projectService;
    private final SkillService skillService;

    @Autowired
    public TaskController(TaskService taskService, UserTokenService userTokenService, ProjectService projectService, SkillService skillService) {
        this.taskService = taskService;
        this.userTokenService = userTokenService;
        this.projectService = projectService;
        this.skillService = skillService;
    }

    @GetMapping("/{projectId}")
    public List<TaskWithSkills> getTasks(@PathVariable int projectId) {
        return taskService.getTasksByProjectId(projectId);
    }

    @OAuth2
    @Sanitation
    @PostMapping("/{projectId}")
    public ResponseEntity<?> createTask(@PathVariable int projectId,
                                        @Valid @RequestBody TaskDto task,
                                        UserInfo userInfo)
            throws AuthenticationException {
        if (!userTokenService.checkIfProviderIdMatchesBusinessId(userInfo.id(), projectId)) {
            throw new AuthenticationException("U bent niet gemachtigd");
        }
        taskService.addTaskToProject(projectId, task);
        return new ResponseEntity<>("Taak toegevoegd.", HttpStatus.CREATED);
    }

    @OAuth2
    @PutMapping("/{taskId}/skills")
    public ResponseEntity<?> addSkillToTask(@PathVariable int taskId,
                                            @RequestBody Collection<Integer> newSkills,
                                            UserInfo userInfo) throws AuthenticationException {
        VerificationDto verification = userTokenService.getVerificationByProviderId(Optional.of(userInfo.id()));

        if (projectService.getProject(taskService.getProjectIdFromTasks(taskId)).business().businessId() != verification.getBusinessId().get()) {
            throw new AuthenticationException("U bent niet de juiste Opdrachtgever.");
        }
        skillService.addSkillToTask(newSkills, taskId);
        return ResponseEntity.ok().build();
    }
}
