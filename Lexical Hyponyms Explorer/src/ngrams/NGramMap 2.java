//package ngrams;
//
//import edu.princeton.cs.algs4.In;
//
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Map;
//
//import static ngrams.TimeSeries.MAX_YEAR;
//import static ngrams.TimeSeries.MIN_YEAR;
//
///**
// * An object that provides utility methods for making queries on the
// * Google NGrams dataset (or a subset thereof).
// *
// * An NGramMap stores pertinent data from a "words file" and a "counts
// * file". It is not a map in the strict sense, but it does provide additional
// * functionality.
// *
// * @author Josh Hug
// */
//public class NGramMap {
//    private Map<String, TimeSeries> wordMap;
//    private TimeSeries totalCount;
//
//    /**
//     * Constructs an NGramMap from WORDSFILENAME and COUNTSFILENAME.
//     */
//    public NGramMap(String wordsFilename, String countsFilename) {
//        wordMap = new HashMap<>();
//        totalCount = new TimeSeries();
//        In wordsFile = new In(wordsFilename);
//        while (wordsFile.hasNextLine()) {
//            String nextLine = wordsFile.readLine();
//            String[] splitLine = nextLine.split("\t");
//            String word = splitLine[0];
//            int year = Integer.parseInt(splitLine[1]);
//            double count = Double.parseDouble(splitLine[2]);
//            if (!wordMap.containsKey(word)) {
//                wordMap.put(word, new TimeSeries());
//            }
//            TimeSeries ts = wordMap.get(word);
//            ts.put(year, count);
//        }
//
//        In countsFile = new In(countsFilename);
//        while (countsFile.hasNextLine()) {
//            String nextLine = countsFile.readLine();
//            String[] splitLine = nextLine.split(",");
//            int year = Integer.parseInt(splitLine[0]);
//            double total = Double.parseDouble(splitLine[1]);
//            totalCount.put(year, total);
//        }
//    }
//
//    /**
//     * Provides the history of WORD between STARTYEAR and ENDYEAR, inclusive of both ends. The
//     * returned TimeSeries should be a copy, not a link to this NGramMap's TimeSeries. In other
//     * words, changes made to the object returned by this function should not also affect the
//     * NGramMap. This is also known as a "defensive copy". If the word is not in the data files,
//     * returns an empty TimeSeries.
//     */
//    public TimeSeries countHistory(String word, int startYear, int endYear) {
//        if (wordMap.containsKey(word)) {
//            TimeSeries ts = wordMap.get(word);
//            return new TimeSeries(ts, startYear, endYear);
//        } else {
//            return new TimeSeries();
//        }
//    }
//
//    /**
//     * Provides the history of WORD. The returned TimeSeries should be a copy, not a link to this
//     * NGramMap's TimeSeries. In other words, changes made to the object returned by this function
//     * should not also affect the NGramMap. This is also known as a "defensive copy". If the word
//     * is not in the data files, returns an empty TimeSeries.
//     */
//    public TimeSeries countHistory(String word) {
//        return countHistory(word, MIN_YEAR, MAX_YEAR);
//    }
//
//    /**
//     * Returns a defensive copy of the total number of words recorded per year in all volumes.
//     */
//    public TimeSeries totalCountHistory() {
//        return totalCount;
//    }
//
//    /**
//     * Provides a TimeSeries containing the relative frequency per year of WORD between STARTYEAR
//     * and ENDYEAR, inclusive of both ends. If the word is not in the data files, returns an empty
//     * TimeSeries.
//     */
//    public TimeSeries weightHistory(String word, int startYear, int endYear) {
//        TimeSeries wordHistory = countHistory(word, startYear, endYear);
//        TimeSeries totalHistory = totalCountHistory();
//        TimeSeries newTS = new TimeSeries();
//        for (Integer year : wordHistory.keySet()) {
//            if (totalHistory.containsKey(year) && totalHistory.get(year) > 0) {
//                double weight = wordHistory.get(year) / totalHistory.get(year);
//                newTS.put(year, weight);
//            }
//        }
//        return newTS;
//    }
//
//    /**
//     * Provides a TimeSeries containing the relative frequency per year of WORD compared to all
//     * words recorded in that year. If the word is not in the data files, returns an empty
//     * TimeSeries.
//     */
//    public TimeSeries weightHistory(String word) {
//        return weightHistory(word, MIN_YEAR, MAX_YEAR);
//    }
//
//    /**
//     * Provides the summed relative frequency per year of all words in WORDS between STARTYEAR and
//     * ENDYEAR, inclusive of both ends. If a word does not exist in this time frame, ignore it
//     * rather than throwing an exception.
//     */
//    public TimeSeries summedWeightHistory(Collection<String> words,
//                                          int startYear, int endYear) {
//        TimeSeries summed = new TimeSeries();
//        for (String word : words) {
//            TimeSeries wordHistory = weightHistory(word, startYear, endYear);
//            summed = summed.plus(wordHistory);
//        }
//        return summed;
//    }
//
//    /**
//     * Returns the summed relative frequency per year of all words in WORDS. If a word does not
//     * exist in this time frame, ignore it rather than throwing an exception.
//     */
//    public TimeSeries summedWeightHistory(Collection<String> words) {
//        return summedWeightHistory(words, MIN_YEAR, MAX_YEAR);
//    }
//}
