package org.mimacom.sample.integration.patterns.user.service.web;

import org.mimacom.sample.integration.patterns.user.service.domain.User;
import org.mimacom.sample.integration.patterns.user.service.integration.AsyncSearchServiceIntegration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.async.DeferredResult;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RequestMapping("/users")
public class AsyncUserController {

  private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final AsyncSearchServiceIntegration asyncSearchServiceIntegration;
  private final ConcurrentHashMap<String, User> userRepository;

  public AsyncUserController(AsyncSearchServiceIntegration asyncSearchServiceIntegration) {
    this.asyncSearchServiceIntegration = asyncSearchServiceIntegration;
    this.userRepository = new ConcurrentHashMap<>();
  }

  @ResponseStatus(CREATED)
  @RequestMapping(method = POST)
  @ResponseBody
  public DeferredResult<String> createUser(@RequestBody User user, @RequestParam(required = false) Integer waitTime) {
    DeferredResult<String> deferredResult = new DeferredResult<>();

    LOG.info("created user '{}' '{}'", user.getFirstName(), user.getLastName());
    this.userRepository.put(user.getId(), user);

    if (waitTime == null) {
      this.asyncSearchServiceIntegration.indexUser(user, ignored -> deferredResult.setResult(user.getId()), deferredResult::setErrorResult);
    } else {
      this.asyncSearchServiceIntegration.indexUserSlow(user, waitTime, ignored -> deferredResult.setResult(user.getId()), deferredResult::setErrorResult);
    }

    return deferredResult;
  }

  @RequestMapping(value = "/{id}", method = GET)
  @ResponseBody
  public ResponseEntity<?> getUser(@PathVariable String id) {
    if (this.userRepository.containsKey(id)) {
      return new ResponseEntity<>(this.userRepository.get(id), OK);
    } else {
      return new ResponseEntity<>(format("user with id '%s' does not exist", id), NOT_FOUND);
    }
  }

  @RequestMapping(value = "/search-by-firstname")
  @ResponseBody
  public DeferredResult<List<User>> searchUserByFirstName(@RequestParam String firstName) {
    DeferredResult<List<User>> deferredResult = new DeferredResult<>();
    this.asyncSearchServiceIntegration.searchUserByFirstName(firstName, deferredResult::setResult, deferredResult::setErrorResult);

    return deferredResult;
  }

}
