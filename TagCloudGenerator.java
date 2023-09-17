import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A program that generates a tag cloud with the specified amount of words from
 * a given input text.
 *
 *
 * A program that reads a text file as input and outputs an HTML file containing
 * a table of every word in the file and the number of times each word appears.
 * In this program, a "word" is a maximal string of alphabetical letter
 * characters or digits not present in the defined separator set, and case is
 * ignored when counting word occurrence--meaning, if "And" is one word found in
 * the file and "and" is another word found in the file, the program considers
 * these the same word and counts "And" as occurring 2 times. As briefly noted
 * above, strings of digits, whether they be decimal digits or Roman numerals,
 * are also counted as "words" in this case.
 *
 * @author Emily Allegretti, Emma Ryan
 *
 */
public final class TagCloudGenerator {

    /**
     *
     * Nested class to create new Pair objects out of Map.Entries.
     *
     */
    private static class Pair {
        /**
         * Data member: the key of the Pair is a String.
         */
        private String key;
        /**
         * Data member: the value of the Pair is an Integer.
         */
        private Integer value;

        /**
         * Constructor. Creates a Pair from a given String (the key) and a given
         * Integer (the value).
         *
         * @param s
         *            the String that is to be the key of the Pair
         * @param i
         *            the Integer that is to be the value of the pair
         */
        Pair(String s, Integer i) {
            this.key = s;
            this.value = i;
        }
    }

    /**
     * Private members.
     */

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private TagCloudGenerator() {
        //no code needed here
    }

    /**
     * Compare integer values in Pairs greatest - least.
     */
    private static class PairGT implements Comparator<Pair> {
        @Override
        public int compare(Pair o1, Pair o2) {
            return o2.value.compareTo(o1.value);
        }
    }

    /**
     * Writes the opening tags to the output HTML file, including title, header,
     * and CSS link tags.
     *
     * @param html
     *            the output stream for writing to the output file
     * @param inputFileName
     *            the name of the input file given by the user
     * @param cloudWordAmt
     *            the amount of words to be included in the tag cloud
     * @updates html
     *
     * @requires html.is_open and inputFileName /= null
     * @ensures html.content = [the opening tags of the output HTML file]
     */
    public static void outputHeader(PrintWriter html, String inputFileName,
            int cloudWordAmt) {

        /*
         * Print the page title, which takes the form of
         * "Top [cloudWordAmt] words in [inputFileName]"
         */
        html.println("<html><head><title>Top " + cloudWordAmt + " Words in "
                + inputFileName + "</title>");

        /*
         * Print the CSS link tags necessary for the tag cloud
         */
        html.println(
                "<link href= \"http://web.cse.ohio-state.edu/software/2231/"
                        + "web-sw2/assignments/projects/tag-cloud-generator/data/"
                        + "tagcloud.css\" rel=\"stylesheet\" type=\"text/css\">");
        html.println("<link href=\"tagcloud.css\" rel=\"stylesheet\""
                + " type=\"text/css\"></head>");

        /*
         * Print the header at the top of the page, which displays the name of
         * the input file
         */
        html.println("<body><h2>Top " + cloudWordAmt + " words in "
                + inputFileName + "</h2><hr>");

    }

    /**
     * Generates the string of separator characters in the given {@code String}
     * into a {@code Set<Character>}, and returns the set.
     *
     * @param separatorString
     *            the given {@code String} consisting of separator characters
     * @return the {@code Set} of separator characters given in {@code str}
     * @ensures generateSeparatorSet = entries(str)
     */
    public static Set<Character> generateSeparatorSet(String separatorString) {
        assert separatorString != null : "Violation of: separatorString is not null";

        /*
         * Create return Set that will contain the separator characters
         */
        Set<Character> separators = new HashSet<>();

        /*
         * Initialize loop variable that will store each character in
         * separatorString
         */
        char letter;
        for (int i = 0; i < separatorString.length(); i++) {
            /*
             * Iterate through every character in separatorString and add each
             * one to the separator Set (if it is not already in the Set)
             */
            letter = separatorString.charAt(i);
            if (!separators.contains(letter)) {
                separators.add(letter);
            }
        }

        return separators;
    }

