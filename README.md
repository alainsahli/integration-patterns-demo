# Integration Patterns Demo

## General Instruction To Start The Applications
Build the application by running `./mvnw package`.
Then start the user application by running `java -jar integration-patterns-demo-user-service/target/integration-patterns-demo-user-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=<profile>`
and the search application by running `java -jar integration-patterns-demo-search-service/target/integration-patterns-demo-search-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=<profile>`.
The part `--spring.profiles.active=<profile>` must only defined if specified in the use case.

## Testing Scripts
There are three testing scripts:

* _./scripts/create-user.sh_: Creates a new user 'Homer Simpson' (see _./scripts/payloads/create-user.json_). There is
  also an option to define the waitTime on the search application. This option is useful to simulate slow requests: _./scripts/create-user.sh 60_
  waits 60 seconds before returning.
* _./scripts/search-user-by-firstname.sh <firstName>_: Searches a user that has a first name starting with the letter provided
  as argument (_./scripts/search-user-by-firstname.sh Hom_ will return Homer Simpson).
* _./scripts/get-user.sh <id>_ Returns the user by it's ID if it exists.

## Important Notice

* The embedded Tomcat is intentionally started with only two worker threads in order to easily show the thread starvation issues.

### Use Case 1: Simple Synchronized Integration

1. Start the user application with the profile _simple_ and the search application without profile.
2. Check that everything works fine by running _./scripts/create-user.sh_ and then searching for it _./scripts/search-user-by-firstname.sh Hom_.
3. Open JVisualVM or your Java profiler of choice and set the focus to the two Tomcat worker threads (http-nio-8080-exec-1 and http-nio-8080-exec-2). 
4. Create two users with a long wait time (in parallel, i.e. in two shells simultaneously): _./scripts/create-user.sh 60_.
5. Try to get a user by id (while the two user creation scripts are running) and assert that no response is coming because all tomcat worker threads are busy.
6. Try to search for a user (while the two user creation scripts are running) _./scripts/search-user-by-firstname.sh Hom_ and assert that no response is coming because all tomcat worker threads are busy.

### Use Case 2: Async Integration
In use case 1 we have seen that when requests to an external application are slow it brings down the whole application.
In this use case we are going to run requests to external systems (search application) in a separate thread pool in order to free the tomcat worker threads as quickly as possible.
This way it should be possible to send requests to tomcat and get a quick response even if the thread pool for the async requests is starving.

1. Start the user application with the profile _async_ and the search application without profile.
2. Check that everything works fine by running _./scripts/create-user.sh_ and then searching for it _./scripts/search-user-by-firstname.sh Hom_.
3. Open JVisualVM or your Java profiler of choice and set the focus to the two Tomcat worker threads (http-nio-8080-exec-1 and http-nio-8080-exec-2)
   and the two search service integration threads (SearchServiceIntegration-1 and SearchServiceIntegration-2).
4. Create two users with a long wait time (in parallel, i.e. in two shells simultaneously): _./scripts/create-user.sh 60_.
5. Try to get a user by id (while the two user creation scripts are running) and assert that a response is quickly coming because the tomcat worker threads are now free.
6. Try to search for a user (while the two user creation scripts are running) _./scripts/search-user-by-firstname.sh Hom_ and assert that no response is coming because all search service integration threads are busy.

### Use case 3: Async Integration With Bulkheads
In use case 2 we have seen that when only one thread pool is used to send requests to an external application and one type of request is slow (create user) it brings
other calls to an external application down (search user was not available anymore). To solve this issue we will create a thread pool per external request type (one for
index user and one for search user). This pattern is called "bulk head".

1. Start the user application with the profile _bulkhead_ and the search application without profile.
2. Check that everything works fine by running _./scripts/create-user.sh_ and then searching for it _./scripts/search-user-by-firstname.sh Hom_.
3. Open JVisualVM or your Java profiler of choice and set the focus to the two Tomcat worker threads (http-nio-8080-exec-1 and http-nio-8080-exec-2), the
   two search service integration threads (SearchUser-1 and SearchUser-2) and the two index service integration threads (IndexUser-1 and IndexUser-2).
