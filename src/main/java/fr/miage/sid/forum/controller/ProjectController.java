package fr.miage.sid.forum.controller;

import fr.miage.sid.forum.domain.Permission;
import fr.miage.sid.forum.domain.Project;
import fr.miage.sid.forum.domain.User;
import fr.miage.sid.forum.service.ProjectService;
import fr.miage.sid.forum.service.TopicService;
import fr.miage.sid.forum.service.UserService;
import java.util.HashSet;
import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ProjectController {

  private final ProjectService projectService;
  private final TopicService topicService;
  private final UserService userService;

  @Autowired
  public ProjectController(ProjectService projectService,
      TopicService topicService, UserService userService) {
    this.projectService = projectService;
    this.topicService = topicService;
    this.userService = userService;
  }

  @GetMapping("/")
  public ModelAndView getAll() {
    ModelAndView modelAndView = new ModelAndView("project/index");
    modelAndView.addObject("projects", projectService.getAll());
    return modelAndView;
  }

  @GetMapping("/project/create")
  @PreAuthorize("isAuthenticated()")
  public ModelAndView getTopicForm(Project project) {
    ModelAndView modelAndView = new ModelAndView("project/create");
    modelAndView.addObject(project);
    return modelAndView;
  }

  @PostMapping("/project")
  @PreAuthorize("isAuthenticated()")
  public ModelAndView createProject(@Valid Project project, BindingResult result) {
    ModelAndView modelAndView = new ModelAndView();

    if (result.hasErrors()) {
      modelAndView.setViewName("project/create");
      return modelAndView;
    }

    projectService.save(project);
    modelAndView.setViewName("redirect:/");

    return modelAndView;
  }

  @GetMapping("project/{projectId}")
  public ModelAndView showProject(@PathVariable("projectId") Long projectId) {
    ModelAndView modelAndView = new ModelAndView();

    Project project = projectService.getOne(projectId);

    if (project == null) {
      ViewUtils.setErrorView(modelAndView, HttpStatus.NOT_FOUND, "This project doesn't exist");
    }

    modelAndView.setViewName("project/show");
    modelAndView.addObject("project", project);
    modelAndView.addObject("topics", topicService.getAllByProject(project));
    return modelAndView;
  }

  @GetMapping("project/{projectId}/edit")
  public ModelAndView editProject(@PathVariable("projectId") String projectId) {
    ModelAndView modelAndView = new ModelAndView();

    try {
      Project project = projectService.getOne(Long.valueOf(projectId));

      HashSet<User> tmpReader = new HashSet<>();
      HashSet<User> tmpWriter = new HashSet<>();

      project.getReaders().forEach((reader) -> {
        tmpReader.add(userService.getOne(reader));
      });
      project.getWriters().forEach((writer) -> {
        tmpWriter.add(userService.getOne(writer));
      });
      System.out.println(tmpReader.size());
      System.out.println(tmpWriter.size());
      modelAndView.setViewName("project/edit");
      modelAndView.addObject("project", project);
      modelAndView.addObject("users", userService.getAll());
      modelAndView.addObject("usersReader", tmpReader);
      modelAndView.addObject("usersWriter", tmpWriter);
    } catch (NumberFormatException | EntityNotFoundException e) {
      modelAndView.setViewName("error/basic");
      modelAndView.setStatus(HttpStatus.NOT_FOUND);
      modelAndView.addObject("errorCode", "404 Not Found");
      modelAndView.addObject("message", "This project does not exist");
    }

    return modelAndView;
  }

  @PostMapping("project/{projectId}/edit")
  public ModelAndView editPostProject(@PathVariable("projectId") Long projectId) {
    ModelAndView modelAndView = new ModelAndView();

    Project project = projectService.getOne(projectId);

    modelAndView.setViewName("project/edit");
    modelAndView.addObject("project", project);
    modelAndView.addObject("users", userService.getAll());

    return modelAndView;
  }

  @GetMapping("permission/{projectId}")
  public void addPermission(@PathVariable("projectId") Long projectId,
      @RequestParam("user") Long userId, @RequestParam("permission") String permission) {
    System.out.println("fr.miage.sid.forum.controller.ProjectController.addPermission()");
    System.out.println(projectId + " " + userId + " " + permission);

    Project project = projectService.getOne(projectId);

    project.givePermissionTo(userId, Permission.valueOf(permission));
    projectService.save(project);

  }

}
