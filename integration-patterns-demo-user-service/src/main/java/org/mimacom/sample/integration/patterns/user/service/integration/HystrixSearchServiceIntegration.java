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

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.contrib.servopublisher.HystrixServoMetricsPublisher;
import com.netflix.hystrix.strategy.HystrixPlugins;
import org.mimacom.sample.integration.patterns.user.service.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.function.Consumer;

import static com.netflix.hystrix.HystrixCommandGroupKey.Factory.asKey;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class HystrixSearchServiceIntegration {

  private final String searchServiceUrl;
  private final RestTemplate restTemplate;

  public HystrixSearchServiceIntegration(String searchServiceUrl) {
    this.searchServiceUrl = searchServiceUrl;
    this.restTemplate = new RestTemplate();
    exposeMetricsOnJmx();
  }

  private static void exposeMetricsOnJmx() {
    HystrixPlugins.getInstance().registerMetricsPublisher(
        HystrixServoMetricsPublisher.getInstance());
  }

  public void indexUser(User user, Consumer<Void> successConsumer, Consumer<Throwable> failureConsumer) {
    IndexUserHystrixCommand hystrixCommand = new IndexUserHystrixCommand(user, this.restTemplate, this.searchServiceUrl + "/index");
    hystrixCommand.observe().subscribe((ignored) -> {
      successConsumer.accept(null);
    }, failureConsumer::accept);
  }

  public void indexUserSlow(User user, int waitTime, Consumer<Void> successConsumer, Consumer<Throwable> failureConsumer) {
    IndexUserHystrixCommand hystrixCommand = new IndexUserHystrixCommand(user, this.restTemplate, this.searchServiceUrl + "/index?waitTime=" + waitTime);
    hystrixCommand.observe().subscribe((ignored) -> {
      successConsumer.accept(null);
    }, failureConsumer::accept);
  }

  public void searchUserByFirstName(String firstName, Consumer<List<User>> successConsumer, Consumer<Throwable> failureConsumer) {
    SearchUserByFirstNameHystrixCommand hystrixCommand = new SearchUserByFirstNameHystrixCommand(this.restTemplate, this.searchServiceUrl, firstName);
    hystrixCommand.observe().subscribe(successConsumer::accept, failureConsumer::accept);
  }


  private static class IndexUserHystrixCommand extends HystrixCommand<Void> {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final RestTemplate restTemplate;
    private final User user;
    private final String url;

    IndexUserHystrixCommand(User user, RestTemplate restTemplate, String url) {
      super(Setter.withGroupKey(asKey("IndexUserGroup"))
          .andCommandPropertiesDefaults(
              HystrixCommandProperties.Setter().withExecutionTimeoutEnabled(false)
                  .withCircuitBreakerRequestVolumeThreshold(2)
          ));

      this.restTemplate = restTemplate;
      this.url = url;
      this.user = user;
    }

    @Override
    protected Void run() throws Exception {
      LOG.info("going to send request to index user '{}' '{}'", this.user.getFirstName(), this.user.getLastName());
      this.restTemplate.postForEntity(this.url, this.user, String.class);
      LOG.info("user '{}' '{}' was indexed", this.user.getFirstName(), this.user.getLastName());

      return null;
    }

    @Override
    protected Void getFallback() {
      LOG.info("index service command fallback is called...");
      return null;
    }
  }


  private static class SearchUserByFirstNameHystrixCommand extends HystrixCommand<List<User>> {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final RestTemplate restTemplate;
    private final String searchServiceUrl;
    private final String firstName;

    private SearchUserByFirstNameHystrixCommand(RestTemplate restTemplate, String searchServiceUrl, String firstName) {
      super(Setter.withGroupKey(asKey("SearchUserByFirstNameGroup"))
          .andCommandPropertiesDefaults(
              HystrixCommandProperties.Setter().withCircuitBreakerRequestVolumeThreshold(2)
          ));

      this.restTemplate = restTemplate;
      this.searchServiceUrl = searchServiceUrl;
      this.firstName = firstName;
    }

    @Override
    protected List<User> run() throws Exception {
      LOG.info("sending request to search service");
      ResponseEntity<User[]> response = this.restTemplate.getForEntity(this.searchServiceUrl + "/search-by-firstname?firstName={firstName}", User[].class, this.firstName);
      LOG.info("search service returned a result");

      return asList(response.getBody());
    }

    @Override
    protected List<User> getFallback() {
      LOG.info("search service command fallback is called...");
      return singletonList(new User("foo", "bar"));
    }
  }

}
