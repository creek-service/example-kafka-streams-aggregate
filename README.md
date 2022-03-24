[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![build](https://github.com/creek-service/example-kafka-streams-aggregate/actions/workflows/gradle.yml/badge.svg)](https://github.com/creek-service/example-kafka-streams-aggregate/actions/workflows/gradle.yml)

# Example Kafka Streams Aggregate

This example demonstrates how Kafka Streams based micro-services can be quickly developed and tested using Creek.

For no reason other than to demonstrate Creek, this repo takes a stream of tweets (just the text itself), splits it 
into individual words, and calculates word counts per-day.

## Modules

The aggregate has the following standard aggregate modules:

* **[api](api)**: defines the public api of the aggregate, i.e. the service descriptor and associated types.
* **[ids](ids)**: defines type safe wrappers around simple id types.
* **[services](services)**: defines all the services in the aggregate, i.e. service descriptors and their associated types.
* **[common](common)**: common code shared between services in this aggregate.

...and the following services:

* **[tweet-splitter-service](tweet-splitter-service)**: accepts a stream of tweets and splits them into words. 
* **[word-counter-service](word-counter-service)**: computes word counts per day. 

## Concepts covered by the example:

* Initialization of the `CreekContext` 
* todo... service / agg descripts, outputs becoming inputs, internal topics, state stores, serde, etc.

### Gradle commands

* `./gradlew format` will format the code using [Spotless][1].
* `./gradlew static` will run static code analysis, i.e. [Spotbugs][2] and [Checkstyle][3].
* `./gradlew check` will run all checks and tests.