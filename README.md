# Project Dependency Analyzer

This Dependency Analyzer can analyze multi-project builds and shows the sub-project's dependencies to each other as a graph.

> To use the Project Dependency Analyzer you need [Graphviz (dot)](https://graphviz.org/download/) installed and on your path.

## Build Project
To build the standalone executable application execute:
```shell
./gradlew app:installDist
```

## Run the Application
To run the application build the [standalone executable](#build-project) and then execute:
```shell
<project dir>/app/build/install/analyzer/bin/analyzer -h
```

This will display the usage of the application and should get you started.
