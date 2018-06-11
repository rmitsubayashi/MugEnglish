package pelicann.linnca.com.corefunctionality.lessonscript;

import org.joda.time.LocalDate;
import org.joda.time.Years;

import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import pelicann.linnca.com.corefunctionality.lessoninstance.Translation;

public final class StringUtils {
    //for converting ints to words
    private static final String[] specialNames = {
            "thousand",
            "million",
            "billion",
            "trillion",
            "quadrillion",
            "quintillion"
    };

    private static final String[] tens = {
            "ten",
            "twenty",
            "thirty",
            "forty",
            "fifty",
            "sixty",
            "seventy",
            "eighty",
            "ninety"
    };

    private static final String[] ones = {
            "one",
            "two",
            "three",
            "four",
            "five",
            "six",
            "seven",
            "eight",
            "nine"
    };

    private static final String[] teens = {
            "eleven",
            "twelve",
            "thirteen",
            "fourteen",
            "fifteen",
            "sixteen",
            "seventeen",
            "eighteen",
            "nineteen"
    };

    private static String convertLessThanOneThousand(int number) {
        String current = "";

        if (number % 100 > 10 && number % 100 < 20){
            current = " " + teens[number % 10 - 1];
            //get hundreds place
            number /= 100;
        }
        else {
            //get ones place
            boolean onesExists = number % 10 > 0;
            if (onesExists)
                current = ones[number % 10 - 1];
            number /= 10;

            //ones and tens
            if (onesExists && number % 10 > 0)
                current = " " + tens[number % 10 - 1] + "-" + current;
            //just tens
            else if (!onesExists && number % 10 > 0)
                current = " " + tens[number % 10 - 1];
            //just ones
            else if (onesExists && number % 10 == 0)
                current = " " + current;
            number /= 10;
        }
        //if we don't need a hundreds place
        if (number == 0) return current;
        //we need a hundreds place
        return " " + ones[number-1] + " hundred" + current;
    }

    private static String convertIntToWord(int number) {

        if (number == 0) { return "zero"; }

        String prefix = "";

        if (number < 0) {
            number = -number;
            prefix = "negative";
        }

        StringBuilder current = new StringBuilder("");
        int place = 0;

        do {
            int n = number % 1000;
            if (n != 0){
                String s = convertLessThanOneThousand(n);
                if (place != 0)
                    current = current.insert(0, s + " " + specialNames[place-1]);
                else
                    current = current.insert(0, s);
            }
            place++;
            number /= 1000;
        } while (number > 0);

        return (prefix + current.toString()).trim();
    }

    public static String convertIntToWord(String numberString){
        int number;
        try {
            number = Integer.parseInt(numberString);
        } catch (ClassCastException e){
            e.printStackTrace();
            return numberString;
        }
        return convertIntToWord(number);
    }

    public static String convertIntToStringWithCommas(int number){
        NumberFormat format = NumberFormat.getInstance(Locale.US);
        return format.format(number);
    }

    //wikiData gives us a full date even if we only want the year only
    public static String getYearFromFullISO8601DateTime(String fullISO8601DateTime){
        if (fullISO8601DateTime == null || fullISO8601DateTime.equals(""))
            return "";
        //just in case we put in just a year
        if (fullISO8601DateTime.length() < 4)
            return fullISO8601DateTime;
        //we can assume the first four letters are the date
        // ex: 1990-01-01T00:00:00Z
        return fullISO8601DateTime.substring(0,4);
    }

    //can't be integers because numbers with 0s in front will be cut
    public static List<String> convertPhoneNumberToPhoneNumberWords(String number){
        String typicalNumber = "";
        String zeroToOhNumber;
        String zerosToHundredsAndThousandsNumber = "";
        String zerosToHundredsAndThousandsWithOhNumber;

        int zeroCt = 0;
        char[] numberArray = number.toCharArray();
        int numberMaxIndex = numberArray.length-1;
        for (int i=numberMaxIndex; i>=0; i--){
            char digitChar = numberArray[i];
            if (!Character.isDigit(digitChar)){
                //we can interpret any non-digit as a break in the number??
                for (int j=0; j<zeroCt; j++){
                    typicalNumber = "zero " + typicalNumber;
                    zerosToHundredsAndThousandsNumber = "zero " + zerosToHundredsAndThousandsNumber;
                    //reset
                    zeroCt = 0;
                }
                continue;
            }
            int digit = Character.getNumericValue(digitChar);
            if (digit == 0){
                zeroCt++;
            } else {
                String digitString = ones[digit-1];
                String typicalNumberToAdd = digitString + " ";
                String zerosToHundredsAndThousandsNumberToAdd = digitString + " ";
                if (zeroCt > 0){
                    for (int j=0; j<zeroCt; j++){
                        typicalNumberToAdd += "zero ";
                    }

                    switch (zeroCt){
                        case 1:
                            zerosToHundredsAndThousandsNumberToAdd += "zero ";
                            break;
                        case 2:
                            zerosToHundredsAndThousandsNumberToAdd += "hundred ";
                            break;
                        case 3:
                            zerosToHundredsAndThousandsNumberToAdd += "thousand ";
                            break;
                        default:
                            for (int j=0; j<zeroCt; j++){
                                zerosToHundredsAndThousandsNumberToAdd += "zero ";
                            }
                    }

                    //reset
                    zeroCt = 0;
                }

                typicalNumber = typicalNumberToAdd + typicalNumber;
                zerosToHundredsAndThousandsNumber = zerosToHundredsAndThousandsNumberToAdd +
                        zerosToHundredsAndThousandsNumber;
            }

        }

        //handle hanging zeroes
        if (zeroCt > 0) {
            for (int i = 0; i < zeroCt; i++) {
                typicalNumber = "zero " + typicalNumber;
                zerosToHundredsAndThousandsNumber = "zero " + zerosToHundredsAndThousandsNumber;
            }
        }

        typicalNumber = typicalNumber.trim();
        zerosToHundredsAndThousandsNumber = zerosToHundredsAndThousandsNumber.trim();

        List<String> returnList = new ArrayList<>(4);
        //order is important to know which is which. make sure this is first
        returnList.add(typicalNumber);
        if (typicalNumber.contains("zero")){
            zeroToOhNumber = typicalNumber.replaceAll("zero","oh");
            returnList.add(zeroToOhNumber);
        }

        if (!typicalNumber.equals(zerosToHundredsAndThousandsNumber)){
            returnList.add(zerosToHundredsAndThousandsNumber);
        }

        if (typicalNumber.contains("zero") && !typicalNumber.equals(zerosToHundredsAndThousandsNumber)) {
            zerosToHundredsAndThousandsWithOhNumber = zerosToHundredsAndThousandsNumber.replaceAll("zero", "oh");
            returnList.add(zerosToHundredsAndThousandsWithOhNumber);
        }

        //can be 1 ~ 4 elements
        return returnList;
    }

