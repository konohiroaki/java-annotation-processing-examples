# Pluggable Annotation Processing API Examples
This is an example project for Pluggable Annotation Processing API (JSR 269).

## How to run
```sh
$ cd /<path-to-project>

# Install library
$ cd annotation-processor
$ mvn clean install

# Do Annotation Processing
$ cd ../simple-project
$ mvn clean compile

# You can see the transformed class files.
```
