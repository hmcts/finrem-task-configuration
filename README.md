# finrem-task-configuration

Work Allocation task configuration for the Financial Remedy jurisdiction. This repository contains DMN files that define when tasks are created, cancelled, completed, and what permissions apply — it is not a deployable application.

## Overview

The DMN files in `src/main/resources/` cover:

- **wa-task-initiation** — which CCD events trigger task creation and the task's working days allowed
- **wa-task-cancellation** — which events cancel an open task
- **wa-task-completion** — which events auto-complete a task
- **wa-task-configuration** — task attributes (name, description, role assignments, priority)
- **wa-task-permissions** — role-based permissions per task type
- **wa-task-types** — registry of all task type IDs
- **wa-task-allowed-days** — working day calendar configuration

## Running the tests

The project uses [Gradle](https://gradle.org) as a build tool. The `./gradlew` wrapper is included, so no local Gradle installation is needed.

Run all tests:

```bash
./gradlew test
```

Run a specific test class:

```bash
./gradlew test --tests CamundaTaskInitiationConsentedTest
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
