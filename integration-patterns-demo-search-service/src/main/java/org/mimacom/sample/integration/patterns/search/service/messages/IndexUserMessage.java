package org.mimacom.sample.integration.patterns.search.service.messages;

import java.io.Serializable;

public class IndexUserMessage implements Serializable {

  private final String id;
  private final String firstName;
  private final String lastName;
  private Integer waitTime;

  public IndexUserMessage(String id, String firstName, String lastName) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
  }

  public String getFirstName() {
    return this.firstName;
  }

  public String getLastName() {
    return this.lastName;
  }

  public Integer getWaitTime() {
    return this.waitTime;
  }

  public void setWaitTime(Integer waitTime) {
    this.waitTime = waitTime;
  }

  public String getId() {
    return this.id;
  }
}
