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
        //since most of the data is for general rankings and not
        // age/gender distributed rankings,
        // the initial list will be based on general rankings + my intuition.
        //after we have enough people using our app, base the starter packs
        // based on user data

        //top 100 well known people (水曜日のダウンタウン)
        add(type, list,
                new WikiDataEntryData("タモリ","日本の芸能人","Q850746","たもり",WikiDataEntryData.CLASSIFICATION_PERSON),
                true, true, true, true);
        add(type, list,
                new WikiDataEntryData("和田アキ子","Zainichi Korean singer","Q710937","わだ あきこ",WikiDataEntryData.CLASSIFICATION_PERSON),
                true, true, false, false);
        add(type, list,
                new WikiDataEntryData("マツコ・デラックス","日本のコラムニスト、エッセイスト、女装タレント","Q813982","まつこ・でらっくす", WikiDataEntryData.CLASSIFICATION_PERSON),
                true, true, true, true);
        add(type, list,
                new WikiDataEntryData("木村拓哉", "Japanese actor and singer","Q362254","きむら たくや",WikiDataEntryData.CLASSIFICATION_PERSON),
                false, true, false, false);
        add(type, list,
                new WikiDataEntryData("田中将大","日本の野球選手","Q911463","たなか まさひろ", WikiDataEntryData.CLASSIFICATION_PERSON),
                true, false, true, false);
        add(type, list,
                new WikiDataEntryData("指原莉乃","Japanese female singer, member of HKT48, former member of AKB48, and producer/manager of =Love","Q181881","さしはら りの", WikiDataEntryData.CLASSIFICATION_PERSON),
                true, true, true, true);
        add(type, list,
                new WikiDataEntryData("吉田沙保里","Japanese sport wrestler","Q230985","よしだ さおり", WikiDataEntryData.CLASSIFICATION_PERSON),
                false, true, false, true);
        add(type, list,
                new WikiDataEntryData("浅田真央","日本のフィギュアスケート選手","Q234577","あさだ まお", WikiDataEntryData.CLASSIFICATION_PERSON),
                false, true, false, true);
        add(type, list,
                new WikiDataEntryData("福山雅治","Japanese singer-songwriter","Q1141809","ふくやま まさはる", WikiDataEntryData.CLASSIFICATION_PERSON),
                false, true, false, true);
        add(type, list,
                new WikiDataEntryData("福原愛","日本の卓球選手","Q239371","ふくはら あい", WikiDataEntryData.CLASSIFICATION_PERSON),
                false, true, false, true);
        add(type, list,
                new WikiDataEntryData("安倍晋三","内閣総理大臣","Q132345","あべ しんぞう", WikiDataEntryData.CLASSIFICATION_PERSON),
                true, true, true, true);
        add(type, list,
                new WikiDataEntryData("小池百合子","日本の政治家","Q261703","こいけ ゆりこ", WikiDataEntryData.CLASSIFICATION_PERSON),
                true, true, true, true);
        add(type, list,
                new WikiDataEntryData("古坂大魔王","お笑い芸人","Q11410993","こさか だいまおう", WikiDataEntryData.CLASSIFICATION_PERSON),
                true, true, true, true);
        add(type, list,
                new WikiDataEntryData("羽生結弦","日本のフィギュアスケート選手","Q597023","はにゅう ゆづる", WikiDataEntryData.CLASSIFICATION_PERSON),
                false, true, false, true);
        add(type, list,
                new WikiDataEntryData("東国原英夫","Japanese actor-politician","Q730869","ひがしこくばる ひでお", WikiDataEntryData.CLASSIFICATION_PERSON),
                true, true, false, false);
        add(type, list,
                new WikiDataEntryData("中居正広","司会者、俳優、歌手","Q1205631","なかい まさひろ", WikiDataEntryData.CLASSIFICATION_PERSON),
                true, true, true, true);
        add(type, list,
                new WikiDataEntryData("小泉純一郎","内閣総理大臣","Q130852","こいずみ じゅんいちろう", WikiDataEntryData.CLASSIFICATION_PERSON),
                true, true, true, true);
        add(type, list,
                new WikiDataEntryData("石原慎太郎","政治家、作家","Q38849","いしはら しんたろう", WikiDataEntryData.CLASSIFICATION_PERSON),
                true, true, false, false);
        add(type, list,
                new WikiDataEntryData("きゃりーぱみゅぱみゅ","日本のファッションモデル、歌手、ブロガー","Q242300","きゃりーぱみゅぱみゅ", WikiDataEntryData.CLASSIFICATION_PERSON),
                false, false, false, true);
        add(type, list,
                new WikiDataEntryData("二宮和也","Japanese actor","Q362277","にのみや かずなり", WikiDataEntryData.CLASSIFICATION_PERSON),
                false, true, false, true);
        add(type, list,
                new WikiDataEntryData("芦田愛菜","日本の子役","Q1135649","あしだ まな", WikiDataEntryData.CLASSIFICATION_PERSON),
                true, true, true, true);

        //google most famous people
        add(type, list,
                new WikiDataEntryData("バラク・オバマ","アメリカ合衆国の政治家、第44代大統領","Q76","ばらく おばま", WikiDataEntryData.CLASSIFICATION_PERSON),
                true, true, true, true);
        add(type, list,
                new WikiDataEntryData("ジャスティン・ビーバー","Canadian singer-songwriter, record producer and actor","Q34086","じゃすてぃん びーばー", WikiDataEntryData.CLASSIFICATION_PERSON),
                true, true, true, true);
        add(type, list,
                new WikiDataEntryData("ドナルド・トランプ","アメリカ合衆国の政治家、第45代大統領","Q22686","どなるど とらんぷ", WikiDataEntryData.CLASSIFICATION_PERSON),
                true, true, true, true);
        add(type, list,
                new WikiDataEntryData("エイブラハム・リンカーン","アメリカ合衆国の政治家","Q91","えいぶらはむ りんかーん", WikiDataEntryData.CLASSIFICATION_PERSON),
                true, false, true, false);
        add(type, list,
                new WikiDataEntryData("テイラー・スウィフト","アメリカ合衆国のカントリー・ミュージック歌手、シンガーソングライター","Q26876","ていらー すうぃふと", WikiDataEntryData.CLASSIFICATION_PERSON),
                false, true, false, true);

        //国民的アニメ
        // https://www.anikore.jp/tag/%E5%9B%BD%E6%B0%91%E7%9A%84%E3%82%A2%E3%83%8B%E3%83%A1/
        add(type, list,
                new WikiDataEntryData("野比のび太","藤子・F・不二雄の漫画『ドラえもん』の登場人物","Q1367960","のび のびた", WikiDataEntryData.CLASSIFICATION_PERSON),
                false, false, true, true);
        add(type, list,
                new WikiDataEntryData("工藤 新一","character in Detective Conan","Q844697","くどう しんいち", WikiDataEntryData.CLASSIFICATION_PERSON),
                false, false, true, true);
        add(type, list,
                new WikiDataEntryData("孫悟空","漫画『ドラゴンボール』の登場人物、主人公","Q2142","そん ごくう", WikiDataEntryData.CLASSIFICATION_PERSON),
                false, false, true, false);
        add(type, list,
                new WikiDataEntryData("月野うさぎ","fictional character from the franchise of the same name","Q757015","つきの うさぎ", WikiDataEntryData.CLASSIFICATION_PERSON),
                false, false, false, true);
        add(type, list,
                new WikiDataEntryData("モンキー・D・ルフィ","fictional character from One Piece","Q477948","もんきー でぃー るふぃー", WikiDataEntryData.CLASSIFICATION_PERSON),
                false, false, true, false);

        //ESPN most famous athletes
        add(type, list,
                new WikiDataEntryData("イチロー","Japanese-born baseball player","Q600","いちろー", WikiDataEntryData.CLASSIFICATION_PERSON),
                true, false, true, false);
        add(type, list,
                new WikiDataEntryData("錦織圭","日本のプロテニス選手","Q311222","にしこり けい", WikiDataEntryData.CLASSIFICATION_PERSON),
                true, true, true, true);
        add(type, list,
                new WikiDataEntryData("セリーナ・ウィリアムズ","American tennis player","Q11459","せりーな・うぃりあむず", WikiDataEntryData.CLASSIFICATION_PERSON),
                false, true, false, true);
        //general countries
        add(type, list,
                new WikiDataEntryData("アメリカ合衆国","北アメリカ大陸および北太平洋に位置する連邦共和国","Q30","あめりかがっしゅうこく", WikiDataEntryData.CLASSIFICATION_PLACE),
                true, true, true, true);
        add(type, list,
                new WikiDataEntryData("日本","東アジアに位置する国家","Q17","にっぽん", WikiDataEntryData.CLASSIFICATION_PLACE),
                true, true, true, true);
        add(type, list,
                new WikiDataEntryData("中華人民共和国","東アジアに位置する国家","Q148","ちゅうかじんみんきょうわこく", WikiDataEntryData.CLASSIFICATION_PLACE),
                true, true, true, true);
        add(type, list,
                new WikiDataEntryData("イギリス","ヨーロッパの国","Q145","いぎりす", WikiDataEntryData.CLASSIFICATION_PLACE),
                true, true, true, true);
        //capital
        add(type, list,
                new WikiDataEntryData("東京都","日本の47都道府県のひとつ。国政の首都機能を有する","Q1490","とうきょうと", WikiDataEntryData.CLASSIFICATION_PLACE),
                true, true, true, true);
        //popular Japanese cities
        add(type, list,
                new WikiDataEntryData("大阪市","大阪府にある市。政令指定都市のひとつ","Q35765","おおさかし", WikiDataEntryData.CLASSIFICATION_PLACE),
                true, true, true, true);
        add(type, list,
                new WikiDataEntryData("京都市","日本の市。政令指定都市。京都府庁所在地","Q34600","きょうとし", WikiDataEntryData.CLASSIFICATION_PLACE),
                true, true, true, true);
        //popular cities
        add(type, list,
                new WikiDataEntryData("パリ","フランスの首都","Q90","ぱり", WikiDataEntryData.CLASSIFICATION_PLACE),
                false, true, false, true);
        add(type, list,
                new WikiDataEntryData("ローマ","イタリアの首都","Q220","ろーま", WikiDataEntryData.CLASSIFICATION_PLACE),
                true, true, true, true);
        add(type, list,
                new WikiDataEntryData("ニューヨーク","ニューヨーク州の都市","Q60","にゅーよーく", WikiDataEntryData.CLASSIFICATION_PLACE),
                true, true, true, true);
        add(type, list,
                new WikiDataEntryData("バンコク","タイ王国の首都","Q1861","ばんこく", WikiDataEntryData.CLASSIFICATION_PLACE),
                true, true, true, true);

        //important Japanese businessmen
        //https://japan.cnet.com/article/20073823/
        add(type, list,
                new WikiDataEntryData("孫正義","Zainichi Korean businessman","Q717038","そん まさよし", WikiDataEntryData.CLASSIFICATION_PERSON),
                true, false, true, false);
        add(type, list,
                new WikiDataEntryData("堀江貴文","日本の実業家、著述家","Q1193093","ほりえ たかふみ", WikiDataEntryData.CLASSIFICATION_PERSON),
                true, true, true, false);
        add(type, list,
                new WikiDataEntryData("三木谷浩史","Japanese businessman, co-founder and CEO of Rakuten","Q5366748","みきたに ひろし", WikiDataEntryData.CLASSIFICATION_PERSON),
                true, true, false, true);

        //google most famous businessmen
        add(type, list,
                new WikiDataEntryData("スティーブ・ジョブズ","アメリカ合衆国の実業家、資産家","Q19837","すてぃーぶ じょぶず", WikiDataEntryData.CLASSIFICATION_PERSON),
                true, false, true, false);
        add(type, list,
                new WikiDataEntryData("マーク・ザッカーバーグ","co-founder of social networking site Facebook","Q36215","まーく ざっかーばーぐ", WikiDataEntryData.CLASSIFICATION_PERSON),
                true, false, true, false);

        //famous Japanese brands (companies)
        //https://zuuonline.com/archives/125505
        add(type, list,
                new WikiDataEntryData("トヨタ自動車","日本の自動車メーカー","Q53268","とよたじどうしゃ", WikiDataEntryData.CLASSIFICATION_OTHER),
                true, false, false, false);
        add(type, list,
                new WikiDataEntryData("キヤノン","Japanese multinational corporation specialised in the manufacture of imaging and optical products","Q62621","きゃのん", WikiDataEntryData.CLASSIFICATION_OTHER),
                true, false, false, false);
        add(type, list,
                new WikiDataEntryData("任天堂","日本の企業","Q8093","にんてんどう", WikiDataEntryData.CLASSIFICATION_OTHER),
                false, false, true, true);
        add(type, list,
                new WikiDataEntryData("ユニクロ","Japanese casual wear designer, manufacturer and retailer","Q26070","ゆにくろ", WikiDataEntryData.CLASSIFICATION_OTHER),
                true, true, true, true);

        //famous global brands
        //http://fortune.com/worlds-most-admired-companies/
        add(type, list,
                new WikiDataEntryData("アップル","アメリカ合衆国のコンピュータ・ソフトウェア会社","Q312","あっぷる", WikiDataEntryData.CLASSIFICATION_OTHER),
                true, true, true, true);
        add(type, list,
                new WikiDataEntryData("Amazon.com","American electronic commerce and cloud computing company","Q3884","あまぞん", WikiDataEntryData.CLASSIFICATION_OTHER),
                true, true, true, true);
        add(type, list,
                new WikiDataEntryData("マイクロソフト","アメリカのコンピュータ・ソフトウェア会社","Q2283","まいくろそふと", WikiDataEntryData.CLASSIFICATION_OTHER),
                true, false, true, false);


        //intuition / my preferences
        //dead athletes
        add(type, list,
                new WikiDataEntryData("ベーブ・ルース","baseball player","Q213812","べーぶ るーす", WikiDataEntryData.CLASSIFICATION_PERSON),
                true, false, true, false);
        add(type, list,
                new WikiDataEntryData("ヨハン・クライフ","オランダのサッカー選手","Q17163","よはん くらいふ", WikiDataEntryData.CLASSIFICATION_PERSON),
                true, false, true, false);
        add(type, list,
                new WikiDataEntryData("ヨハン・クライフ","オランダのサッカー選手","Q17163","よはん くらいふ", WikiDataEntryData.CLASSIFICATION_PERSON),
                true, false, true, false);

        //ace of Jpn soccer
        add(type, list,
                new WikiDataEntryData("香川真司","日本のサッカー選手","Q154708","かがわ しんじ", WikiDataEntryData.CLASSIFICATION_PERSON),
                true, true, true, true);
        //businessmen for young people
        add(type, list,
                new WikiDataEntryData("宮本茂","日本のゲームクリエイター","Q12382","みやもと しげる", WikiDataEntryData.CLASSIFICATION_PERSON),
                false, false, true, true);
        //pop culture
        add(type, list,
                new WikiDataEntryData("HIKAKIN","日本のYouTuber","Q11222605","ひかきん", WikiDataEntryData.CLASSIFICATION_PERSON),
                false, false, true, true);
        add(type, list,
                new WikiDataEntryData("はじめしゃちょー","日本のYouTuber","Q24230271","はじめしゃちょー", WikiDataEntryData.CLASSIFICATION_PERSON),
                false, false, true, true);


        return list;
    }
    
    private static void add(int type, List<WikiDataEntryData> list, WikiDataEntryData data, boolean forMan, boolean forWoman, boolean forBoy, boolean forGirl){
        if (type == MAN && forMan){
            list.add(data);
        } else if (type == WOMAN && forWoman){
            list.add(data);
        } else if (type == BOY && forBoy){
            list.add(data);
        } else if (type == GIRL && forGirl){
            list.add(data);
        }
    }
}
