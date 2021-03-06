package fr.miage.sid.forum.controller;

import fr.miage.sid.forum.config.security.CurrentUser;
import fr.miage.sid.forum.config.security.MyPrincipal;
import fr.miage.sid.forum.domain.Post;
import fr.miage.sid.forum.domain.Topic;
import fr.miage.sid.forum.domain.User;
import fr.miage.sid.forum.service.PostService;
import fr.miage.sid.forum.service.TopicService;
import fr.miage.sid.forum.service.UserService;
import java.util.Set;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.servlet.ModelAndView;


@Controller
@Slf4j
public class TopicController {

  private final TopicService topicService;
  private final PostService postService;
  private final UserService userService;

  @Autowired
  public TopicController(TopicService topicService,
      PostService postService, UserService userService) {
    this.topicService = topicService;
    this.postService = postService;
    this.userService = userService;
  }

  /**
   * Returns form to create a topic
   */
  @GetMapping("project/{projectId}/topic/create")
  @PreAuthorize("isAuthenticated() and @permissionService.canWriteProject(#projectId)")
  public ModelAndView getTopicCreateForm(Topic topic, @PathVariable("projectId") Long projectId) {
    ModelAndView modelAndView = new ModelAndView("topic/create");
    modelAndView.addObject(topic);
    modelAndView.addObject("projectId", projectId);
    return modelAndView;
  }

  /**
   * Validate and create a topic with a first post
   */
  @PostMapping("project/{projectId}/topic")
  @PreAuthorize("isAuthenticated() and @permissionService.canWriteProject(#projectId)")
  public ModelAndView createTopic(
      @Valid Topic topic,
      BindingResult result,
      String postContent,
      @CurrentUser MyPrincipal principal,
      @PathVariable("projectId") Long projectId) {
    ModelAndView modelAndView = new ModelAndView();

    if (result.hasErrors() || postContent.equals("")) {
      modelAndView.setViewName("topic/create");
      modelAndView.addObject("errorPostContent", postContent);
      return modelAndView.addObject("projectId", projectId);
    }

    topic.addFollower(userService.getOne(principal.getId()));
    Topic createdTopic = topicService.save(topic, projectId);

    postService.save(new Post().setContent(postContent), createdTopic.getId());
    modelAndView.setViewName("redirect:/project/" + projectId);
    return modelAndView;
  }

  /**
   * return the page of a topic with all posts
   */
  @GetMapping("/topic/{topicId}")
  @PreAuthorize("@permissionService.canReadTopic(#topicId)")
  public ModelAndView showTopic(@PathVariable("topicId") Long topicId,
      @CurrentUser MyPrincipal principal) {
    ModelAndView modelAndView = new ModelAndView();

    Topic topic = topicService.getOne(topicId);
    modelAndView.setViewName("topic/show");
    modelAndView.addObject("topic", topic);
    modelAndView.addObject("posts", postService.getAllByTopic(topic));

    if (principal != null) {
      boolean following = topicService.isFollowing(principal.getId(), topic);
      modelAndView.addObject("userIsAdmin", principal.isAdmin());
      modelAndView.addObject("isFollowing", following);
    }

    return modelAndView;
  }

  /**
   * Returns form to edit a topic
   */
  @GetMapping("topic/{topicId}/edit")
  @PreAuthorize("isAuthenticated()")
  public ModelAndView editTopic(@PathVariable("topicId") Long topicId,
      @CurrentUser MyPrincipal principal) {
    ModelAndView modelAndView = new ModelAndView();
    Topic topic = topicService.getOne(topicId);

    if (isNotTopicCreatorOrAdmin(principal, topic)) {
      return ViewUtils
          .setErrorView(modelAndView, HttpStatus.FORBIDDEN, "This is not your topic ! :)");
    }

    Set<User> readers = userService.getAllTopicReaders(topicId);
    Set<User> writers = userService.getAllTopicWriters(topicId);

    modelAndView.setViewName("topic/edit");
    modelAndView.addObject("topic", topic);
    modelAndView.addObject("users", userService.getAll());
    modelAndView.addObject("usersReader", readers);
    modelAndView.addObject("usersWriter", writers);

    return modelAndView;
  }


  /**
   * Put handler to edit a project's name
   */
  @PutMapping("topic/{topicId}")
  @PreAuthorize("isAuthenticated()")
  public ModelAndView editTopicName(@PathVariable("topicId") Long topicId, String title,
      boolean anonymousCanAccess, @CurrentUser MyPrincipal principal) {
    ModelAndView modelAndView = new ModelAndView();

    Topic topic = topicService.getOne(topicId);

    if (isNotTopicCreatorOrAdmin(principal, topic)) {
      return ViewUtils
          .setErrorView(modelAndView, HttpStatus.FORBIDDEN, "This is not your topic ! :)");
    }

    topic.setTitle(title);
    topic.setAnonymousCanAccess(anonymousCanAccess);
    Topic saved = topicService.save(topic);

    modelAndView.addObject("topic", saved);
    modelAndView.addObject("users", userService.getAll());
    modelAndView.setViewName("redirect:/topic/" + topicId);
    return modelAndView;
  }

  private boolean isNotTopicCreatorOrAdmin(@CurrentUser MyPrincipal principal, Topic topic) {
    return !topicService.isCreator(principal.getId(), topic) && !principal.isAdmin();
  }
}
