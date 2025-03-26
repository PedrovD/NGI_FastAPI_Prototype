package com.han.pwac.pinguins.backend.controller;

import com.han.pwac.pinguins.backend.annotations.OAuth2;
import com.han.pwac.pinguins.backend.annotations.Sanitation;
import com.han.pwac.pinguins.backend.domain.DTO.BusinessProjectsWithTasksAndSkillsDto;
import com.han.pwac.pinguins.backend.domain.DTO.CreateProjectDto;
import com.han.pwac.pinguins.backend.domain.DTO.FileDto;
import com.han.pwac.pinguins.backend.domain.DTO.GetProjectDto;
import com.han.pwac.pinguins.backend.domain.UserInfo;
import com.han.pwac.pinguins.backend.exceptions.InvalidDataException;
import com.han.pwac.pinguins.backend.services.UserTokenService;
import com.han.pwac.pinguins.backend.services.contract.IFileService;
import com.han.pwac.pinguins.backend.services.contract.IProjectService;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/projects")
public class ProjectController {
    private final IProjectService projectService;
    private final IFileService fileService;
    private final UserTokenService tokenService;

    public ProjectController(IProjectService getService, IFileService fileService, UserTokenService tokenService) {
        this.projectService = getService;
        this.fileService = fileService;
        this.tokenService = tokenService;
    }

    @GetMapping
    public Collection<GetProjectDto> getAllProjects(@RequestParam(required = false) Optional<Integer> businessId) {
        if (businessId.isEmpty()) {
            return projectService.getAllProjectsWithSkills();
        }
        return projectService.getAllByBusinessId(businessId.get());
    }

    @OAuth2
    @Sanitation
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public int createProject(@Length(min = 1, max = GetProjectDto.TITLE_LENGTH) @RequestParam("title") String title,
                             @RequestParam("description") String description,
                             @RequestParam("image") MultipartFile image,
                             UserInfo userInfo) {
        // user id should always be found as the user must be logged in
        int tempUserId = tokenService.getVerificationByProviderId(Optional.of(userInfo.id())).getUserId().get();

        CreateProjectDto project = new CreateProjectDto(title, description);
        String imagePath = fileService.uploadFile(image, new MimeType("image", "*"));

        project.setImagePath(new FileDto(Optional.ofNullable(imagePath)));
        return projectService.createProject(project, tempUserId);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<GetProjectDto> getProject(@PathVariable int projectId) {
        return ResponseEntity.ok(projectService.getProject(projectId));
    }

    @GetMapping("/all")
    public ResponseEntity<Collection<BusinessProjectsWithTasksAndSkillsDto>> getAllBusinessesWithProjectsAndTasks() {
        Collection<BusinessProjectsWithTasksAndSkillsDto> businesses = projectService.getAllBusinessesWithProjectsAndTasks();
        if (businesses.isEmpty()) {
            throw new InvalidDataException("The BusinessProjectsWithTasksAndSkills data could not be retrieved");
        }
        return ResponseEntity.ok(businesses);
    }
}
