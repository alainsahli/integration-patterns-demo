package org.mimacom.sample.integration.patterns.search.service.web;

import org.mimacom.sample.integration.patterns.search.service.document.User;
import org.mimacom.sample.integration.patterns.search.service.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.lang.invoke.MethodHandles;
import java.util.List;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@ResponseBody
@RequestMapping
public class SearchServiceController {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final SearchService searchService;

  public SearchServiceController(SearchService searchService) {
    this.searchService = searchService;
  }

  @ResponseStatus(OK)
  @RequestMapping(value = "/index", method = POST)
  public void index(@RequestBody User user, @RequestParam(required = false) Integer waitTime) {
    LOG.info("index was called!");

    if (waitTime != null) {
      waitFor(waitTime);
    }

    this.searchService.indexUser(user);
  }

  @RequestMapping(value = "/search-by-firstname", method = GET)
  public List<User> searchUserByFirstName(@RequestParam String firstName) {
    return this.searchService.searchUserByFirstName(firstName);
  }

  private static void waitFor(int waitTimeInSeconds) {
    try {
      Thread.sleep(waitTimeInSeconds * 1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

}
