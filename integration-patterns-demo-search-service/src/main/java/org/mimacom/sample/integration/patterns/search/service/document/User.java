package org.mimacom.sample.integration.patterns.search.service.document;


public class User {

  private final String id;
  private final String firstName;
  private final String lastName;

  protected User() {
    this.id = null;
    this.firstName = null;
    this.lastName = null;
  }

  public User(String id, String firstName, String lastName) {
    this.id = id;
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

}
