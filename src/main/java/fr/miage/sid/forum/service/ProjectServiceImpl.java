package fr.miage.sid.forum.service;

import fr.miage.sid.forum.domain.Project;
import fr.miage.sid.forum.domain.ProjectRepository;
import fr.miage.sid.forum.domain.User;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;

@Service
public class ProjectServiceImpl implements ProjectService {

  private ProjectRepository projectRepository;

  @Autowired
  public ProjectServiceImpl(ProjectRepository projectRepository) {
    this.projectRepository = projectRepository;
  }

  @Override
  public Project save(Project project) {
    return projectRepository.save(project);
  }

  @Override
  public Project getOne(Long projectId) {
    return projectRepository.getOne(projectId);
  }

  @Override
  @PostFilter("@permissionService.canReadProject(filterObject)")
  public List<Project> getAllAllowed() {
    return projectRepository.findAll();
  }

  @Override
  public int countCreatedByUser(User user) {
    return projectRepository.countAllByCreatedBy(user);
  }
}