4. Create two users with a long wait time (in parallel, i.e. in two shells simultaneously): _./scripts/create-user.sh 60_.
5. Try to search for a user (while the two user creation scripts are running) _./scripts/search-user-by-firstname.sh Hom_ and assert that a response is quickly coming because the search user thread pool has free threads.
6. Try to get a user by id (while the two user creation scripts are running) and assert that a quick response is still coming.
7. Stop the search application and assert how exceptions are thrown on every request (create user or search user).

### Use Case 4: Integration With Circuit Breakers
In use case 3 there was no performance issues anymore, if one endpoint becomes slow (index user) the rest of the application is still working fine. But as soon
as the external application becomes unavailable a lot of exception are thrown. To solve this issue the circuit breaker helps. A circuit breaker has an internal
counter for failures and when a defined threshold is reached the circuit opens and no more requests are sent and a fallback is provided. This way pressure on the
external system is removed and this favours recovery. To implement this pattern [Hystrix](https://github.com/Netflix/Hystrix) is used, which also implements the bulkhead
pattern out of the box.

1. Start the user application with the profile _hystrix_ and the search application without profile.
2. Check that everything works fine by running _./scripts/create-user.sh_ and then searching for it _./scripts/search-user-by-firstname.sh Hom_.
3. Create two users with a long wait time (in parallel, i.e. in two shells simultaneously): _./scripts/create-user.sh 60_.
4. Try to search for a user (while the two user creation scripts are running) _./scripts/search-user-by-firstname.sh Hom_ and assert that a response is quickly coming because the search user thread pool has free threads.
5. Try to get a user by id (while the two user creation scripts are running) and assert that a quick response is still coming.
6. Stop the search application.
7. Try to search a lot of times in a row and assert how a default answer is coming. At the same time, pay attention to the logs to assert that only a few request are actually sent to the search application.
   If the search application is restarted, everything works find again.
   
INFORMATION: Hystrix was configured to open the circuit if more than two requests are failing per 5 seconds. After that only one request every 5 seconds passes through and if it is successful
the circuit is closed again.

### Use Case 5: Reliable Integration With Queues
In use case 4 almost everything is good except that when a user is created and the search service is not available it is not going to be indexed. To solve that a persistent
messaging solution must be used to assure that the indexation of the user will happen at least once. To implement that we will create local message brokers using AcitveMQ
in the user application and search application. Every local message broker has its own database and the brokers are connected together using the network transportation feature
of ActiveMQ. This way when a user is created a message is published and persisted and if the search application is not available it will be replayed as soon as it is available
again.

1. Start the user application with the profile _jms_ and the search application with the profile _jms_.
2. Check that everything works fine by running _./scripts/create-user.sh_ and then searching for it _./scripts/search-user-by-firstname.sh Hom_.
3. Stop the search application
4. Create a user _./scripts/create-user.sh_.
5. Start the search application and check the logs to assert that the newly created user is immediately created after startup. 
6. Try to search for a user _./scripts/search-user-by-firstname.sh Hom_ and assert that a response is quickly coming.
7. Optionally throw an exception in the [listener endpoint](https://github.com/alainsahli/integration-patterns-demo/blob/master/integration-patterns-demo-search-service/src/main/java/org/mimacom/sample/integration/patterns/search/service/service/IndexUserListenerEndpoint.java#L39)
   and create a user. ActiveMQ will do some retries and the message will land in a dead letter queue.
8. Remove the exception.
9. Using JMX you can replay the message in the dead letter queue and assert the proper indexation of the user.

## Open Points
* Store users in database instead of memory to show the 'shared resource' transaction management with JMS
