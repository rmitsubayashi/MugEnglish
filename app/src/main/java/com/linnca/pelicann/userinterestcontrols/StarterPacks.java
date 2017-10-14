package com.linnca.pelicann.userinterestcontrols;

import com.linnca.pelicann.userinterests.WikiDataEntryData;

import java.util.ArrayList;
import java.util.List;

public class StarterPacks {
    public static final int MAN = 1;
    public static final int WOMAN = 2;
    public static final int BOY = 3;
    public static final int GIRL = 4;

    private StarterPacks(){}

    //commented lines are reasons why we chose them
    public static List<WikiDataEntryData> getStarterPack(int type){
        List<WikiDataEntryData> list = new ArrayList<>();
        //general ones for all age groups
        list.add(new WikiDataEntryData("マツコ・デラックス","日本のコラムニスト、エッセイスト、女装タレント","Q813982","まつこ・でらっくす", 0));
        //ESPN most famous athletes
        list.add(new WikiDataEntryData("イチロー","Japanese-born baseball player","Q600","いちろー", 0));
        list.add(new WikiDataEntryData("錦織圭","日本のプロテニス選手","Q311222","にしこり けい", 0));
        //general countries
        list.add(new WikiDataEntryData("アメリカ合衆国","北アメリカ大陸および北太平洋に位置する連邦共和国","Q30","あめりかがっしゅうこく", 1));
        list.add(new WikiDataEntryData("日本","東アジアに位置する国家","Q17","にっぽん", 1));
        //capital
        list.add(new WikiDataEntryData("東京都","日本の47都道府県のひとつ。国政の首都機能を有する","Q1490","とうきょうと", 1));
        //popular cities
        list.add(new WikiDataEntryData("大阪市","大阪府にある市。政令指定都市のひとつ","Q35765","おおさかし", 1));
        list.add(new WikiDataEntryData("京都市","日本の市。政令指定都市。京都府庁所在地","Q34600","きょうとし", 1));

        switch (type){
            case MAN :
                //ace of Jpn soccer
                list.add(new WikiDataEntryData("香川真司","日本のサッカー選手","Q154708","かがわ しんじ", 0));
                //most influential positions in Japanese politics
                list.add(new WikiDataEntryData("安倍晋三","内閣総理大臣","Q132345","あべ しんぞう", 0));

                break;
            case WOMAN :
                //most influential positions in Japanese politics
                list.add(new WikiDataEntryData("安倍晋三","内閣総理大臣","Q132345","あべ しんぞう", 0));
                //ESPN most famous athletes
                list.add(new WikiDataEntryData("セリーナ・ウィリアムズ","American tennis player","Q11459","せりーな・うぃりあむず", 0));

                break;
            case BOY :

                break;
            case GIRL :

                break;
            default :



        }

        return list;
    }
}
