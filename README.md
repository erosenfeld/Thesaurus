# Introduction #
---

This README describes how the Thesaurus was produced. Much of the work is based off of [Automatic Retrieval and Clustering of Similar Words](http://www.computing.dcu.ie/~ebicici/Week3/acl98.pdf), along with some ideas from [A Dataset of Syntactic-Ngrams over Time from a Very Large Corpus of English Books](http://commondatastorage.googleapis.com/books/syntactic-ngrams/syntngrams.final.pdf). Instructions on how to produce your own Thesaurus using a different corpus can be found under **Creating Your Own Thesaurus** (be warned that for any appreciably-sized dataset this takes several hundred hours of runtime using several hundred gigs of both hard-drive space and RAM). Instructions on using the provided thesaurus can be found under **How to Implement**.

Due to the data being split into 99 files (arcs.00-of-98.txt through arcs.98-of-98.txt), "\*\*" will be used to indicate all files, 00 through 98.

The initial files required are the Syntactic N-grams Over Time provided by Google Research. The overview can be found at <http://googleresearch.blogspot.com/2013/05/syntactic-ngrams-over-time.html>, which links to the actual download at <http://commondatastorage.googleapis.com/books/syntactic-ngrams/index.html>. Only the standard Arcs (files 00-98) from the subsection **English 1 Million** were used. While the files themselves are not included, an md5 checksum of each of the gzipped arc files and each file's word count is located in eras/arcs.

The arc files provided by Google were parsed into a dictionary, where each word hashed to its number of occurrences in four different eras, as well as the total count of the word. The four eras were \*\*\*\*-1920, 1920-1950, 1950-2000, and 2000-\*\*\*\* (roughly divided by WWI and WWII, as well as the new millennium). For some reason, the Google files include a few items that are marked as appearing zero times total, so the arc files had to be purged of all such occurrences.

The words were then sorted by magnitude, based on their number of occurrences in each of the eras, and written to four different files (one for each era) Using Mathematica, each of the four files were graphed on a plot of word frequency rank by actual word frequency, shown below.

![pre1920](https://s3.amazonaws.com/erosenfeld.github.com/listlogplot%5B1%5D.jpg)
![1920-1950](https://s3.amazonaws.com/erosenfeld.github.com/listlogplot%5B2%5D.jpg)
![1950-2000](https://s3.amazonaws.com/erosenfeld.github.com/listlogplot%5B3%5D.jpg)
![post2000](https://s3.amazonaws.com/erosenfeld.github.com/listlogplot%5B4%5D.jpg)

At this point it was decided that only the top 250,000 words should be used, and only from eras 2 and 3 (otherwise subsequent scripts would take far too long to run). Thus, the two eras were consolidated, and the arc files were purged of any words that were not present among the most frequent 250K.

Next, all the arcs were rewritten in the form [word, POS tag, relation, second word, POS tag, count]. These arcs also had any occurrence of particular non-alphanumeric characters (such as '/','\','.', etc.) removed; the existence of these characters in some of the words was attributed to mistakes made by Google's OCR system, as this is where a large portion of the data came from.

Each word and its Part of Speech (POS) tag was then replaced with a token. Each token is simply the word, with "/A", "/N", or "/V" appended to the end, depending on whether the part of speech indicates the word is used as an adjective, noun, or verb, respectively. Another opportunity was taken to cleanse the dataset, where any words whose POS tags were not indicative of either a noun, verb, or adjective were removed from consideration.

The thesaurus needs a list of dictionary words. I used the dictionary provided by Mathematica by calling `DictionaryLookup[]`.

Following these steps, a series of Python scripts were used to further condense and format the data. A full step-by-step procedure can be found under **Creating Your Own Thesaurus**.

# How to Implement #
---

There are two options for implementing the thesaurus, each with upsides and downsides. The first approach is slower but more thorough, while the second approach requires additional setup, but rewards you with a practically instant lookup time.

## Similarity ##

### SimilarityTester ###

To get a feel for the meaning of the "similarity" value, you can run

    java SimilarityTester

Note that this program requires about 4 GB of memory, so you may have to increase the Java heap space with the `-Xmx` flag. This will read in the 98 entropy files and prompt you for two words. Make sure to append "/N", "/A", or "/V" to the words, as the thesaurus treats different POS tags as entirely different tokens. Since similarity is symmetric, the order of the words doesn't matter. If a word doesn't appear in the corpus, the value returned will be 0.0.

Running this will give you a feel for the kind of data to expect. As a general rule, a similarity of 0.1 between nouns indicates a strong correlation, while verbs and adjectives will often require a similarity of 0.15 or even higher before they can be considered "linked". Numerically, similarity is an estimate of the probability that one word can replace another while maintaining syntactic integrity. Since the similarity between `train/N` and `bus/N` is 0.15184711204531073, this means that ~15% of the time, one can be substituted for the other and the text will not change drastically in meaning.

Lower similarities indicate either no connection or a little connection; a high similarity means that either the two words are actual synonyms, or that one word can often be replaced with another without a major syntax change (e.g. "I took the train to work" vs. "I took the bus to work").  Some examples are listed below:

```
car/N	- 	automobile/N:	0.2648248259050074
car/N	- 	bicycle/N:		0.12565174064582982
car/N	- 	flower/N:		0.03820317325885916

compute/V	- 	calculate/V:	0.5653742891117509
compute/V	- 	multiply/V:		0.18779126065230747
compute/V	- 	extricate/V:	0.02329896232188951

happy/A - cheerful/A:		0.2585512615001065
happy/A - sad/A:			0.16087481318121205
happy/A - melodramatic/A:	0.017074012637758835
```

While the program accepts two words with different POS tags, they rarely have a high similarity because of how "similarity" is defined and calculated. However, the option is there.

### Running the Program ###

Now that you have a good feel for similarity, you can run

    java Similarity

Again, this will require about 4 GB of memory, so use the `-Xmx` flag if necessary. This program takes in a single word and prints out all matches with a similarity of 0.1 or higher, keeping track of the maximum similarity match. Keep in mind that this runs *much* slower than the second method (described below), but is more thorough and requires no additional setup.

## Thesaurus ##

If you would like to create an almost instant-lookup thesaurus, as opposed to just running through and calculating the similarities for a single word, you should use `CreateThesaurus.java`. This requires some additional setup which can be completed by running

    python degree_sort.py

This performs three operations, detailed below (keep in mind file names will have to be changed due to repository size limitations).

* First, it reads in the entropies and indexes them by the second element of each pair (relation and second word), rather than by first word.

* Second, it reads in the files it just wrote and removes all pairs in which any of the involved words are not found in the dictionary provided by Mathematica.

* Finally, this new data is read in, and is sorted by degree distribution. The degree of a relation-to-second-word pair is the total number of "first words" to which the pair is attached. The earlier files are those arcs whose second element have degrees of one, and increases (the largest is around 40,000).

Now that these three steps have been performed, `CreateThesaurus.java` can be run. This takes as a command line argument two file numbers between 00 and 98, and uses the data in that range of files (including) to create a file in thesaurus_data with an appropriate name. If you would like to include all data for a specific range of degrees, run

    python find_degree_range.py

which takes as command line input the upper and lower bounds, and returns the file range that includes those bounds.

A file created from a range of 20 parsed files will be about 500MB, though it will vary depending on which 20 files were used. Keep in mind that the accuracy may decrease massively if fewer than the full 98 files are used (though 20 files is usually enough to get a good estimate). Because of the method of calculating mutual entropy and similarity, small changes in size of data can drastically alter results. Also note that the lower-numbered files provide less information, and therefore data based on only lower numbers tends to be extremely erratic (but they are much faster to parse through).

You can now run `Thesaurus.java`. This takes as a command line input the same two files for the range (so it knows what file to search for). After a quick read through of the data, you can give the program any word (with the attached POS tag) and the program will output any word in its database whose similarity with the given word is above the calculated threshold. The threshold is calculated based on the number of file parsed; since it keeps a running total, the value for a "highly similar matching" is obviously going to be less than 0.1 if you didn't go through the entire database). Note that the threshold changes for different types of words (noun/verb/adjective) in order to provide a more appropriate matching. Also, only words of the same POS will be returned, as those were the only ones whose similarities were calculated in the first place. You can also give a threshold (in the form of a double) after the word to specify a particular threshold (for example, type "0" after the word to get a list of all ~400 matches).

# Creating Your Own Thesaurus #
---

