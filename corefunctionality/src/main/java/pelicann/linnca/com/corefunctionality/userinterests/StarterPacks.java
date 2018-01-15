package pelicann.linnca.com.corefunctionality.userinterests;

import java.util.ArrayList;
import java.util.List;

public class StarterPacks {
    public static final int MAN = 1;
    public static final int WOMAN = 2;
    public static final int BOY = 3;
    public static final int GIRL = 4;

    int type;

    private StarterPacks(){}

    public StarterPacks(int type){
        this.type = type;
    }

    //commented lines are reasons why we chose them
    public List<WikiDataEntity> getStarterPack(){
        List<WikiDataEntity> list = new ArrayList<>();
        //since most of the data is for general rankings and not
        // age/gender distributed rankings,
        // the initial list will be based on general rankings + my intuition.
        //after we have enough people using our app, base the starter packs
        // based on user data

        //top 100 well known people (水曜日のダウンタウン)
        add(type, list,
                new WikiDataEntity("タモリ","日本の芸能人","Q850746","たもり", WikiDataEntity.CLASSIFICATION_PERSON),
                true, true, true, true);
        add(type, list,
                new WikiDataEntity("和田アキ子","Zainichi Korean singer","Q710937","わだ あきこ", WikiDataEntity.CLASSIFICATION_PERSON),
                true, true, false, false);
        add(type, list,
                new WikiDataEntity("マツコ・デラックス","日本のコラムニスト、エッセイスト、女装タレント","Q813982","まつこ・でらっくす", WikiDataEntity.CLASSIFICATION_PERSON),
                true, true, true, true);
        add(type, list,
                new WikiDataEntity("木村拓哉", "Japanese actor and singer","Q362254","きむら たくや", WikiDataEntity.CLASSIFICATION_PERSON),
                false, true, false, false);
        add(type, list,
                new WikiDataEntity("田中将大","日本の野球選手","Q911463","たなか まさひろ", WikiDataEntity.CLASSIFICATION_PERSON),
                true, false, true, false);
        add(type, list,
                new WikiDataEntity("指原莉乃","Japanese female singer, member of HKT48, former member of AKB48, and producer/manager of =Love","Q181881","さしはら りの", WikiDataEntity.CLASSIFICATION_PERSON),
                true, true, true, true);
        add(type, list,
                new WikiDataEntity("吉田沙保里","Japanese sport wrestler","Q230985","よしだ さおり", WikiDataEntity.CLASSIFICATION_PERSON),
                false, true, false, true);
        add(type, list,
                new WikiDataEntity("浅田真央","日本のフィギュアスケート選手","Q234577","あさだ まお", WikiDataEntity.CLASSIFICATION_PERSON),
                false, true, false, true);
        add(type, list,
                new WikiDataEntity("福山雅治","Japanese singer-songwriter","Q1141809","ふくやま まさはる", WikiDataEntity.CLASSIFICATION_PERSON),
                false, true, false, true);
        add(type, list,
                new WikiDataEntity("福原愛","日本の卓球選手","Q239371","ふくはら あい", WikiDataEntity.CLASSIFICATION_PERSON),
                false, true, false, true);
        add(type, list,
                new WikiDataEntity("安倍晋三","内閣総理大臣","Q132345","あべ しんぞう", WikiDataEntity.CLASSIFICATION_PERSON),
                true, true, true, true);
        add(type, list,
                new WikiDataEntity("小池百合子","日本の政治家","Q261703","こいけ ゆりこ", WikiDataEntity.CLASSIFICATION_PERSON),
                true, true, true, true);
        add(type, list,
                new WikiDataEntity("古坂大魔王","お笑い芸人","Q11410993","こさか だいまおう", WikiDataEntity.CLASSIFICATION_PERSON),
                true, true, true, true);
        add(type, list,
                new WikiDataEntity("羽生結弦","日本のフィギュアスケート選手","Q597023","はにゅう ゆづる", WikiDataEntity.CLASSIFICATION_PERSON),
                false, true, false, true);
        add(type, list,
                new WikiDataEntity("東国原英夫","Japanese actor-politician","Q730869","ひがしこくばる ひでお", WikiDataEntity.CLASSIFICATION_PERSON),
                true, true, false, false);
        add(type, list,
                new WikiDataEntity("中居正広","司会者、俳優、歌手","Q1205631","なかい まさひろ", WikiDataEntity.CLASSIFICATION_PERSON),
                true, true, true, true);
        add(type, list,
                new WikiDataEntity("小泉純一郎","内閣総理大臣","Q130852","こいずみ じゅんいちろう", WikiDataEntity.CLASSIFICATION_PERSON),
                true, true, true, true);
        add(type, list,
                new WikiDataEntity("石原慎太郎","政治家、作家","Q38849","いしはら しんたろう", WikiDataEntity.CLASSIFICATION_PERSON),
                true, true, false, false);
        add(type, list,
                new WikiDataEntity("きゃりーぱみゅぱみゅ","日本のファッションモデル、歌手、ブロガー","Q242300","きゃりーぱみゅぱみゅ", WikiDataEntity.CLASSIFICATION_PERSON),
                false, false, false, true);
        add(type, list,
                new WikiDataEntity("二宮和也","Japanese actor","Q362277","にのみや かずなり", WikiDataEntity.CLASSIFICATION_PERSON),
                false, true, false, true);
        add(type, list,
                new WikiDataEntity("芦田愛菜","日本の子役","Q1135649","あしだ まな", WikiDataEntity.CLASSIFICATION_PERSON),
                true, true, true, true);

        //google most famous people
        add(type, list,
                new WikiDataEntity("バラク・オバマ","アメリカ合衆国の政治家、第44代大統領","Q76","ばらく おばま", WikiDataEntity.CLASSIFICATION_PERSON),
                true, true, true, true);
        add(type, list,
                new WikiDataEntity("ジャスティン・ビーバー","Canadian singer-songwriter, record producer and actor","Q34086","じゃすてぃん びーばー", WikiDataEntity.CLASSIFICATION_PERSON),
                true, true, true, true);
        add(type, list,
                new WikiDataEntity("ドナルド・トランプ","アメリカ合衆国の政治家、第45代大統領","Q22686","どなるど とらんぷ", WikiDataEntity.CLASSIFICATION_PERSON),
                true, true, true, true);
        add(type, list,
                new WikiDataEntity("エイブラハム・リンカーン","アメリカ合衆国の政治家","Q91","えいぶらはむ りんかーん", WikiDataEntity.CLASSIFICATION_PERSON),
                true, false, true, false);
        add(type, list,
                new WikiDataEntity("テイラー・スウィフト","アメリカ合衆国のカントリー・ミュージック歌手、シンガーソングライター","Q26876","ていらー すうぃふと", WikiDataEntity.CLASSIFICATION_PERSON),
                false, true, false, true);

        //国民的アニメ
        // https://www.anikore.jp/tag/%E5%9B%BD%E6%B0%91%E7%9A%84%E3%82%A2%E3%83%8B%E3%83%A1/
        add(type, list,
                new WikiDataEntity("野比のび太","藤子・F・不二雄の漫画『ドラえもん』の登場人物","Q1367960","のび のびた", WikiDataEntity.CLASSIFICATION_PERSON),
                false, false, true, true);
        add(type, list,
                new WikiDataEntity("工藤 新一","character in Detective Conan","Q844697","くどう しんいち", WikiDataEntity.CLASSIFICATION_PERSON),
                false, false, true, true);
        add(type, list,
                new WikiDataEntity("孫悟空","漫画『ドラゴンボール』の登場人物、主人公","Q2142","そん ごくう", WikiDataEntity.CLASSIFICATION_PERSON),
                false, false, true, false);
        add(type, list,
                new WikiDataEntity("月野うさぎ","fictional character from the franchise of the same name","Q757015","つきの うさぎ", WikiDataEntity.CLASSIFICATION_PERSON),
                false, false, false, true);
        add(type, list,
                new WikiDataEntity("モンキー・D・ルフィ","fictional character from One Piece","Q477948","もんきー でぃー るふぃー", WikiDataEntity.CLASSIFICATION_PERSON),
                false, false, true, false);

        //ESPN most famous athletes
        add(type, list,
                new WikiDataEntity("イチロー","Japanese-born baseball player","Q600","いちろー", WikiDataEntity.CLASSIFICATION_PERSON),
                true, false, true, false);
        add(type, list,
                new WikiDataEntity("錦織圭","日本のプロテニス選手","Q311222","にしこり けい", WikiDataEntity.CLASSIFICATION_PERSON),
                true, true, true, true);
        add(type, list,
                new WikiDataEntity("セリーナ・ウィリアムズ","American tennis player","Q11459","せりーな・うぃりあむず", WikiDataEntity.CLASSIFICATION_PERSON),
                false, true, false, true);
        //general countries
        add(type, list,
                new WikiDataEntity("アメリカ合衆国","北アメリカ大陸および北太平洋に位置する連邦共和国","Q30","あめりかがっしゅうこく", WikiDataEntity.CLASSIFICATION_PLACE),
                true, true, true, true);
        add(type, list,
                new WikiDataEntity("日本","東アジアに位置する国家","Q17","にっぽん", WikiDataEntity.CLASSIFICATION_PLACE),
                true, true, true, true);
        add(type, list,
                new WikiDataEntity("中華人民共和国","東アジアに位置する国家","Q148","ちゅうかじんみんきょうわこく", WikiDataEntity.CLASSIFICATION_PLACE),
                true, true, true, true);
        add(type, list,
                new WikiDataEntity("イギリス","ヨーロッパの国","Q145","いぎりす", WikiDataEntity.CLASSIFICATION_PLACE),
                true, true, true, true);
        //capital
        add(type, list,
                new WikiDataEntity("東京都","日本の47都道府県のひとつ。国政の首都機能を有する","Q1490","とうきょうと", WikiDataEntity.CLASSIFICATION_PLACE),
                true, true, true, true);
        //popular Japanese cities
        add(type, list,
                new WikiDataEntity("大阪市","大阪府にある市。政令指定都市のひとつ","Q35765","おおさかし", WikiDataEntity.CLASSIFICATION_PLACE),
                true, true, true, true);
        add(type, list,
                new WikiDataEntity("京都市","日本の市。政令指定都市。京都府庁所在地","Q34600","きょうとし", WikiDataEntity.CLASSIFICATION_PLACE),
                true, true, true, true);
        //popular cities
        add(type, list,
                new WikiDataEntity("パリ","フランスの首都","Q90","ぱり", WikiDataEntity.CLASSIFICATION_PLACE),
                false, true, false, true);
        add(type, list,
                new WikiDataEntity("ローマ","イタリアの首都","Q220","ろーま", WikiDataEntity.CLASSIFICATION_PLACE),
                true, true, true, true);
        add(type, list,
                new WikiDataEntity("ニューヨーク","ニューヨーク州の都市","Q60","にゅーよーく", WikiDataEntity.CLASSIFICATION_PLACE),
                true, true, true, true);
        add(type, list,
                new WikiDataEntity("バンコク","タイ王国の首都","Q1861","ばんこく", WikiDataEntity.CLASSIFICATION_PLACE),
                true, true, true, true);

        //important Japanese businessmen
        //https://japan.cnet.com/article/20073823/
        add(type, list,
                new WikiDataEntity("孫正義","Zainichi Korean businessman","Q717038","そん まさよし", WikiDataEntity.CLASSIFICATION_PERSON),
                true, false, true, false);
        add(type, list,
                new WikiDataEntity("堀江貴文","日本の実業家、著述家","Q1193093","ほりえ たかふみ", WikiDataEntity.CLASSIFICATION_PERSON),
                true, true, true, false);
        add(type, list,
                new WikiDataEntity("三木谷浩史","Japanese businessman, co-founder and CEO of Rakuten","Q5366748","みきたに ひろし", WikiDataEntity.CLASSIFICATION_PERSON),
                true, true, false, true);

        //google most famous businessmen
        add(type, list,
                new WikiDataEntity("スティーブ・ジョブズ","アメリカ合衆国の実業家、資産家","Q19837","すてぃーぶ じょぶず", WikiDataEntity.CLASSIFICATION_PERSON),
                true, false, true, false);
        add(type, list,
                new WikiDataEntity("マーク・ザッカーバーグ","co-founder of social networking site Facebook","Q36215","まーく ざっかーばーぐ", WikiDataEntity.CLASSIFICATION_PERSON),
                true, false, true, false);

        //famous Japanese brands (companies)
        //https://zuuonline.com/archives/125505
        add(type, list,
                new WikiDataEntity("トヨタ自動車","日本の自動車メーカー","Q53268","とよたじどうしゃ", WikiDataEntity.CLASSIFICATION_OTHER),
                true, false, false, false);
        add(type, list,
                new WikiDataEntity("キヤノン","Japanese multinational corporation specialised in the manufacture of imaging and optical products","Q62621","きゃのん", WikiDataEntity.CLASSIFICATION_OTHER),
                true, false, false, false);
        add(type, list,
                new WikiDataEntity("任天堂","日本の企業","Q8093","にんてんどう", WikiDataEntity.CLASSIFICATION_OTHER),
                false, false, true, true);
        add(type, list,
                new WikiDataEntity("ユニクロ","Japanese casual wear designer, manufacturer and retailer","Q26070","ゆにくろ", WikiDataEntity.CLASSIFICATION_OTHER),
                true, true, true, true);

        //famous global brands
        //http://fortune.com/worlds-most-admired-companies/
        add(type, list,
                new WikiDataEntity("アップル","アメリカ合衆国のコンピュータ・ソフトウェア会社","Q312","あっぷる", WikiDataEntity.CLASSIFICATION_OTHER),
                true, true, true, true);
        add(type, list,
                new WikiDataEntity("Amazon.com","American electronic commerce and cloud computing company","Q3884","あまぞん", WikiDataEntity.CLASSIFICATION_OTHER),
                true, true, true, true);
        add(type, list,
                new WikiDataEntity("マイクロソフト","アメリカのコンピュータ・ソフトウェア会社","Q2283","まいくろそふと", WikiDataEntity.CLASSIFICATION_OTHER),
                true, false, true, false);


        //intuition / my preferences
        //dead athletes
        add(type, list,
                new WikiDataEntity("ベーブ・ルース","baseball player","Q213812","べーぶ るーす", WikiDataEntity.CLASSIFICATION_PERSON),
                true, false, true, false);
        add(type, list,
                new WikiDataEntity("ヨハン・クライフ","オランダのサッカー選手","Q17163","よはん くらいふ", WikiDataEntity.CLASSIFICATION_PERSON),
                true, false, true, false);
        add(type, list,
                new WikiDataEntity("ヨハン・クライフ","オランダのサッカー選手","Q17163","よはん くらいふ", WikiDataEntity.CLASSIFICATION_PERSON),
                true, false, true, false);

        //ace of Jpn soccer
        add(type, list,
                new WikiDataEntity("香川真司","日本のサッカー選手","Q154708","かがわ しんじ", WikiDataEntity.CLASSIFICATION_PERSON),
                true, true, true, true);
        //businessmen for young people
        add(type, list,
                new WikiDataEntity("宮本茂","日本のゲームクリエイター","Q12382","みやもと しげる", WikiDataEntity.CLASSIFICATION_PERSON),
                false, false, true, true);
        //pop culture
        add(type, list,
                new WikiDataEntity("HIKAKIN","日本のYouTuber","Q11222605","ひかきん", WikiDataEntity.CLASSIFICATION_PERSON),
                false, false, true, true);
        add(type, list,
                new WikiDataEntity("はじめしゃちょー","日本のYouTuber","Q24230271","はじめしゃちょー", WikiDataEntity.CLASSIFICATION_PERSON),
                false, false, true, true);


        return list;
    }
    
    private static void add(int type, List<WikiDataEntity> list, WikiDataEntity data, boolean forMan, boolean forWoman, boolean forBoy, boolean forGirl){
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
