package org.mimacom.sample.integration.patterns.search.service.web;

import org.mimacom.sample.integration.patterns.search.service.document.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class SearchServiceController {

  private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final Map<String, User> userIndex;

  public SearchServiceController() {
    this.userIndex = new ConcurrentHashMap<>();
  }

  @ResponseStatus(OK)
  @RequestMapping(value = "/index", method = POST)
  public void index(@RequestBody User user, @RequestParam(required = false) Integer waitTime) {
    LOG.info("index was called!");

    if (waitTime != null) {
      waitFor(waitTime);
    }

    indexUser(user);
  }

  @RequestMapping(value = "/search-by-firstname", method = GET)
  public List<User> searchUserByFirstName(@RequestParam String firstName) {
    return this.userIndex.entrySet().stream()
        .filter(entry -> entry.getKey().startsWith(firstName))
        .map(Map.Entry::getValue)
        .collect(toList());
  }

  private void indexUser(User user) {
    this.userIndex.put(user.getFirstName(), user);
  }

  private static void waitFor(int waitTimeInSeconds) {
    try {
      Thread.sleep(waitTimeInSeconds * 1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

}
