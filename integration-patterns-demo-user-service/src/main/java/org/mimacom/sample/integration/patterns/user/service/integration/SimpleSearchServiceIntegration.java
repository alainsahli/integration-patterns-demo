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

package org.mimacom.sample.integration.patterns.user.service.integration;

import org.mimacom.sample.integration.patterns.user.service.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.lang.invoke.MethodHandles;
import java.util.List;

import static java.util.Arrays.asList;

public class SimpleSearchServiceIntegration {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final String searchServiceUrl;
  private final RestTemplate restTemplate;

  public SimpleSearchServiceIntegration(String searchServiceUrl) {
    this.searchServiceUrl = searchServiceUrl;
    this.restTemplate = new RestTemplate();
  }

  public void indexUser(User user) {
    LOG.info("going to send request to index user '{}' '{}'", user.getFirstName(), user.getLastName());

    ResponseEntity<String> response = this.restTemplate.postForEntity(this.searchServiceUrl + "/index", user, String.class);
    LOG.info("user '{}' '{}' was indexed and response status code was '{}'", user.getFirstName(), user.getLastName(), response.getStatusCode());
  }

  public void indexUserSlow(User user, int waitTime) {
    LOG.info("[SLOW!] going to send request to index user '{}' '{}'", user.getFirstName(), user.getLastName());

    ResponseEntity<String> response = this.restTemplate.postForEntity(this.searchServiceUrl + "/index?waitTime={waitTime}", user, String.class, waitTime);
    LOG.info("[SLOW!] user '{}' '{}' was indexed and response status code was '{}'", user.getFirstName(), user.getLastName(), response.getStatusCode());
  }

  public List<User> searchUserByFirstName(String firstName) {
    ResponseEntity<User[]> response = this.restTemplate.getForEntity(this.searchServiceUrl + "/search-by-firstname?firstName={firstName}", User[].class, firstName);

    return asList(response.getBody());
  }

}
