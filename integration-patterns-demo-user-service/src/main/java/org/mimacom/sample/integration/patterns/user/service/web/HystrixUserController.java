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

package org.mimacom.sample.integration.patterns.user.service.web;

import org.mimacom.sample.integration.patterns.user.service.domain.User;
import org.mimacom.sample.integration.patterns.user.service.integration.HystrixSearchServiceIntegration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RequestMapping("/users")
@ResponseBody
public class HystrixUserController {

  private final HystrixSearchServiceIntegration hystrixSearchServiceIntegration;
  private final ConcurrentHashMap<String, User> userRepository;

  public HystrixUserController(HystrixSearchServiceIntegration hystrixSearchServiceIntegration) {
    this.hystrixSearchServiceIntegration = hystrixSearchServiceIntegration;
    this.userRepository = new ConcurrentHashMap<>();
  }

  @ResponseStatus(CREATED)
  @RequestMapping(method = POST)
  public DeferredResult<String> createUser(@RequestBody User user, @RequestParam(required = false) Integer waitTime) {
    DeferredResult<String> deferredResult = new DeferredResult<>();

    this.userRepository.put(user.getId(), user);

    if (waitTime == null) {
      this.hystrixSearchServiceIntegration.indexUser(user, ignored -> deferredResult.setResult(user.getId()), deferredResult::setErrorResult);
    } else {
      this.hystrixSearchServiceIntegration.indexUserSlow(user, waitTime, ignored -> deferredResult.setResult(user.getId()), deferredResult::setErrorResult);
    }

    return deferredResult;
  }

  @RequestMapping(value = "/{id}", method = GET)
  public ResponseEntity<?> getUser(@PathVariable String id) {
    if (this.userRepository.containsKey(id)) {
      return new ResponseEntity<>(this.userRepository.get(id), OK);
    } else {
      return new ResponseEntity<>(format("user with id '%s' does not exist", id), NOT_FOUND);
    }
  }

  @RequestMapping(value = "/search-by-firstname")
  public DeferredResult<List<User>> searchUserByFirstName(@RequestParam String firstName) {
    DeferredResult<List<User>> deferredResult = new DeferredResult<>();
    this.hystrixSearchServiceIntegration.searchUserByFirstName(firstName, deferredResult::setResult, deferredResult::setErrorResult);

    return deferredResult;
  }

}
