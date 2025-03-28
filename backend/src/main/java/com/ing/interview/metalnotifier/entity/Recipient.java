package com.ing.interview.metalnotifier.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ing.interview.metalnotifier.entity.NotificationTemplate;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Recipient {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String email;

  @ManyToOne
  @JoinColumn(name = "template_id")
  @JsonIgnore
  private NotificationTemplate template;

  public Recipient() {}

  public Recipient(String email) {
    this.email = email;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public NotificationTemplate getTemplate() {
    return template;
  }

  public void setTemplate(NotificationTemplate template) {
    this.template = template;
  }
}
