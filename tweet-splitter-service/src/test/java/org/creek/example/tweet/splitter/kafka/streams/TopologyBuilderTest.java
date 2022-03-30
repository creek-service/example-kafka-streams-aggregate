/*
 * Copyright 2022 Creek Contributors (https://github.com/creek-service)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.creek.example.tweet.splitter.kafka.streams;

import static org.apache.kafka.streams.KeyValue.pair;
import static org.creek.api.kafka.streams.test.TestTopics.inputTopic;
import static org.creek.api.kafka.streams.test.TestTopics.outputTopic;
import static org.creek.example.services.TweetSplitterServiceDescriptor.TweetTopic;
import static org.creek.example.services.TweetSplitterServiceDescriptor.WordTopic;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.creek.api.kafka.streams.extension.KafkaStreamsExtension;
import org.creek.api.kafka.streams.test.TestKafkaStreamsExtensionOptions;
import org.creek.api.service.context.CreekContext;
import org.creek.api.service.context.CreekServices;
import org.creek.api.test.TestPaths;
import org.creek.example.services.TweetSplitterServiceDescriptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TopologyBuilderTest {

    private static CreekContext ctx;

    private TopologyTestDriver testDriver;
    private TestInputTopic<Long, String> tweets;
    private TestOutputTopic<String, Long> words;
    private Topology topology;

    @BeforeAll
    public static void classSetup() {
        ctx =
                CreekServices.builder(new TweetSplitterServiceDescriptor())
                        .with(TestKafkaStreamsExtensionOptions.defaults())
                        .build();
    }

    @BeforeEach
    public void setUp() {
        final KafkaStreamsExtension ext = ctx.extension(KafkaStreamsExtension.class);

        topology = new TopologyBuilder(ext).build();

        testDriver = new TopologyTestDriver(topology, ext.properties());

        tweets = inputTopic(TweetTopic, ctx, testDriver);
        words = outputTopic(WordTopic, ctx, testDriver);
    }

    @AfterEach
    public void tearDown() {
        testDriver.close();
    }

    @Test
    void shouldOutputWords() {
        // When:
        tweets.pipeInput(1L, "my first tweet");

        // Then:
        assertThat(
                words.readKeyValuesToList(),
                contains(pair("my", 1L), pair("first", 1L), pair("tweet", 1L)));
    }

    @Test
    void shouldOutputWordsInLowerCase() {
        // When:
        tweets.pipeInput(2L, "Some TeXt");

        // Then:
        assertThat(words.readKeyValuesToList(), contains(pair("some", 2L), pair("text", 2L)));
    }

    @Test
    void shouldTrim() {
        // When:
        tweets.pipeInput(3L, "\t some \r text \n ");

        // Then:
        assertThat(words.readKeyValuesToList(), contains(pair("some", 3L), pair("text", 3L)));
    }

    @Test
    void shouldIgnorePunctuation() {
        // When:
        tweets.pipeInput(-1L, "text. with, punctuation!");

        // Then:
        assertThat(
                words.readKeyValuesToList(),
                contains(pair("text", -1L), pair("with", -1L), pair("punctuation", -1L)));
    }

    @Test
    void shouldOutputNothingOnEmptyTweet() {
        // When:
        tweets.pipeInput(1L, "");

        // Then:
        assertThat(words.readKeyValuesToList(), is(empty()));
    }

    @Test
    void shouldOutputNothingOnTweetWithNoWords() {
        // When:
        tweets.pipeInput(1L, " \t \n\r");

        // Then:
        assertThat(words.readKeyValuesToList(), is(empty()));
    }

    /**
     * A test that intentionally fails when ever the topology changes.
     *
     * <p>This is to make it less likely that unintentional changes to the topology are committed
     * and that thought is given to any intentional changes to ensure they won't break any deployed
     * instances.
     *
     * <p>Care must be taken when changing a deployed topology to ensure either:
     *
     * <ol>
     *   <li>Changes are backwards compatible and won't leave data stranded in unused topics, or
     *   <li>The existing topology is drained before the new topology is deployed
     * </ol>
     *
     * <p>Option #1 allows for the simplest deployment, but is not always possible or desirable.
     */
    @Test
    void shouldNotChangeTheTopologyUnintentionally() {
        // Given:
        final String expectedTopology =
                TestPaths.readString(
                        TestPaths.moduleRoot("tweet-splitter-service")
                                .resolve("src/test/resources/kafka/streams/expected_topology.txt"));

        // When:
        final String currentTopology = topology.describe().toString();

        // Then:
        assertThat(currentTopology.trim(), is(expectedTopology.trim()));
    }
}
