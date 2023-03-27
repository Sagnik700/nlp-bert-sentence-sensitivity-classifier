package com.example.nlpbertandroid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** Word piece tokenization to split a piece of text into its word pieces. */
public final class BertTokenizer {
    private final Map<String, Integer> dic;

    private static final String UNKNOWN_TOKEN = "[UNK]"; // For unknown words.
    private static final int MAX_INPUTCHARS_PER_WORD = 200;

    public BertTokenizer(Map<String, Integer> vocab) {
        dic = vocab;
    }

    /**
     * Tokenizes a piece of text into its word pieces. This uses a greedy longest-match-first
     * algorithm to perform tokenization using the given vocabulary. For example: input = "unaffable",
     * output = ["un", "##aff", "##able"].
     *
     * @param text: A single token or whitespace separated tokens.
     * @return A list of wordpiece tokens.
     */
    public List<String> wordTokenization(String text) {
        if (text == null) {
            throw new NullPointerException("The input String is null.");
        }

        List<String> outputTokens = new ArrayList<>();
        for (String token : whitespaceTokenize(text)) {

            if (token.length() > MAX_INPUTCHARS_PER_WORD) {
                outputTokens.add(UNKNOWN_TOKEN);
                continue;
            }

            boolean isBad = false; // Mark if a word cannot be tokenized into known subwords.
            int start = 0;
            List<String> subTokens = new ArrayList<>();

            while (start < token.length()) {
                String curSubStr = "";

                int end = token.length(); // Longer substring matches first.
                while (start < end) {
                    String subStr =
                            (start == 0) ? token.substring(start, end) : "##" + token.substring(start, end);
                    if (dic.containsKey(subStr)) {
                        curSubStr = subStr;
                        break;
                    }
                    end--;
                }

                // The word doesn't contain any known subwords.
                if ("".equals(curSubStr)) {
                    isBad = true;
                    break;
                }

                // curSubStr is the longeset subword that can be found.
                subTokens.add(curSubStr);

                // Proceed to tokenize the resident string.
                start = end;
            }

            if (isBad) {
                outputTokens.add(UNKNOWN_TOKEN);
            } else {
                outputTokens.addAll(subTokens);
            }
        }

        return outputTokens;
    }


    public int[] finalTokenization(List<String> parts) {
        ArrayList<Integer> tokenizedIntegerMessage = new ArrayList<Integer>();
        tokenizedIntegerMessage.add(101);
        for (String part : parts) {
            if (part.trim() != "") {
                int index = 0;
                if (dic.get(part.toLowerCase()) == null) {
                    index = 0;
                } else {
                    index = dic.get(part.toLowerCase());
                }
                tokenizedIntegerMessage.add(index);
            }
        }
        tokenizedIntegerMessage.add(102);
        for (int i=157-tokenizedIntegerMessage.size(); i>0; i--){
            tokenizedIntegerMessage.add(0);
        }
        return tokenizedIntegerMessage.stream().mapToInt(i -> i).toArray();
    }

    public String preTokenization(String message) {
        message = message.toLowerCase();
        ArrayList<String> tokenizedStringMessage = new ArrayList<String>();
        ArrayList<Integer> tokenizedIntegerMessage = new ArrayList<Integer>();
        int ind1 = 0, ind2 = 0;
        while (ind2 < message.length()) {
            if (message.substring(ind1, ind2 + 1).matches("[a-zA-Z]+")) {
                ind2++;
            } else if (!((message.substring(ind1, ind2)).trim()).equals("")) {
                tokenizedStringMessage.add(message.substring(ind1, ind2));
                ind1 = ind2;
                ind2++;
            } else if (ind1 == ind2) {
                ind2++;
            } else {
                ind1++;
                ind2++;
            }
        }
        tokenizedStringMessage.add(message.substring(ind1, ind2));
        tokenizedIntegerMessage.add(101);

        String[] parts = new String[tokenizedStringMessage.size()];
        parts = tokenizedStringMessage.toArray(parts);
        //System.out.println("parts: " + String.join(" ", parts));
        return String.join(" ", parts);
    }

    /* Runs basic whitespace cleaning and splitting on a piece of text. */
    private List<String> whitespaceTokenize(String text) {
        if (text == null) {
            throw new NullPointerException("The input String is null.");
        }
        return Arrays.asList(text.split(" "));
    }

}
