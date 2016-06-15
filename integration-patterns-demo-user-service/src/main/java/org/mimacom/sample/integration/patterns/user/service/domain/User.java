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

  public User(String firstName, String lastName) {
    this.id = generateId();
    this.firstName = firstName;
    this.lastName = lastName;
  }

  public String getId() {
    return this.id;
  }

  public String getFirstName() {
    return this.firstName;
  }

  public String getLastName() {
    return this.lastName;
  }

  private static String generateId() {
    return randomUUID().toString();
  }

}
