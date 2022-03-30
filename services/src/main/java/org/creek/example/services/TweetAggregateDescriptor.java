/*
 * Copyright 2021-2022 Creek Contributors (https://github.com/creek-service)
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

package org.creek.example.services;

import static org.creek.example.internal.TopicConfigBuilder.withPartitions;
import static org.creek.example.internal.TopicDescriptors.outputTopic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.creek.api.kafka.metadata.OwnedKafkaTopicOutput;
import org.creek.api.platform.metadata.AggregateDescriptor;
import org.creek.api.platform.metadata.ComponentInput;
import org.creek.api.platform.metadata.ComponentOutput;

/**
 * The aggregate descriptor of a fictitious upstream aggregate that publishes tweet text.
 *
 * <p>Normally, this would be defined in the upstream aggregate's {@code api} jar, and brought in as
 * a dependency. However, to keep this example repo self-contained, its defined here.
 */
public final class TweetAggregateDescriptor implements AggregateDescriptor {

    private static final List<ComponentInput> INPUTS = new ArrayList<>();
    private static final List<ComponentOutput> OUTPUTS = new ArrayList<>();

    /** A stream of tweet-id to tweet-text. */
    public static final OwnedKafkaTopicOutput<Long, String> TweetTopic =
            register(outputTopic("tweet.text", long.class, String.class, withPartitions(1)));

    @Override
    public Collection<ComponentInput> inputs() {
        return List.copyOf(INPUTS);
    }

    @Override
    public Collection<ComponentOutput> outputs() {
        return List.copyOf(OUTPUTS);
    }

    private static <T extends ComponentOutput> T register(final T output) {
        OUTPUTS.add(output);
        return output;
    }
}
