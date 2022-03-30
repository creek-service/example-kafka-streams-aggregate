# Example tweet splitter service

Consumes the `tweet.text` topic, which contains the text of tweets in the value of each topic record, and splits 
each tweet into its constituent words using a simple algorithum. Each word is output as a separate record to the
`tweet.word` topic, with the word stored in the record key. 

Storing the word in the record key, not the value, leverages Kafka's key partitioning to ensure all instances of
the same word are stored in the same topic-partitioning. This makes the job of the `word-counter-service` much
easier, as it only needs to count word counts per-partition.