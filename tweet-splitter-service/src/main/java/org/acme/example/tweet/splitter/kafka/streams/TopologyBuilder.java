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

package org.acme.example.tweet.splitter.kafka.streams;

import static java.util.Objects.requireNonNull;
import static org.acme.example.services.TweetSplitterServiceDescriptor.TweetTopic;
import static org.acme.example.services.TweetSplitterServiceDescriptor.WordTopic;
import static org.apache.kafka.streams.KeyValue.pair;

import java.util.Arrays;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.kstream.TransformerSupplier;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.creekservice.api.kafka.common.resource.KafkaTopic;
import org.creekservice.api.kafka.streams.extension.KafkaStreamsExtension;
import org.creekservice.api.kafka.streams.extension.util.Name;

/**
 * Builds an example topology for splitting tweet text into individual words.
 *
 * <p>The splitting algorithm is not meant to be fool-proof, it's just an example ;)
 */
public final class TopologyBuilder {

    private final KafkaStreamsExtension ext;
    private final Name name = Name.root();

    public TopologyBuilder(final KafkaStreamsExtension ext) {
        this.ext = requireNonNull(ext, "ext");
    }

    public Topology build() {
        final StreamsBuilder builder = new StreamsBuilder();

        final KafkaTopic<Long, String> tweets = ext.topic(TweetTopic);
        final KafkaTopic<String, Long> words = ext.topic(WordTopic);

        builder.stream(
                        tweets.name(),
                        Consumed.with(tweets.keySerde(), tweets.valueSerde())
                                .withName(name.name("ingest-" + tweets.name())))
                .flatTransform(split(), name.named("split"))
                .to(
                        words.name(),
                        Produced.with(words.keySerde(), words.valueSerde())
                                .withName(name.name("egress-" + words.name())));

        return builder.build(ext.properties());
    }

    private TransformerSupplier<Long, String, Iterable<KeyValue<String, Long>>> split() {
        return () ->
                new Transformer<>() {
                    @Override
                    public void init(final ProcessorContext context) {}

                    @Override
                    public Iterable<KeyValue<String, Long>> transform(
                            final Long tweetId, final String tweetText) {
                        return () ->
                                Arrays.stream(tweetText.split("[\\s[^a-z|A-Z]]"))
                                        .filter(word -> !word.isBlank())
                                        .map(String::toLowerCase)
                                        .map(word -> pair(word, tweetId))
                                        .iterator();
                    }

                    @Override
                    public void close() {}
                };
    }
}
