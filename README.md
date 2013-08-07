# Introduction #
---

This README describes how the Thesaurus was produced. Much of the work is based off of [Automatic Retrieval and Clustering of Similar Words](http://www.computing.dcu.ie/~ebicici/Week3/acl98.pdf), along with some ideas from [A Dataset of Syntactic-Ngrams over Time from a Very Large Corpus of English Books](http://commondatastorage.googleapis.com/books/syntactic-ngrams/syntngrams.final.pdf). Instructions on how to produce your own Thesaurus using a different corpus can be found under [**Creating Your Own Thesaurus**](https://github.com/erosenfeld/Thesaurus#creating-your-own-thesaurus) (be warned that for any appreciably-sized dataset this takes several hundred hours of runtime using several hundred gigs of both hard-drive space and RAM). Instructions on using the provided thesaurus can be found under [**How to Implement**](https://github.com/erosenfeld/Thesaurus#how-to-implement).

Due to the data being split into 99 files (arcs.00-of-98.txt through arcs.98-of-98.txt), "\*\*" will be used to indicate all files, 00 through 98.

The initial files required are the Syntactic N-grams Over Time provided by Google Research. The overview can be found at <http://googleresearch.blogspot.com/2013/05/syntactic-ngrams-over-time.html>, which links to the actual download at <http://commondatastorage.googleapis.com/books/syntactic-ngrams/index.html>. Only the standard Arcs (files 00-98) from the subsection *English 1 Million* were used. While the files themselves are not included, an md5 checksum of each of the gzipped arc files and each file's word count is located in eras/arcs.

The arc files provided by Google were parsed into a dictionary, where each word hashed to its number of occurrences in four different eras, as well as the total count of the word. The four eras were pre-1920, 1920-1950, 1950-2000, and post-2000 (roughly divided by WWI, WWII, and the new millennium). For some reason, the Google files include a few items that are marked as appearing zero times total, so the arc files had to be purged of all such occurrences.

The words were then sorted by magnitude, based on their number of occurrences in each of the eras, and written to four different files (one for each era) Using Mathematica, each of the four files were graphed on a plot of word frequency rank by actual word frequency, shown below.

![pre1920](https://s3.amazonaws.com/erosenfeld.github.com/listlogplot%5B1%5D.jpg)
![1920-1950](https://s3.amazonaws.com/erosenfeld.github.com/listlogplot%5B2%5D.jpg)
![1950-2000](https://s3.amazonaws.com/erosenfeld.github.com/listlogplot%5B3%5D.jpg)
![post2000](https://s3.amazonaws.com/erosenfeld.github.com/listlogplot%5B4%5D.jpg)

At this point it was decided that only the top 250,000 words should be used, and only from eras 2 and 3 (otherwise subsequent scripts would take far too long to run). Thus, the two eras were consolidated, and the arc files were purged of any words that were not present among the most frequent 250K.

Next, all the arcs were rewritten in the form [word1, POS tag, relation, word2, POS tag, count]. These arcs also had any occurrence of particular non-alphanumeric characters (such as ' / ',' \ ',' . ', etc.) removed; the existence of these characters in some of the words was attributed to mistakes made by Google's OCR system, as this is where a large portion of the data came from.

Each word and its Part of Speech (POS) tag was then replaced with a token. Each token is simply the word, with "/A", "/N", or "/V" appended to the end, depending on whether the part of speech indicates the word is used as an adjective, noun, or verb, respectively. Another opportunity was taken to cleanse the dataset, where any words whose POS tags were not indicative of either a noun, verb, or adjective were removed from consideration.

The thesaurus needs a list of dictionary words. I used the dictionary provided by Mathematica via `DictionaryLookup[]`.

Following these steps, a series of Python scripts were used to further condense and format the data, at which point the thesaurus was complete. A full step-by-step procedure can be found under [**Creating Your Own Thesaurus**](https://github.com/erosenfeld/Thesaurus#creating-your-own-thesaurus).

# How to Implement #
---

There are two options for implementing the thesaurus, each with upsides and downsides. The first approach is slower but more thorough, while the second approach requires additional setup, but rewards you with a practically instant lookup time.

## Similarity ##

#### SimilarityTester ####

To get a feel for the meaning of the "similarity" value, you can run

    java SimilarityTester

Note that this program requires about 4GB of memory, so you may have to increase the Java heap space with the `-Xmx` flag. This will read in the 98 entropy files and prompt you for two words. Make sure to append "/N", "/A", or "/V" to the words, as the thesaurus treats different POS tags as entirely different tokens. Since similarity is symmetric, the order of the words doesn't matter. If a word doesn't appear in the corpus, the value returned will be 0.0.

Running this will give you a feel for the kind of data to expect. As a general rule, a similarity of 0.1 between nouns indicates a strong correlation, while verbs and adjectives will often require a similarity of 0.15 or even higher before they can be considered "linked". Numerically, similarity is an estimate of the probability that one word can replace another while maintaining syntactic integrity (that is, the entropy of the sentence). Since the similarity between `train/N` and `bus/N` is 0.15184711204531073, we can deduce that approximately 15% of the time, one can be substituted for the other and the text will not change drastically information content or meaning.

Keep in mind that opposites still have about the same entropy, and from a parser's perspective they have similar meanings since they fall into the exact same category. It follows that while some words may intuitively have no similarity--such as `happy/A` and `sad/A`--they actually are very similar; for a syntactical parser, "I feel happy" and "I feel sad" are practically identical, as the two words will be parsed and linked to other words similarly.

Lower similarities indicate either no connection or a little connection; a high similarity means that either the two words are actual synonyms, or that one word can often be replaced with another without a major syntax change (e.g. "I took the train to work" vs. "I took the bus to work").  Some examples are listed below:

```
car/N - automobile/N:	0.2648248259050074
car/N - bicycle/N:		0.12565174064582982
car/N - flower/N:		0.03820317325885916

compute/V - calculate/V:	0.5653742891117509
compute/V - multiply/V:		0.18779126065230747
compute/V - extricate/V:	0.02329896232188951

happy/A - cheerful/A:		0.2585512615001065
happy/A - sad/A:			0.16087481318121205
happy/A - melodramatic/A:	0.017074012637758835
```

While the program accepts two words with different POS tags, they rarely have a high similarity because of how "similarity" is defined and calculated. However, the option is there.

#### Running the Program ####

Now that you have a good feel for similarity, you can run

    java Similarity

Again, this will require about 4GB of memory, so use the `-Xmx` flag if necessary. This program takes in a single word and prints out all matches above the similarity threshold, keeping track of the maximum similarity match. Keep in mind that this runs *much* slower than the second method (described below), but requires no additional setup and is more thorough (the faster version only keeps a record of the first ~400 matches; any more would be space-prohibitive). Here is an example of its usage:

```
elan-mp:java elan$ java -Xmx4g Similarity

Reading:
 00 / 01 / 02 / 03 / 04 / 05 / 06 / 07 / 08 / 09 /
 10 / 11 / 12 / 13 / 14 / 15 / 16 / 17 / 18 / 19 /
 20 / 21 / 22 / 23 / 24 / 25 / 26 / 27 / 28 / 29 /
 30 / 31 / 32 / 33 / 34 / 35 / 36 / 37 / 38 / 39 /
 40 / 41 / 42 / 43 / 44 / 45 / 46 / 47 / 48 / 49 /
 50 / 51 / 52 / 53 / 54 / 55 / 56 / 57 / 58 / 59 /
 60 / 61 / 62 / 63 / 64 / 65 / 66 / 67 / 68 / 69 /
 70 / 71 / 72 / 73 / 74 / 75 / 76 / 77 / 78 / 79 /
 80 / 81 / 82 / 83 / 84 / 85 / 86 / 87 / 88 / 89 /
 90 / 91 / 92 / 93 / 94 / 95 / 96 / 97 / 98 /
Database created. Size: 60986967
Total # of words: 296964

Enter word: calculus/N
calculus/N found at index 38585

algebra/N - 0.21553904380128924
arithmetic/N - 0.12226048592758765
concretions/N - 0.10856868713046178
equations/N - 0.10930502523137159
formulation/N - 0.10733756077285754
geometry/N - 0.12814478043616603
logic/N - 0.10569228205816365
mathematics/N - 0.12151039139750312
notation/N - 0.12965385517465147
semantics/N - 0.10428999093776702
trigonometry/N - 0.10925969942359745

Max is algebra/N with 0.21553904380128924
```

## Thesaurus ##

If you would like to create an almost instant-lookup thesaurus, as opposed to just running through and calculating the similarities for a single word, you should use `CreateThesaurus.java`. This requires some additional setup which can be completed by running

    python degree_sort.py

This performs three operations, detailed below.

* First, it reads in the [word1 relation word2 entropy] sets and indexes them by the second element of each pair (relation and word2), rather than by word1.

* Second, it reads in the files it just wrote and removes all pairs in which any of the involved words are not found in the dictionary provided by Mathematica.

* Finally, this new data is read in, and is sorted by degree distribution. The degree of a [relation-word2] pair is the total number of [word1] to which the pair is attached. The earlier files are those arcs whose second element have degrees of one, and increases as you go through the files (the largest is around 40,000).

Now that these three steps have been performed, you can run

    java CreateThesaurus <file1> <file2>

Again, this will probably require an increase in the maximum Java heap size; the size required depends on how many files/which files you use. This takes as a command line argument two file numbers between 00 and 98, and uses the data in that range of files (including) to create a file in thesaurus_data with an appropriate name. If you would like to include all data for a specific range of degrees, run

    python find_degree_range.py <degree-lower-bound> <degree-upper-bound>

which takes as command line input the upper and lower bounds, and returns the file range that includes those bounds.

A file created from a range of 20 parsed files will be about 500MB and can take anywhere from a dozen to several dozen hours, depending on which 20 files are used. Keep in mind that the accuracy will decrease if fewer than the full 98 files are used (though 20 files is usually enough to get a good estimate). Because of the method of calculating mutual entropy and similarity, small changes in the size of the data can drastically alter results. Also note that the lower-numbered files provide less information, and therefore data based on only lower numbers tend to be erratic and inaccurate (but they are much faster to parse through).

You can now run

    java Thesaurus <file1> <file2>

This takes as a command line input the same two files for the range (so it knows what file to search for). After a quick read through the data, you can give the program any word (with the attached POS tag) and the program will output any word in its database whose similarity with the given word is above the calculated threshold. The threshold is calculated based on the number of files parsed; since it keeps a running total, the value for a "highly similar matching" is obviously going to be less than 0.1 if you didn't go through the entire database. Using some predetermined boundaries, [a graph was derived](https://s3.amazonaws.com/erosenfeld.github.com/noun-similarity-threshold.png), representing minimum required similarity as a function of number of files parsed.

Note that the threshold changes for different types of words (noun/verb/adjective) in order to provide a more appropriate matching. Also, only words of the same POS will be returned, as those were the only ones whose similarities were calculated in the first place. An example is shown below:

```
elan-mp:java elan$ java -Xmx4g Thesaurus 0 20

Reading...............................................................

Enter word: calculus/N

calculi/N - 0.01942499467852897
algebra/N - 0.01399100278066545
geometry/N - 0.008408733712801793
arithmetic/N - 0.006997741489690139
concretions/N - 0.006980555983174796
logic/N - 0.006182549045309564
integral/N - 0.005629090815530466
gallstone/N - 0.004993704825605044
notation/N - 0.004267997942994417
equations/N - 0.004212733060622694
plaque/N - 0.0038693567393676544
caries/N - 0.0038497330362240547
tumor/N - 0.0037901328999775438
multiplication/N - 0.00361934387862254
tartar/N - 0.003603262298334568
physics/N - 0.0035871388101105356
predicates/N - 0.0034127363121227387
bladder/N - 0.003280695597132573
goldman/N - 0.0031363630550712528
saliva/N - 0.00312613588163945
gravel/N - 0.0030887793139308217
```

You can also give a threshold (in the form of a double) after the word to specify a particular threshold (for example, type "0" after the word to get a list of all ~400 matches).

# Creating Your Own Thesaurus #
---

This section provides step-by-step instructions on preparing the files necessary to create your own thesaurus using a different corpus. The arc files must be parsed and formatted in the method described by Google Research in their paper (see [the **Introduction**](https://github.com/erosenfeld/Thesaurus#introduction)). A few *very* simple scripts are left out (this was initially intended to be a solo project), but can easily be reproduced. Assume a base directory of Thesaurus/, with all subsequent subdirectories explicitly indicated where necessary for file distinction. All code was written in `Python 2.7.2` and `java version "1.6.0_51"`.

**While all this computation could have been condensed into a single script, there are likely to be a lot of individual requirements, time and space constraints, etc. If this were all done at once, it would require hundreds of gigs of RAM and hard-drive space, as well as several hundred hours of computation time. As a result, the computation has been kept separated as it was done originally.**

## Setup ##

Download the arc files to the `arcs\` directory, then navigate to `eras/` and run `python eras.py`. This will sort and format the data as described above, and write it to `eras/eras.\*\*.txt`. You should parse through these files and ensure that there are no occurrences of words that are marked as appearing 0 times (they do exist). This data was specifically written to `arcs-fixed-.\*\*-\*\*.txt`, but it is not necessary that you do this. Next, consolidate these files into one file called era-counts.txt. The reason for this intermediate step was solely a matter of memory.

Next, sort the data in `era-counts.txt` by each of the four eras (columns 2-5) and write these lists to `era-countsX.txt`, where 'X' represents the number of the era by which the list is sorted (0-3).

Go back into the home directory and run `python parser.py`. This will create a file called `total-word-counts.txt`, which contains an alphabetical list of each word, followed by its frequency. You should then sort the list by frequency, and write the output to `total-word-counts-sorted.txt`.

Next, run `python relcount.py`, which will count the total occurrences of each relation and write them to stdout. You should save this data in a file called `rel-counts-total.txt`.

Because at this point only the top 250,000 words were necessary (and indeed, any more would've drastically increased runtime for all scripts), `era2-counts.txt` and `era3-counts.txt` were combined, and only the words that were also among the first 250,000 in `total-word-counts-sorted.txt` were written to `arcs-top-250K/era3-top-250K.txt`. Finally, using `total-word-counts-sorted.txt`, the arc files were purged of any words that were not among the most frequent 250K, and these new arc files were written to `arcs/arcs.top.**.txt`.

Navigate to `arcs-top-250K` and run `python parser.py`. This will read in the newly-created arcs and format them, writing them to `p-arcs/top/p-arcs.top.**.txt`. Next run `python purge.py`, which will purge these files of particularly troublesome non-alphanumeric characters; these are written to `p-arcs/top/p-arcs.**.txt`.

Make sure you are in the `p-arcs/` directory, the run `python tag.py`. This will replace each word and its POS tag with a token; a token is simply the word followed by a "/N", "/V", or "/A" depending on the word's part of speech. This is written to `tagged/p-arcs.tagged.**.txt`, but must be cleansed of all occurrences of "/X", which is written when a word's POS tag matches none of the three categories. Write this data to `purged/p-arcs.purged.**.txt`.

Now you need a dictionary, because much of the data is comprised of junk words as a result of OCR error. I printed out the dictionary from Mathematica to `arcs-top-250L/dictionary.txt`, though you can get yours elsewhere. Run `python dict_format.py` to format it and write it to `dict.txt`, or write your own formatter if you have a differently formatted list to begin with.

## Final Formatting ##

1. While still in the `p-arcs/` directory, run `python extend.py`. This extends the files so that no word (no WORD, rather than no TOKEN) extends between two files. This is written to `purged/p-arcs.f-purged.**.txt`.
2. Run `python sorter.py`, which will sort each group of words alphabetically. This will incidentally also ensure that the entire list is sorted by token. This data is written to `purged/p-arcs.purged-sorted.**.txt`.
3. Because of the abstraction of POS tags (i.e., several types of nouns, verbs, adjectives), there are now duplicates. Type `python combine.py`, which will consolidate the duplicates and write the files out to `final/p-arcs.ps-final.**.txt`.
4. You should now navigate to `arcs-top-250K` and run `python normalize.py`, which will write the newest list of words in alphabetical order to `word-values.txt`.
5. Go the `p-arcs/` directory and type `python p-ind.py` to create `final/final-indices.txt`, which has the starting indices for each token.
6. Now run `python relcount.py` (make sure you are in the `p-arcs/` directory!), to count the frequency of each relation. This is written to `p-arcs/relcounts.txt`.
7. Next, type `python reverse.py`. This takes each [word1, relation, word2, count] tuple and rewrites it as [word2, relation, word1, count]. This is so that finding matches of the pattern || \* r w' || doesn't take obscenely long. These files are written to `final/reversed/p-arcs.reversed.**.txt`.
8. Again, navigate to `arcs-top-250K` and type `python converter.py`. This uses several of the files you just produced and converts each arc into a single 64-bit long. Any occurrence of an 'X' indicates that one or more of the words in the arc are not in the top 250K, so it should be disregarded. This is written to `long-arcs/final/long-arcs.purged.**.txt`.
9. Now we need to repeat this consolidation for the newly created reversed files. Repeat steps 1, 5, and 8, but use the files with '-r' appended to their names, for "reversed". This new data is written to `final/final-indices-r.txt` and `long-arcs/final/reversed/long-arcs.reversed.**.txt`.
10. Now that all these files are prepared, you can navigate to the `arcs-top-250K/java` folder and run `java Stats.java` (which will take a LONG TIME unless it is segmented to run on several cores). This will use the `long-arcs/final/long-arcs.purged` files and replace the count in each [word1, relation, word2, count] tuple with the two words' calculated mutual entropy. It will only write the mutual information if it is positive, so if there is a word with no pairs for which the mutual information is positive, the program will simply write an "X" to indicate this. This replaced data is written to `long-arcs/final/replaced/long-arcs.replaced.**.txt`.
11. Switching back to python, run arcs-top-250K/precalculate.py, which reads in the long-arc.replaced files produced in step 10, and appends to each word's mutual information list the sum of all the entropies. This is because the calculation of any two words' similarities requires the sum of the entropies for each word, so rather than calculating it dynamically we precalculate it to save time. These files are written to `entropies/entropies.**.txt`.

Now that you have the entropy files, you can use the thesaurus by running `java Similarity`. Optionally, you can do further setup to create an instant-lookup thesaurus. Both methods are described in detail under [**How to Implement**](https://github.com/erosenfeld/Thesaurus#how-to-implement).