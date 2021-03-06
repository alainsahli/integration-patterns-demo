/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
