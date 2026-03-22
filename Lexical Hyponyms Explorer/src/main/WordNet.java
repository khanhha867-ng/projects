package main;

import edu.princeton.cs.algs4.In;
import ngrams.NGramMap;
import ngrams.TimeSeries;

import java.util.*;

public class WordNet {
    private Graph graph;
    private Map<Integer, Set<String>> idToWords;
    private Map<String, Set<Integer>> wordToIDs;
    private Map<Integer, Set<Integer>> idToIDs;

    public WordNet(String synsetFile, String hyponymFile) {
        this.idToWords = new HashMap<>();
        this.idToIDs = new HashMap<>();
        this.wordToIDs = new HashMap<>();
        this.graph = new Graph();
        parseSynsets(synsetFile);
        parseHyponyms(hyponymFile);
    }

    public void reload(String synsetFile, String hyponymFile) {
        this.idToWords.clear();
        this.wordToIDs.clear();
        this.idToIDs.clear();
        this.graph = new Graph();
        parseSynsets(synsetFile);
        parseHyponyms(hyponymFile);
    }


    public void parseSynsets(String synsetFile) {
        In s = new In(synsetFile);
        while (!s.isEmpty()) {
            String nextLine = s.readLine();
            String[] splitLine = nextLine.split(",");
            int id = Integer.parseInt(splitLine[0]);
            String word = splitLine[1];
            graph.addNode(id);

            String[] subWords = word.split(" ");
            idToWords.putIfAbsent(id, new HashSet<>()); // Create an ID key

            for (String subWord : subWords) {
                wordToIDs.putIfAbsent(subWord, new HashSet<>()); // Create a word key
                wordToIDs.get(subWord).add(id); // Add ID for a word
                idToWords.get(id).add(subWord); // Add each word to the same ID
            }
        }
    }

    public void parseHyponyms(String hyponymFile) {
        In h = new In(hyponymFile);
        while (!h.isEmpty()) {
            String nextLine = h.readLine();
            String[] splitLine = nextLine.split(",");
            Integer id = Integer.parseInt(splitLine[0]);
            idToIDs.put(id, new HashSet<>()); // Create an ID key
            for (int i = 1; i < splitLine.length; i++) {
                int child = Integer.parseInt(splitLine[i]);
                graph.addEdge(id, child); // Add edges
                idToIDs.get(id).add(child); // Add ID hyponyms
            }
        }
    }

    public List<String> convertIDsToHyponyms(Set<Integer> hyponymsIDs) {
        if (hyponymsIDs == null) {
            return Collections.emptyList();
        }
        Set<String> hyponyms = new HashSet<>();
        for (int id : hyponymsIDs) {
            Set<String> words = idToWords.get(id);
            if (words != null) {
                hyponyms.addAll(words);
            }
        }
        if (hyponyms.isEmpty()) {
            return Collections.emptyList();
        }
        return hyponyms.stream().sorted().toList();
    }

    public Set<Integer> getHyponymIDs(String word) {
        if (word == null) {
            return Collections.emptySet();
        }
        Set<Integer> parentIDs = wordToIDs.get(word);
        if (parentIDs == null) {
            return Collections.emptySet();
        }
        Set<Integer> hyponymsIDs = new HashSet<>();
        for (int parentID : parentIDs) {
            Set<Integer> reachableIDs = graph.getReachableNodes(parentID);
            if (reachableIDs != null) {
                hyponymsIDs.addAll(reachableIDs);
            }
        }
        if (hyponymsIDs.isEmpty()) {
            return Collections.emptySet();
        }
        return hyponymsIDs;
    }

    public List<String> convertWordsToHyponyms(List<String> words) {
        if (words.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Integer> commonIDs = null;
        for (String word : words) {
            Set<Integer> hyponymIDs = getHyponymIDs(word);
            if (commonIDs == null) {
                commonIDs = new HashSet<>(hyponymIDs);
            }
            commonIDs.retainAll(hyponymIDs);
            if (commonIDs.isEmpty()) {
                return Collections.emptyList();
            }
        }
        return convertIDsToHyponyms(commonIDs);
    }

    public List<String> getHyponyms(List<String> words, Integer startYear, Integer endYear, Integer k, NGramMap ngm) {
        // Default years if not provided
        if (startYear == null) startYear = 1900;
        if (endYear == null) endYear = 2020;

        // Convert words to hyponyms
        List<String> hyponyms = convertWordsToHyponyms(words);
        if (hyponyms.isEmpty()) {
            return Collections.emptyList();
        }

        // Map to store hyponyms by their total counts
        Map<Double, Set<String>> popularCount = new HashMap<>();
        for (String hyponym : hyponyms) {
            TimeSeries ts;
            if (k == null || k == 0) {
                // Ignore time frame if k is not specified
                return hyponyms.stream().sorted().toList();
            } else {
                // Use specified time frame
                ts = ngm.countHistory(hyponym, startYear, endYear);
            }

            double totalCount = 0.0;
            for (int year : ts.keySet()) {
                totalCount += ts.get(year);
            }

            // Exclude entries with total count 0 in the specified time frame
            if ((k != null && k != 0) && totalCount == 0.0) {
                continue;
            }

            popularCount.putIfAbsent(totalCount, new HashSet<>());
            popularCount.get(totalCount).add(hyponym);
        }

        // Sort the counts in descending order
        List<Double> sortedCount = new ArrayList<>(popularCount.keySet());
        sortedCount.sort(Collections.reverseOrder());

        // Collect hyponyms based on sorted counts
        List<String> popularHyponyms = new ArrayList<>();
        for (double count : sortedCount) {
            List<String> wordsEachCount = new ArrayList<>(popularCount.get(count));
            Collections.sort(wordsEachCount);
            popularHyponyms.addAll(wordsEachCount);
        }

        // If k is null or 0, return all sorted hyponyms
        if (k > popularHyponyms.size()) {
            return popularHyponyms.stream().sorted().toList();
        }

        // Extract top k hyponyms
        List<String> topKHyponyms = popularHyponyms.subList(0, k);
        return topKHyponyms.stream().sorted().toList();
    }

    public List<String> getHyponyms(List<String> words, int k, NGramMap ngm) {
        List<String> hyponyms = convertWordsToHyponyms(words);
        List<String> topKHyponyms = hyponyms.subList(0, k);
        return topKHyponyms.stream().sorted().toList();
    }
}
