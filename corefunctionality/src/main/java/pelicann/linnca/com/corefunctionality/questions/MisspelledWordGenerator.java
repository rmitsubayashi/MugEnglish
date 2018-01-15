package pelicann.linnca.com.corefunctionality.questions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import pelicann.linnca.com.corefunctionality.lessongeneration.StringUtils;

public final class MisspelledWordGenerator {
    private MisspelledWordGenerator(){}

    //higher the number, the more similar it will be to the original
    public static List<String> createMisspelledWords(String word, int odds, int ct){
        List<String> results = new ArrayList<>(ct);
        for (int i=0; i<ct; i++) {
            String result = word;
            int maxLoopCt = word.length();
            while ( (result.equals(word) || results.contains(result))
                    && maxLoopCt != 0) {
                result = createMisspelledWordHelper(word, odds);
                maxLoopCt--;
            }

            if (result.equals(word) || results.contains(result)) {
                String shuffleWord = word;
                while (shuffleWord.equals(word) || results.contains(shuffleWord)) {
                    shuffleWord = StringUtils.shuffleString(word);
                }
                result = shuffleWord;
            }
            results.add(result);
        }

        return results;
    }

    private static String createMisspelledWordHelper(String word, int odds){
        //make sure the word somehow resembles the word
        Random random = new Random(System.currentTimeMillis());
        int nextRandom;

        //single consonant between vowels <-> double consonants
        //ie sine -> sinne, letter -> leter, bass -> bas
        int wordLetterCt = word.length();
        String tempWord = word;
        int changeCt = 0;
        for (int i=0; i<wordLetterCt; i++){
            char c = tempWord.charAt(i);
            //it can't be in between two letters
            if (i == 0 || i == wordLetterCt-1)
                continue;

            //not a character
            if (!Character.isLetter(c))
                continue;

            //no need to check
            if (isVowel(c)){
                continue;
            }

            if (tempWord.charAt(i+1) == c){
                nextRandom = random.nextInt(odds);
                if (nextRandom == 0){
                    String doubleConsonant = Character.toString(c) + Character.toString(c);
                    word = word.replaceFirst(doubleConsonant, Character.toString(c));
                    changeCt--;
                }
            }

            if (isVowel(tempWord.charAt(i-1)) && isVowel(tempWord.charAt(i+1))){
                nextRandom = random.nextInt(odds);
                if (nextRandom == 0){
                    String part1 = word.substring(0, i+changeCt);
                    String part2 = word.substring(i+changeCt, word.length());
                    word = part1 + Character.toString(c) + part2;
                    changeCt++;
                }
            }
        }

        //switch similar letters
        //i  <-> y
        //t  <-> th
        //th <-> d
        //o  <-> oh
        //ck <-> k
        //r  <-> l
        //a  <-> e
        //e  <-> ee
        //aw <-> ow
        //w  <-> wh
        //n  <-> m
        //c  <-> s
        //x   -> cks
        //u   -> a
        //b  <-> v
        //f  <-> ph
        //g  <-> j

        tempWord = word;
        StringBuilder builder = new StringBuilder(word);
        wordLetterCt = word.length();
        changeCt = 0;
        for (int i=0; i<wordLetterCt; i++){
            char letter = tempWord.charAt(i);
            if (letter == 't'){
                //make sure we don't go out of range
                if (i+1 != wordLetterCt){
                    char nextLetter = tempWord.charAt(i+1);
                    nextRandom = random.nextInt(odds);
                    if (nextLetter == 'h'){
                        if (nextRandom == 0){
                            nextRandom = random.nextInt(2);
                            if (nextRandom == 0) {
                                //th -> t
                                builder.deleteCharAt(i + 1 + changeCt);
                                changeCt--;
                            } else {
                                //th -> d
                                builder.replace(i+changeCt, i+changeCt+2, "d");
                                changeCt--;
                            }
                        }
                    } else {
                        //t -> th
                        if (nextRandom == 0){
                            builder.insert(i+1+ changeCt, 'h');
                        }
                    }
                }
            } else if (letter == 'd'){
                nextRandom =random.nextInt(odds);
                if (nextRandom == 0){
                    builder.replace(i+changeCt, i+changeCt+1, "th");
                    changeCt++;
                }
            }

            if (letter == 'y'){
                //if not the first letter (always consonant)
                if (i != 0){
                    nextRandom =random.nextInt(odds);
                    if (nextRandom == 0){
                        builder.replace(i+changeCt, i+changeCt+1, "i");
                    }
                }
            } else if (letter == 'i'){
                //if not the first letter (never consonant ie igloo, instant)
                if (i != 0){
                    nextRandom =random.nextInt(odds);
                    if (nextRandom == 0){
                        builder.replace(i+changeCt, i+changeCt+1, "y");
                    }
                }
            }

            if (letter == 'a'){
                nextRandom =random.nextInt(odds);
                if (nextRandom == 0){
                    builder.replace(i+changeCt, i+changeCt+1, "e");
                }
            } else if (letter == 'e'){
                //make sure we don't go out of range
                if (i+1 != wordLetterCt) {
                    char nextLetter = tempWord.charAt(i + 1);
                    nextRandom = random.nextInt(odds);
                    if (nextLetter == 'e'){
                        if (nextRandom == 0){
                            builder.insert(i+1 + changeCt, 'e');
                            changeCt++;
                        }
                    } else {
                        if (nextRandom == 0) {
                            builder.replace(i + changeCt, i + changeCt + 1, "a");
                        }
                    }
                }
            }

            //wh is most often at start of word
            if (letter == 'w' && i == 0){
                //make sure we don't go out of range
                if (i+1 != wordLetterCt){
                    char nextLetter = tempWord.charAt(i+1);
                    nextRandom = random.nextInt(odds);
                    if (nextLetter == 'h'){
                        //wh -> w
                        if (nextRandom == 0){
                            builder.deleteCharAt(i+1 + changeCt);
                            changeCt--;
                        }
                    } else {
                        //w -> wh
                        if (nextRandom == 0){
                            builder.insert(i+1 + changeCt, 'h');
                            changeCt++;
                        }
                    }
                }
            }

            if (letter == 'o'){
                //make sure we don't go out of range
                if (i+1 != wordLetterCt){
                    char nextLetter = tempWord.charAt(i+1);
                    nextRandom = random.nextInt(odds);
                    if (nextLetter == 'h'){
                        //oh -> o
                        if (nextRandom == 0){
                            builder.deleteCharAt(i+1 + changeCt);
                            changeCt--;
                        }
                    } else if (nextLetter == 'w'){
                        //ow -> aw
                        if (nextRandom == 0){
                            builder.replace(i+changeCt, i+changeCt+1, "a");
                        }

                    }else{
                        //o -> oh
                        if (nextRandom == 0){
                            builder.insert(i+1+ changeCt, 'h');
                            changeCt++;
                        }
                    }
                }
            }

            if (letter == 'l'){
                nextRandom =random.nextInt(odds);
                if (nextRandom == 0){
                    builder.replace(i+changeCt, i+changeCt+1, "r");
                }
            } else if (letter == 'r'){
                nextRandom =random.nextInt(odds);
                if (nextRandom == 0){
                    builder.replace(i+changeCt, i+changeCt+1, "l");
                }
            }

            if (letter == 'm'){
                nextRandom =random.nextInt(odds);
                if (nextRandom == 0){
                    builder.replace(i+changeCt, i+changeCt+1, "n");
                }
            } else if (letter == 'n'){
                nextRandom =random.nextInt(odds);
                if (nextRandom == 0){
                    builder.replace(i+changeCt, i+changeCt+1, "m");
                }
            }

            if (letter == 'v'){
                nextRandom =random.nextInt(odds);
                if (nextRandom == 0){
                    builder.replace(i+changeCt, i+changeCt+1, "b");
                }
            } else if (letter == 'b'){
                nextRandom =random.nextInt(odds);
                if (nextRandom == 0){
                    builder.replace(i+changeCt, i+changeCt+1, "v");
                }
            }

            if (letter == 'g'){
                nextRandom =random.nextInt(odds);
                if (nextRandom == 0){
                    builder.replace(i+changeCt, i+changeCt+1, "j");
                }
            } else if (letter == 'j'){
                nextRandom =random.nextInt(odds);
                if (nextRandom == 0){
                    builder.replace(i+changeCt, i+changeCt+1, "g");
                }
            }

            if (letter == 's'){
                nextRandom =random.nextInt(odds);
                if (nextRandom == 0){
                    builder.replace(i+changeCt, i+changeCt+1, "c");
                }
            } else if (letter == 'c'){
                //make sure we don't go out of range
                if (i+1 != wordLetterCt) {
                    char nextLetter = tempWord.charAt(i + 1);
                    nextRandom = random.nextInt(odds);
                    if (nextLetter == 'k') {
                        if (nextRandom == 0) {
                            builder.deleteCharAt(i + changeCt);
                            changeCt--;
                        }
                    } else {
                        if (nextRandom == 0){
                            builder.replace(i+changeCt, i+changeCt+1, "s");
                        }
                    }
                }
            }

            if (letter == 'x'){
                nextRandom =random.nextInt(odds);
                if (nextRandom == 0){
                    builder.replace(i+changeCt, i+changeCt+1, "cks");
                    changeCt += 2;
                }
            }

            if (letter == 'u'){
                nextRandom =random.nextInt(odds);
                if (nextRandom == 0){
                    builder.replace(i+changeCt, i+changeCt+1, "a");
                }
            }

            if (letter == 'f'){
                nextRandom =random.nextInt(odds);
                if (nextRandom == 0){
                    builder.replace(i+changeCt, i+changeCt+1, "ph");
                    changeCt++;
                }
            } else if (letter == 'p'){
                //make sure we don't go out of range
                if (i+1 != wordLetterCt) {
                    char nextLetter = tempWord.charAt(i + 1);
                    nextRandom = random.nextInt(odds);
                    if (nextLetter == 'h') {
                        if (nextRandom == 0) {
                            builder.replace(i+changeCt, i+changeCt+2, "f");
                            changeCt--;
                        }
                    }
                }
            }
        }

        word = builder.toString();

        return word;

    }

    private static boolean isVowel(char c){
        return c == 'a' || c == 'e' || c == 'i'
                || c == 'o' || c == 'u';
    }
}
