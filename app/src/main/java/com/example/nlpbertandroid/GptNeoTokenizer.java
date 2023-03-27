package com.example.nlpbertandroid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** Word piece tokenization to split a piece of text into its word pieces. */
public final class GptNeoTokenizer {
    private final Map<String, Integer> dic;

    private static final String UNKNOWN_TOKEN = "[UNK]"; // For unknown words.
    private static final int MAX_INPUTCHARS_PER_WORD = 200;

    public GptNeoTokenizer(Map<String, Integer> vocab) {
        dic = vocab;
    }

    /**
     * Tokenizes a piece of text into its word pieces. This uses a greedy longest-match-first
     * algorithm to perform tokenization using the given vocabulary. For example: input = "unaffable",
     * output = ["un", "aff", "able"]
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
                    String subStr = token.substring(start, end);
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
        System.out.println(outputTokens);
        return outputTokens;

    }

    public long[] finalTokenization(List<String> parts) {
        ArrayList<Long> tokenizedIntegerMessage = new ArrayList<Long>();
        for (String part : parts) {
            if (part.trim() != "") {
                int index = 0;
                if (dic.get(part) == null) {
                    index = 0;
                } else {
                    index = dic.get(part);
                }
                tokenizedIntegerMessage.add((long) index);
            }
        }
        for (int i=128-tokenizedIntegerMessage.size(); i>0; i--){
            tokenizedIntegerMessage.add(0L);
        }
        return tokenizedIntegerMessage.stream().mapToLong(i -> i).toArray();
    }

    /* Runs basic whitespace cleaning and splitting on a piece of text. For every word after the first one,
    * add Ġ to it's beginning. So every 2nd word onwards, Ġ is added to the beginning of the word*/
    private List<String> whitespaceTokenize(String text) {
        if (text == null) {
            throw new NullPointerException("The input String is null.");
        }
        List<String> whiteSpaceTokens = Arrays.asList(text.split(" "));
        List<String> whiteSpaceTokens2 = new ArrayList<String>();
        for (int i=0;i<whiteSpaceTokens.size();i++){
            if(i==0 && text.charAt(0)!=' '){
                whiteSpaceTokens2.add(whiteSpaceTokens.get(0));
            } else {
                whiteSpaceTokens2.add(i, "Ġ" + whiteSpaceTokens.get(i));
            }
        }
        return whiteSpaceTokens2;
    }

}
