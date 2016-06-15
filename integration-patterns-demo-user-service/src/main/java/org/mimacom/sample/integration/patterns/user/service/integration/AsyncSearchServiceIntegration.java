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
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Arrays.asList;

public class AsyncSearchServiceIntegration {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final String searchServiceUrl;
  private final AsyncRestTemplate asyncRestTemplate;

  public AsyncSearchServiceIntegration(String searchServiceUrl) {
    this.searchServiceUrl = searchServiceUrl;
    this.asyncRestTemplate = initializeRestTemplate();
  }

  public void indexUser(User user, Consumer<Void> successConsumer, Consumer<Throwable> failureConsumer) {
    LOG.info("going to send request to index user '{}' '{}'", user.getFirstName(), user.getLastName());

    HttpEntity<User> requestEntity = new HttpEntity<>(user);
    ListenableFuture<ResponseEntity<String>> listenableFuture = this.asyncRestTemplate.postForEntity(this.searchServiceUrl + "/index", requestEntity, String.class);

    listenableFuture.addCallback(result -> {
      LOG.info("user '{}' '{}' was indexed and response status code was '{}'", user.getFirstName(), user.getLastName(), result.getStatusCode());
      successConsumer.accept(null);
    }, failureConsumer::accept);

  }

  public void indexUserSlow(User user, int waitTime, Consumer<Void> successConsumer, Consumer<Throwable> failureConsumer) {
    LOG.info("[SLOW!] going to send request to index user '{}' '{}'", user.getFirstName(), user.getLastName());

    HttpEntity<User> requestEntity = new HttpEntity<>(user);
    ListenableFuture<ResponseEntity<String>> listenableFuture = this.asyncRestTemplate.postForEntity(this.searchServiceUrl + "/index?waitTime={waitTime}", requestEntity, String.class, waitTime);

    listenableFuture.addCallback(result -> {
      LOG.info("[SLOW!] user '{}' '{}' was indexed and response status code was '{}'", user.getFirstName(), user.getLastName(), result.getStatusCode());
      successConsumer.accept(null);
    }, failureConsumer::accept);
  }

  public void searchUserByFirstName(String firstName, Consumer<List<User>> successConsumer, Consumer<Throwable> failureConsumer) {
    ListenableFuture<ResponseEntity<User[]>> listenableFuture = this.asyncRestTemplate.getForEntity(this.searchServiceUrl + "/search-by-firstname?firstName={firstName}", User[].class, firstName);

    listenableFuture.addCallback(result -> successConsumer.accept(asList(result.getBody())), failureConsumer::accept);

  }

  private static AsyncRestTemplate initializeRestTemplate() {
    ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
    threadPoolTaskExecutor.setCorePoolSize(2);
    threadPoolTaskExecutor.setMaxPoolSize(2);
    threadPoolTaskExecutor.setThreadNamePrefix("SearchServiceIntegration-");
    threadPoolTaskExecutor.afterPropertiesSet();

    return new AsyncRestTemplate(threadPoolTaskExecutor);
  }

}
