# TREND: Webinterface

The webinterface as a frontend can be used to add watermarks via a GUI. It uses the TREND
watermarker library.

## Getting Started

### System prerequisites

The following things are needed to run this application:

- For manual builds:
    - A Java Runtime Environment (JRE)
    - The watermarker library, published in maven local(*)
- For containerized builds:
    - docker & docker-compose

(*) To publish the watermarker library to your maven local repository (if not already done), execute
the following commands from the root directory of the project:

1. `cd watermarker`
2. `./gradlew publishToMavenLocal`

### Manual Build

Use Gradle to manually build and run the webinterface:

1. `cd webinterface` (if not already there)
2. `./gradlew -t run`
    - In case of a production build, `./gradlew clean zip` should be used instead
3. Visit http://localhost:3000

### Containerized Build

Run the `docker-compose.yml` file in the root directory of the project:

```
docker-compose up
```

After the startup finished, try to visit the webinterface at http://localhost:8080
