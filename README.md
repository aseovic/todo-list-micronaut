# Course materials for Coherence To Do List (Micronaut) workshop

>This is a complete, final implementation of the application created during the workshop.
> 
>To switch to the starting point for the workshop, check out the `initial` branch:
> 
>```bash
> git checkout initial
> ```

## Instructions
   
### Build the Application

#### Maven

```bash
mvn clean package
```

#### Gradle

```bash
./gradlew clean build
```

### Run the Application

#### Maven

```bash  
mvn exec:exec
```

#### Gradle

```bash
./gradlew run
```

### Build a Docker Image

#### Maven

```bash
mvn clean install
mvn package -P docker 
```

#### Gradle

```bash
./gradlew clean jibDockerBuild
```

### Run the Docker Container

```bash
docker run -d -p 5001:5001 -P 5002:5002 ghcr.io/coherence-community/todo-list-micronaut-server
```

> NOTE: `5001` is the HTTP port, and `5002` is the metrics port.

### Access the Web UI

Access via http://localhost:5001/

![To Do List - React Client](assets/react-client.png)

### Query the GraphQL Endpoint

The GraphQL Endpoint is available at http://localhost:5001/graphql. Use one of the following tools to interact wih it:

- [GraphiQL](https://github.com/graphql/graphiql)
- [Insomnia](https://insomnia.rest/download)

To retrieve a collection of tasks, use the following query:

```graphql
query {
  tasks(completed: false) {
    id
    description
    completed
    createdAt
    createdAtDate
  }
}
```

## References

* [Coherence CE](https://coherence.community/)
* [Micronaut](https://micronaut.io/)
