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

package org.creek.example.word.counter.kafka.streams;

import static java.lang.String.valueOf;
import static java.util.Objects.requireNonNull;
import static org.creek.example.internal.TopicConfigBuilder.MIN_SEGMENT_SIZE;
import static org.creek.example.services.WordCounterServiceDescriptor.WordCountTopic;
import static org.creek.example.services.WordCounterServiceDescriptor.WordTopic;

import java.util.Map;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.Stores;
import org.creek.api.kafka.common.resource.KafkaTopic;
import org.creek.api.kafka.streams.extension.KafkaStreamsExtension;
import org.creek.api.kafka.streams.util.Name;

/** Builds an example topology for grouping by and counting words. */
public final class TopologyBuilder {

    private final KafkaStreamsExtension ext;
    private final Name name = Name.root();

    public TopologyBuilder(final KafkaStreamsExtension ext) {
        this.ext = requireNonNull(ext, "ext");
    }

    public Topology build() {
        final StreamsBuilder builder = new StreamsBuilder();

        final KafkaTopic<String, Long> words = ext.topic(WordTopic);
        final KafkaTopic<String, Long> wordCounts = ext.topic(WordCountTopic);

        builder.stream(
                        words.name(),
                        Consumed.with(words.keySerde(), words.valueSerde())
                                .withName(name.name("ingest-" + words.name())))
                .groupByKey(Grouped.as(name.name("group-by-key")))
                .count(
                        name.named("count-word"),
                        Materialized.<String, Long>as(Stores.inMemoryKeyValueStore("count-store"))
                                .withLoggingEnabled(
                                        Map.of(
                                                TopicConfig.SEGMENT_BYTES_CONFIG,
                                                valueOf(MIN_SEGMENT_SIZE)))
                                .withKeySerde(wordCounts.keySerde())
                                .withValueSerde(wordCounts.valueSerde()))
                .toStream(name.named("to-stream"))
                .to(
                        wordCounts.name(),
                        Produced.with(wordCounts.keySerde(), wordCounts.valueSerde())
                                .withName(name.name("egress-" + wordCounts.name())));

        return builder.build(ext.properties());
    }
}