    /**
     * Reads the entire input file and returns a {@code SortedMap} of every word
     * present in the file and their respective occurrence counts. In this
     * program, a "word" is a string of alphabetical letter characters or digits
     * not present in {@code separators} , and case is ignored when counting
     * word occurrence --meaning, if "And" is one word found in the file and
     * "and" is another word found in the file, the program considers these as
     * the same word and counts "and" as occurring 2 times. If there is nothing
     * to be read in the file, an empty Map is returned. As briefly noted above,
     * digit strings are also counted as words.
     *
     *
     * @param file
     *            the input stream for reading the input file
     * @param separators
     *            the set of "separator" characters that will be used to
     *            determine what is a "word" in the file and what isn't
     * @updates file
     *
     * @return a {@code SortedMap} of each word present in the file and the
     *         corresponding count of times each one occurs
     *
     * @requires file.ready and separators /= null
     *
     * @ensures file.content = <> and parseWordsInFile = [word -> word count map
     *          from input file]
     */
    public static SortedMap<String, Integer> parseWordsInFile(
            BufferedReader file, Set<Character> separators) {

        /*
         * Create return Map that will hold words and word counts
         */
        SortedMap<String, Integer> words = new TreeMap<>();

        /*
         * Repeatedly read individual lines from input file until end of
         * filestream has been reached
         */
        try {
            String s = file.readLine();
            while (s != null) {

                int pos = 0;
                /*
                 * Starting at index 0 of line, repeatedly parse line until the
                 * end of the line has been reached
                 */
                while (pos < s.length()) {
                    /*
                     * Call nextWordOrSeparator() to find the next word or
                     * separator string in line
                     */
                    String str = nextWordOrSeparator(s, pos, separators);
                    /*
                     * Check if the string returned by nextWordOrSeparator is
                     * not a separator string
                     */
                    if (!separators.contains(str.charAt(0))) {
                        /*
                         * If str is not a separator string, then it is a word
                         * and should be in the words Map. First, convert str to
                         * a proper noun so it looks pretty in the tag cloud
                         */
                        str = str.toUpperCase();
                        char ch = str.charAt(0);
                        str = ch + str.substring(1).toLowerCase();
                        /*
                         * Check if the Map contains the current word stored in
                         * str
                         */
                        if (!words.containsKey(str)) {
                            /*
                             * If words does not contain the current word stored
                             * in str, then add str to the Map, with 1 as its
                             * initial count value
                             */

                            words.put(str, 1);

                        } else {
                            /*
                             * If words does contain the current word stored in
                             * str, then add 1 to its count value
                             */

                            Integer value = words.remove(str);
                            words.put(str, value + 1);

                        }
                    }
                    /*
                     * The next index checked in line will be the one
                     * immediately after the end of the current word/separator
                     * string
                     */
                    pos += str.length();
                }

                /*
                 * Read next line from file
                 */
                s = file.readLine();
            }
        } catch (IOException e) {
            System.err.println("Error reading file in ParseWords");
        }

        return words;
    }

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code separators}) or "separator string" (maximal length string of
     * characters in {@code separators}) in the given {@code text} starting at
     * the given {@code position}.
     *
     * @param text
     *            the {@code String} from which to get the word or separator
     *            string
     * @param position
     *            the starting index
     * @param separators
     *            the {@code Set} of separator characters
     * @return the first word or separator string found in {@code text} starting
     *         at index {@code position}
     * @requires 0 <= position < |text|
     * @ensures <pre>
     * nextWordOrSeparator =
     *   text[position, position + |nextWordOrSeparator|)  and
     * if entries(text[position, position + 1)) intersection separators = {}
     * then
     *   entries(nextWordOrSeparator) intersection separators = {}  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      intersection separators /= {})
     * else
     *   entries(nextWordOrSeparator) is subset of separators  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      is not subset of separators)
     * </pre>
     */
    public static String nextWordOrSeparator(String text, int position,
            Set<Character> separators) {
        assert text != null : "Violation of: text is not null";
        assert separators != null : "Violation of: separators is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        /*
         * Initialize return String that will contain the next word or separator
         * substring found in text
         */
        String substring = "";
        /*
         * Check if the character at the given position index is in the
         * separator set
         */
        if (separators.contains(text.charAt(position))) {
            /*
             * If the character at the given position is in separators, then it
             * is considered a "separator" character and is not part of a word.
             */
            for (int i = position; i < text.length()
                    && separators.contains(text.charAt(i)); i++) {
                /*
                 * Iterate through the rest of the characters in text until
                 * either the end of the String is reached or until a character
                 * NOT in separators is found, each time adding the subsequent
                 * character to the return substring. Once a character is found
                 * that is not in separators, the loop breaks and the maximal
                 * length string of separators starting at position has been
                 * found.
                 */
                substring = text.substring(position, i + 1);
            }

        } else {
            /*
             * If the character at the given position is NOT in separators, then
             * it must be an alphabetical letter character and therefore part of
             * a word.
             */
            for (int i = position; i < text.length()
                    && !separators.contains(text.charAt(i)); i++) {
                /*
                 * Iterate through the rest of the characters in text until
                 * either the end of the String is reached or until a character
                 * IN separators is found, each time adding the subsequent
                 * character to the return substring. Once a character is found
                 * that IS in separators, the loop breaks and the complete
                 * "word"--maximal length string of non-separator
                 * characters--starting at position has been found.
                 */
                substring = text.substring(position, i + 1);
            }

        }

        return substring;

    }

    /**
     * Adds the pairs from {@code map} into a new List and sorts the List by
     * values in decreasing order.
     *
     * @param map
     *            the {@code SortedMap} whose entries will be sorted
     *
     * @return the List containing all the map pairs, sorted by values greatest
     *         to least
     * @ensures numericalSort = entries(map) stored in a List and sorted in >=
     *          by values
     */
    public static List<Pair> numericalSort(SortedMap<String, Integer> map) {

        assert map != null : "Violation of map is not null";

        //comparator declaration/initialization for sorting values >=
        Comparator<TagCloudGenerator.Pair> ci = new PairGT();

        //return entry set of the map
        Set<Map.Entry<String, Integer>> entrySet = map.entrySet();

        //create a List to hold the entrySet keys, values using Pair class
        List<Pair> entryList = new LinkedList<>();

        //Add keys, values in entrySet to the List as new Pairs
        for (Map.Entry<String, Integer> pair : entrySet) {
            Pair listPair = new Pair(pair.getKey(), pair.getValue());
            entryList.add(listPair);
        }

        //sort the List based on values, greatest to least
        Collections.sort(entryList, ci);

        return entryList;
    }

    /**
     * Adds the first {@code n} pairs from {@code sortedCounts} into new
     * {@code SortedMap}, which sorts them alphabetically by pair.keys (<=).
     *
     *
     * @param sortedCounts
     *            the List of word-count Pairs, sorted by value from greatest to
     *            least
     * @param n
     *            the amount of word-count Pairs to remove from the front of the
     *            List (and output to tag cloud)
     * @updates sortedCounts
     *
     * @requires n > 0 and [sortedCounts is sorted by values from greatest to
     *           least]
     *
     * @return a Map with the first n Pairs from the sorted List, sorted by
     *         "natural ordering" of the keys (lexicographically)
     * @ensures alphabeticalSort = [first n entries of sortedCounts sorted by
     *          Pair.keys <=] and |alphabeticalSort| = n
     *
     */
    public static SortedMap<String, Integer> alphabeticalSort(
            List<Pair> sortedCounts, int n) {

        assert sortedCounts != null : "Violation of map not null";
        assert n > 0 : "Violation of number of words > 0";

        /*
         * remove the first n elements from the list and add them to a new
         * SortedMap so that keys are sorted in lexicographic order
         */
        SortedMap<String, Integer> newMap = new TreeMap<>();
        for (int i = 0; i < n; i++) {
            Pair entry = sortedCounts.remove(0);
            newMap.put(entry.key, entry.value);
        }

        return newMap;
    }

    /**
     * Writes the body of the html file to the given output file.
     *
     * @param outputWords
     *            the alphabetically sorted Map of the words to be output
     * @param out
     *            the output file
     * @param maxWordCount
     *            the highest count value present in the SortedMap, used to
     *            compute the proportional font of the output words
     * @updates out
     * @requires out is open and maxWordCount > 0
     */
    public static void

            outputBody(SortedMap<String, Integer> outputWords, PrintWriter out,
                    int maxWordCount) {
        assert outputWords != null : "Violation of map not null";

        final int minFont = 11;
        final int maxFont = 48;

        //output headers
        out.println("<div class =\"cdiv\">");
        out.println("<p class=\"cbox\">");

        //get the entry set of outputWords, and an iterator for the entry set
        Set<Map.Entry<String, Integer>> entries = outputWords.entrySet();
        Iterator<Map.Entry<String, Integer>> it = entries.iterator();

        //output words
        while (it.hasNext()) {
            //return a pair from the entry set
            Map.Entry<String, Integer> pair = it.next();

            //assign word and its count from pair
            String word = pair.getKey();
            int wordCount = pair.getValue();

            //get font size proportional to word count
            int fontSize = minFont
                    + ((maxFont - minFont) * wordCount) / maxWordCount;

            //output html
            out.println("<span style=\"cursor:default\" class=\"f" + fontSize
                    + "\" title=\"count:" + wordCount + "\">" + word
                    + "</span>");

        }

        //output footers
        out.println("</p>");
        out.println("</div>");

    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {

        /*
         * Open keyboard input stream
         */
        BufferedReader in = new BufferedReader(
                new InputStreamReader(System.in));

        final String separatorString = " \t\n\r,\"*-.!?[];'`~:/()&=|{}@#$%^_+<>\\";

        /*
         * Generate Set containing all the characters in separatorString
         */
        Set<Character> separatorSet = generateSeparatorSet(separatorString);

        /*
         * Prompt user to enter name of input file
         */
        System.out.print("Enter the name of an input text file: ");
        String inputFile;
        try {
            inputFile = in.readLine();
        } catch (IOException e) {
            System.err.print("Error reading input");
            return;
        }

        /*
         * Open input stream for the entered filename
         */
        BufferedReader file;
        try {
            file = new BufferedReader(new FileReader(inputFile));
        } catch (IOException e) {
            System.err.println("Error opening input file");
            return;
        }

        /*
         * Parse the input file to generate a Map containing words and counts
         * from the file
         */
        SortedMap<String, Integer> wordCounts = parseWordsInFile(file,
                separatorSet);

        /*
         * Check if the newly returned map is empty before running the rest of
         * the program, because since the user must input a positive integer for
         * the number of words in the tag cloud, a tag cloud cannot be generated
         * for a file with 0 words.
         */
        if (wordCounts.size() > 0) {

            /*
             * Count the amount of unique words in the file by returning the
             * amount of pairs in the Map, so that user input can be checked for
             * validity
             */
            int uniqueWordsAmt = wordCounts.size();

            /*
             * Prompt user to input the number of words to be included in the
             * generated tag cloud
             */
            System.out.println(
                    "Enter the amount of words from this file that you "
                            + "would like to "
                            + "be included in the generated tag cloud.");
            System.out.print(
                    "(must be a positive integer that is less than or equal "
                            + "to " + uniqueWordsAmt + "): ");

            String amount;
            try {
                amount = in.readLine();
            } catch (IOException e) {
                System.err.println("Error reading keyboard input");
                return;
            }

            /*
             * Try to parse user input to an integer--if the user input does not
             * consist of only an integer, then parseInt will throw an exception
             * and the program will be terminated
             */

            int amountToInt = Integer.parseInt(amount);
            /*
             * Check if the desired word amount is greater than the total word
             * count in the file, or if it is not a positive integer--if so,
             * repeatedly ask user for new input until the given amount is valid
             */
            while (amountToInt > uniqueWordsAmt || amountToInt <= 0) {
                System.out.print("ERROR: invalid input. "
                        + "Enter a positive integer that is less than "
                        + uniqueWordsAmt + ": ");
                try {
                    amount = in.readLine();
                } catch (IOException e) {
                    System.err.println("Error reading keyboard input");
                }
                amountToInt = Integer.parseInt(amount);
            }

            /*
             * Prompt user to enter name of output HTML file
             */
            System.out.println("Enter the name of an output HTML file: ");

            /*
             * Open output stream to write to the entered filename
             */
            PrintWriter html;
            try {
                html = new PrintWriter(
                        new BufferedWriter(new FileWriter(in.readLine())));
            } catch (IOException e) {
                System.err.println("Error opening output file");
                return;
            }
            /*
             * Get a List of the Map entries sorted by values in decreasing
             * order
             */
            List<Pair> sortedCounts = numericalSort(wordCounts);
            /*
             * Get the highest word count from the List (to pass to outputBody)
             */
            Integer maxWordCount = sortedCounts.get(0).value;

            SortedMap<String, Integer> outputWords = alphabeticalSort(
                    sortedCounts, amountToInt);
            /*
             * Output the HTML header and body to the output file
             */
            outputHeader(html, inputFile, amountToInt);
            outputBody(outputWords, html, maxWordCount);

            /*
             * Output closing HTML tags to the output file
             */
            html.println("</body>");
            html.println("</html>");

            /*
             * Close output file stream
             */

            html.close();

        } else {
            /*
             * if wordCounts Map was empty, output error message
             */
            System.out.println(
                    "The input file was empty, so no tag cloud could be generated.");

        }

        /*
         * Close input and output streams
         */
        try {
            in.close();
        } catch (IOException e) {
            System.err.println("Error closing input stream");
        }
        try {
            file.close();
        } catch (IOException e) {
            System.err.println("Error closing input stream");
        }
    }

}
