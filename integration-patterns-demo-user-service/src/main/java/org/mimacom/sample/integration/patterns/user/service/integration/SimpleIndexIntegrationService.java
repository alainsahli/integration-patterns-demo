package org.mimacom.sample.integration.patterns.user.service.integration;

import org.mimacom.sample.integration.patterns.user.service.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.lang.invoke.MethodHandles;
import java.util.List;

import static java.util.Arrays.asList;

public class SimpleIndexIntegrationService implements SearchServiceIntegration {

  private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final String searchServiceUrl;
  private final RestTemplate restTemplate;

  public SimpleIndexIntegrationService(String searchServiceUrl) {
    this.searchServiceUrl = searchServiceUrl;
    this.restTemplate = new RestTemplate();
  }

  @Override
  public void indexUser(User user) {
    LOG.info("going to send request to index user '{}' '{}'", user.getFirstName(), user.getLastName());

    ResponseEntity<String> response = this.restTemplate.postForEntity(this.searchServiceUrl + "/index", user, String.class);
    LOG.info("user '{}' '{}' was indexed and response status code was '{}'", user.getFirstName(), user.getLastName(), response.getStatusCode());
  }

  @Override
  public void indexUserSlow(User user, int waitTime) {
    LOG.info("[SLOW!] going to send request to index user '{}' '{}'", user.getFirstName(), user.getLastName());

    ResponseEntity<String> response = this.restTemplate.postForEntity(this.searchServiceUrl + "/index?waitTime=" + waitTime, user, String.class);
    LOG.info("[SLOW!] user '{}' '{}' was indexed and response status code was '{}'", user.getFirstName(), user.getLastName(), response.getStatusCode());
  }

  @Override
  public List<User> searchUserByFirstName(String firstName) {
    ResponseEntity<User[]> response = this.restTemplate.getForEntity(this.searchServiceUrl + "/search-by-firstname?firstName=" + firstName, User[].class);

    return asList(response.getBody());
  }

}
