package org.example.api.controllers;

import org.example.api.controllers.helpers.ControllerHelper;
import org.example.api.dto.AckDto;
import org.example.api.dto.ProjectDto;
import org.example.api.exceptions.BadRequestException;
import org.example.api.factories.ProjectDtoFactory;
import org.example.store.entities.ProjectEntity;
import org.example.store.repositories.ProjectRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@RestController
public class ProjectController {
    ProjectRepository projectRepository;
    ProjectDtoFactory projectDtoFactory;
    ControllerHelper controllerHelper;

    public static final String FETCH_PROJECTS = "/api/projects";
    public static final String CREATE_OR_UPDATE_PROJECT = "/api/projects";
    public static final String DELETE_PROJECT = "/api/projects/{project_id}";

    @GetMapping(FETCH_PROJECTS)
    public List<ProjectDto> fetchProjects(@RequestParam(value = "prefix_name", required = false) Optional<String> optionalPrefixName) {
        optionalPrefixName = optionalPrefixName.filter((prefixName) -> !prefixName.trim().isEmpty());
        Stream<ProjectEntity> projectStream = optionalPrefixName
                .map(projectRepository::streamAllByNameStartsWithIgnoreCase)
                .orElseGet(projectRepository::streamAllBy);

        return projectStream
                .map(projectDtoFactory::makeProjectDto)
                .collect(Collectors.toList());
    }

    @PutMapping(CREATE_OR_UPDATE_PROJECT)
    public ProjectDto createOrUpdateProject(
            @RequestParam(value = "project_id", required = false) Optional<Long> optionalProjectId,
            @RequestParam(value = "project_name", required = false) Optional<String> optionalProjectName) {

        optionalProjectName.filter((projectName) -> !projectName.trim().isEmpty());
        boolean isCreate = !optionalProjectId.isPresent();

        if (isCreate) {
            throw new BadRequestException("Project name can't be empty");
        }

        final ProjectEntity project = optionalProjectId
                .map(controllerHelper::getProjectOrThrowException)
                .orElseGet(() -> ProjectEntity.builder().build());

        optionalProjectName
                .ifPresent(projectName -> {
                    projectRepository
                            .findByName(projectName)
                            .filter(anotherProject -> !Objects.equals(anotherProject.getId(), project.getId()));


                });

        return null;
    }


//    @DeleteMapping(DELETE_PROJECT)
//    public AckDto ProjectDto deleteProject(@PathVariable("project_id") Long projectId) {
//
//    }


}
