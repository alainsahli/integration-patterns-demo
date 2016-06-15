package org.mimacom.sample.integration.patterns.search.service.service;

import org.mimacom.sample.integration.patterns.search.service.document.User;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.stream.Collectors.toList;

public class SearchService {

  private final List<User> userIndex;

  public SearchService() {
    this.userIndex = new CopyOnWriteArrayList<>();
  }

  public void indexUser(User user) {
    this.userIndex.add(user);
  }

  public List<User> searchUserByFirstName(String firstName) {
    return this.userIndex.stream()
        .filter(user -> user.getFirstName().startsWith(firstName))
        .collect(toList());
  }

}
