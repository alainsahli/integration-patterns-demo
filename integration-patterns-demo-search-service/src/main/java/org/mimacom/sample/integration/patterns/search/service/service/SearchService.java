package org.mimacom.sample.integration.patterns.search.service.service;

import org.mimacom.sample.integration.patterns.search.service.document.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;

@Service
public class SearchService {

  private final Map<String, User> userIndex;

  public SearchService() {
    this.userIndex = new ConcurrentHashMap<>();
  }

  public void indexUser(User user) {
    this.userIndex.put(user.getFirstName(), user);
  }

  public List<User> searchUserByFirstName(String firstName) {
    return this.userIndex.entrySet().stream()
        .filter(entry -> entry.getKey().startsWith(firstName))
        .map(Map.Entry::getValue)
        .collect(toList());
  }

}
