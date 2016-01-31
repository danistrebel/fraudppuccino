Summary
-------
This framework allows to scalably identify dependent financial transactions based on local transaction matching and provides a flexible query language to express a broad trange of transactional patterns.

Background
----------

The detection of fraudulent patterns in large sets of financial transaction data is a crucial task in forensic investigations of money laundering, employee fraud and various other illegal activities. Scalable and flexible tools are needed to be able to analyze these large amounts of data and express the complex structures of the patterns that should be de- tected.

This work is the result of my [Master thesis](http://www.merlin.uzh.ch/publication/show/9077 "Thesis Link") that aimed to present a novel approach of locally identifying associations between incoming and outgoing transactions for each participant of a transaction network and then aggregating these associations to larger patterns. The identified patters can be pruned and visualized in a graphical user interface to conduct further investigations.
The evaluation of this approach showed that it allows a stream-processing of real-world financial transactions with a throughput of more than one million transactions per minute. Furthermore we demonstrate the capability of our approach to express sophisticated money laundering patterns.
