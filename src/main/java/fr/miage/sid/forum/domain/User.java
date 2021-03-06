package fr.miage.sid.forum.domain;


import java.io.Serializable;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity
// We specify table name because we can't have an User table in psql
@Table(name = "Users")
public class User extends Auditable implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String oauthId;

  private String email;

  private String username;

  private String password;

  private String firstname;

  private String lastname;

  private String picture = "https://i.imgur.com/yfGGq35.png";

  private UserOrigin origin;

  @ManyToMany
  private Set<Role> roles;

  private boolean enabled = true;
}