    public static boolean containsJapanese(String str){
        if (str == null){
            return false;
        }
        for(int i = 0 ; i < str.length() ; i++) {
            char ch = str.charAt(i);
            Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(ch);

            if (Character.UnicodeBlock.HIRAGANA.equals(unicodeBlock))
                return true;

            if (Character.UnicodeBlock.KATAKANA.equals(unicodeBlock))
                return true;

            if (Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS.equals(unicodeBlock))
                return true;

            if (Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS.equals(unicodeBlock))
                return true;

            if (Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION.equals(unicodeBlock))
                return true;
        }
        return false;
    }

    //there is no inherent method to shuffle an array
    public static String shuffleString(String str){
        char[] stringArray = str.toCharArray();
        Random random = new Random(System.currentTimeMillis());
        //Fisher-Yates
        int startIndex = stringArray.length-1;
        for (int i=startIndex; i>0; i--){
            int shuffleToIndex = random.nextInt(i+1);
            char temp = stringArray[i];
            stringArray[i] = stringArray[shuffleToIndex];
            stringArray[shuffleToIndex] = temp;
        }
        return String.valueOf(stringArray);
    }

    public static boolean isAlphanumeric(String str){
        return StandardCharsets.US_ASCII.newEncoder().canEncode(str);
    }

    public static int getAge(String birthdayString){
        LocalDate birthday = LocalDate.parse(birthdayString);
        LocalDate now = new LocalDate();
        Years age = Years.yearsBetween(birthday, now);
        return age.getYears();
    }

    //with periods, if the end of the sentence
    // ie. Martin Luther King Jr. & Leicester F.C.
    // we shouldn't have duplicate periods. for other punctuations,
    // it doesn't matter (we add question marks and exclamation marks
    // after the period
    public static String addPeriod(String sentence){
        if (!sentence.endsWith(".")){
            sentence = sentence + ".";
        }
        return sentence;
    }

    public static String zenkakuKatakanaToZenkakuHiragana(String s) {
        StringBuilder sb = new StringBuilder(s);
        int length = sb.length();
        for (int i = 0; i < length; i++) {
            char c = sb.charAt(i);
            if (c >= 'ァ' && c <= 'ン') {
                sb.setCharAt(i, (char)(c - 'ァ' + 'ぁ'));
            } else if (c == 'ヵ') {
                sb.setCharAt(i, 'か');
            } else if (c == 'ヶ') {
                sb.setCharAt(i, 'け');
            } else if (c == 'ヴ') {
                sb.setCharAt(i, 'う');
                sb.insert(i + 1, '゛');
                i++;
            }
        }
        return sb.toString();
    }

    public static boolean containsKanji(String s){
        int length = s.length();
        for (int i=0; i < length; i++){
            char c = s.charAt(i);
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS){
                return true;
            }
        }

        return false;
    }

    //util for gender (we will be using a lot of gender)
    public static final int CASE_SUBJECTIVE = 1;
    public static final int CASE_OBJECTIVE = 2;
    public static final int CASE_POSSESSIVE = 3;
    public static Translation getGenderPronoun(boolean isMale, int grammarCase){
        Translation translation = new Translation();
        if (isMale) {
            switch (grammarCase){
                case CASE_SUBJECTIVE :
                    translation.setEnglish("he");
                    translation.setJapanese("彼");
                    break;
                case CASE_OBJECTIVE :
                    translation.setEnglish("him");
                    translation.setJapanese("彼");
                    break;
                case CASE_POSSESSIVE :
                    translation.setEnglish("his");
                    translation.setJapanese("彼");
                    break;
            }

        } else {
            switch (grammarCase){
                case CASE_SUBJECTIVE :
                    translation.setEnglish("she");
                    translation.setJapanese("彼女");
                    break;
                case CASE_OBJECTIVE :
                    translation.setEnglish("her");
                    translation.setJapanese("彼女");
                    break;
                case CASE_POSSESSIVE :
                    translation.setEnglish("her");
                    translation.setJapanese("彼女");
                    break;
            }
        }

        return translation;
    }
}
