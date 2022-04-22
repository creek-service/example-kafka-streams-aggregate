[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Coverage Status](https://coveralls.io/repos/github/creek-service/example-kafka-streams-aggregate/badge.svg?branch=main)](https://coveralls.io/github/creek-service/example-kafka-streams-aggregate?branch=main)
[![build](https://github.com/creek-service/example-kafka-streams-aggregate/actions/workflows/gradle.yml/badge.svg)](https://github.com/creek-service/example-kafka-streams-aggregate/actions/workflows/gradle.yml)
[![CodeQL](https://github.com/creek-service/example-kafka-streams-aggregate/actions/workflows/codeql.yml/badge.svg)](https://github.com/creek-service/example-kafka-streams-aggregate/actions/workflows/codeql.yml)

# Example Kafka Streams Aggregate

This example demonstrates how Kafka Streams based micro-services can be quickly developed and tested using Creek.

For no reason other than to demonstrate Creek, this repo takes a stream of tweets (just the text itself), splits it 
into individual words, and calculates word counts.  This is done across two services purely to demonstrate
how resources, such as topics, are shared between services.

## Concepts covered by the example:

The repo deomonstrates how to build Kafka Streams based micro-service using Creek. Including:

* Defining an [Aggregate descriptors][1]
* Defining Service Descriptors ([tweet-splitter-service][2], [word-counter-service][3]), including using
  resources from other descriptors to link descriptor inputs and outputs.
* Initialization of a `CreekContext` in each service.
* Building simple topologies using the `KafkaStreamExtension` to Creek ([tweet-splitter-service][4], [word-counter-service][5]).
* Testing simple topologies using the Creek test utils for Kafka Streams ([tweet-splitter-service][6], [word-counter-service][7]).

## Modules

The aggregate has the following standard aggregate modules:

* **[api](api)**: defines the public api of the aggregate, i.e. the service descriptor and associated types.
* **[ids](ids)**: defines type safe wrappers around simple id types.
* **[services](services)**: defines all the services in the aggregate, i.e. service descriptors and their associated types.
* **[common](common)**: common code shared between services in this aggregate.

...and the following services:

* **[tweet-splitter-service](tweet-splitter-service)**: accepts a stream of tweets and splits them into words. 
* **[word-counter-service](word-counter-service)**: computes ongoing word counts.

### Gradle commands

* `./gradlew format` will format the code using [Spotless][1].
* `./gradlew static` will run static code analysis, i.e. [Spotbugs][2] and [Checkstyle][3].
* `./gradlew check` will run all checks and tests.

[1]: api/src/main/java/org/creek/example/api/WordCountAggregateDescriptor.java
[2]: services/src/main/java/org/creek/example/services/TweetSplitterServiceDescriptor.java
[3]: services/src/main/java/org/creek/example/services/WordCounterServiceDescriptor.java
[4]: tweet-splitter-service/src/main/java/org/creek/example/tweet/splitter/kafka/streams/TopologyBuilder.java
[5]: word-counter-service/src/main/java/org/creek/example/word/counter/kafka/streams/TopologyBuilder.java
[6]: tweet-splitter-service/src/test/java/org/creek/example/tweet/splitter/kafka/streams/TopologyBuilderTest.java
[7]: word-counter-service/src/test/java/org/creek/example/word/counter/kafka/streams/TopologyBuilderTest.java