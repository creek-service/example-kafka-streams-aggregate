# Example tweet word count service

Consumes the `tweet.word` topic, which contains records with specific words in the record key, groups by the key and
calculates daily word counts, which are output to the `tweet.word.count` topic.