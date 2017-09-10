package com.linnca.pelicann.questiongenerator;

public class QGUtils {
    public static String stripWikidataID(String str){
        int lastIndexID = str.lastIndexOf('/');
        return str.substring(lastIndexID+1);
    }

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

    public static String convertIntToWord(int number) {

        if (number == 0) { return "zero"; }

        String prefix = "";

        if (number < 0) {
            number = -number;
            prefix = "negative";
        }

        String current = "";
        int place = 0;

        do {
            int n = number % 1000;
            if (n != 0){
                String s = convertLessThanOneThousand(n);
                if (place != 0)
                    current = s + " " + specialNames[place-1] + current;
                else
                    current = s + current;
            }
            place++;
            number /= 1000;
        } while (number > 0);

        return (prefix + current).trim();
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

    public static boolean containsJapanese(String str){
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
}
