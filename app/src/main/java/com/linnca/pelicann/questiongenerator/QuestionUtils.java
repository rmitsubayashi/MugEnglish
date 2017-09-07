package com.linnca.pelicann.questiongenerator;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class QuestionUtils {
    public static String TRUE_FALSE_QUESTION_TRUE = "true";
    public static String TRUE_FALSE_QUESTION_FALSE = "false";
    public static String FILL_IN_BLANK_TEXT = "@blankText@";
    public  static String FILL_IN_BLANK_NUMBER = "@blankNum@";
    public static String FILL_IN_BLANK_MULTIPLE_CHOICE = "@blankMC@";

    private static List<Character> characterList;

    public static void shuffle(List<String> choices){
        Collections.shuffle(choices, new Random(System.currentTimeMillis()));
    }

    public static String formatPuzzlePieceAnswer(List<String> choices){
        String answer = "";
        for (String choice : choices){
            answer += choice + "|";
        }

        answer = answer.substring(0, answer.length()-1);

        return answer;
    }

    public static void populateRandomCharacterBasedOnProbability(){
        characterList = new ArrayList<>();
        //probability based on
        //https://en.wikipedia.org/wiki/Letter_frequency#Relative_frequencies_of_letters_in_the_English_language
        characterList.addAll(Collections.nCopies(82, 'a'));
        characterList.addAll(Collections.nCopies(15, 'b'));
        characterList.addAll(Collections.nCopies(28, 'c'));
        characterList.addAll(Collections.nCopies(43, 'd'));
        characterList.addAll(Collections.nCopies(127, 'e'));
        characterList.addAll(Collections.nCopies(22, 'f'));
        characterList.addAll(Collections.nCopies(20, 'g'));
        characterList.addAll(Collections.nCopies(61, 'h'));
        characterList.addAll(Collections.nCopies(70, 'i'));
        characterList.addAll(Collections.nCopies(15, 'j'));
        characterList.addAll(Collections.nCopies(77, 'k'));
        characterList.addAll(Collections.nCopies(40, 'l'));
        characterList.addAll(Collections.nCopies(24, 'm'));
        characterList.addAll(Collections.nCopies(47, 'n'));
        characterList.addAll(Collections.nCopies(75, 'o'));
        characterList.addAll(Collections.nCopies(19, 'p'));
        characterList.addAll(Collections.nCopies(1, 'q'));
        characterList.addAll(Collections.nCopies(60, 'r'));
        characterList.addAll(Collections.nCopies(63, 's'));
        characterList.addAll(Collections.nCopies(91, 't'));
        characterList.addAll(Collections.nCopies(28, 'u'));
        characterList.addAll(Collections.nCopies(10, 'v'));
        characterList.addAll(Collections.nCopies(24, 'w'));
        characterList.addAll(Collections.nCopies(2, 'x'));
        characterList.addAll(Collections.nCopies(20, 'y'));
        characterList.addAll(Collections.nCopies(1, 'z'));

    }

    public static char getRandomCharacterBasedOnProbability(){
        if (characterList == null){
            populateRandomCharacterBasedOnProbability();
        }
        Random random = new Random();
        int index = random.nextInt(characterList.size());
        return characterList.get(index);
    }

    public static void clearRandomCharacters(){
        if (characterList == null){
            return;
        }
        characterList.clear();
        characterList = null;
    }
}
