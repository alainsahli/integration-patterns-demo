package org.mimacom.sample.integration.patterns.user.service.domain;


import static java.util.UUID.randomUUID;

public class User {

  private final String id;
  private final String firstName;
  private final String lastName;

  protected User() {
    this.id = generateId();
    this.firstName = null;
    this.lastName = null;
  }

  public String getId() {
    return id;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  private static String generateId() {
    return randomUUID().toString();
  }

}
