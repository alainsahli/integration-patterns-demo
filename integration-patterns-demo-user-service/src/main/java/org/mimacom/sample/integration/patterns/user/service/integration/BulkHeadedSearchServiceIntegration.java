package org.mimacom.sample.integration.patterns.user.service.integration;

import org.mimacom.sample.integration.patterns.user.service.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Arrays.asList;

@Service
public class BulkHeadedSearchServiceIntegration {

  private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final String searchServiceUrl;
  private final AsyncRestTemplate asyncSearchRestTemplate;
  private final AsyncRestTemplate asyncIndexRestTemplate;

  @Autowired
  public BulkHeadedSearchServiceIntegration(@Value("${search-service-url}") String searchServiceUrl) {
    this.searchServiceUrl = searchServiceUrl;
    this.asyncSearchRestTemplate = initializeRestTemplate("SearchUser-");
    this.asyncIndexRestTemplate = initializeRestTemplate("IndexUser-");
  }

  public void indexUser(User user, Consumer<Void> successConsumer, Consumer<Throwable> failureConsumer) {
    LOG.info("going to send request to index user '{}' '{}'", user.getFirstName(), user.getLastName());

    HttpEntity<User> requestEntity = new HttpEntity<>(user);
    ListenableFuture<ResponseEntity<String>> listenableFuture = this.asyncIndexRestTemplate.postForEntity(this.searchServiceUrl + "/index", requestEntity, String.class);

    listenableFuture.addCallback(result -> {
      LOG.info("user '{}' '{}' was indexed and response status code was '{}'", user.getFirstName(), user.getLastName(), result.getStatusCode());
      successConsumer.accept(null);
    }, failureConsumer::accept);

  }

  public void indexUserSlow(User user, int waitTime, Consumer<Void> successConsumer, Consumer<Throwable> failureConsumer) {
    LOG.info("[SLOW!] going to send request to index user '{}' '{}'", user.getFirstName(), user.getLastName());

    HttpEntity<User> requestEntity = new HttpEntity<>(user);
    ListenableFuture<ResponseEntity<String>> listenableFuture = this.asyncIndexRestTemplate.postForEntity(this.searchServiceUrl + "/index?waitTime={waitTime}", requestEntity, String.class, waitTime);

    listenableFuture.addCallback(result -> {
      LOG.info("[SLOW!] user '{}' '{}' was indexed and response status code was '{}'", user.getFirstName(), user.getLastName(), result.getStatusCode());
      successConsumer.accept(null);
    }, failureConsumer::accept);
  }

  public void searchUserByFirstName(String firstName, Consumer<List<User>> successConsumer, Consumer<Throwable> failureConsumer) {
    ListenableFuture<ResponseEntity<User[]>> listenableFuture = this.asyncSearchRestTemplate.getForEntity(this.searchServiceUrl + "/search-by-firstname?firstName={firstName}", User[].class, firstName);

    listenableFuture.addCallback(result -> {
      successConsumer.accept(asList(result.getBody()));
    }, failureConsumer::accept);

  }

  private static AsyncRestTemplate initializeRestTemplate(String threadNamePrefix) {
    ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
    threadPoolTaskExecutor.setCorePoolSize(2);
    threadPoolTaskExecutor.setMaxPoolSize(2);
    threadPoolTaskExecutor.setThreadNamePrefix(threadNamePrefix);
    threadPoolTaskExecutor.afterPropertiesSet();

    return new AsyncRestTemplate(threadPoolTaskExecutor);
  }

}
