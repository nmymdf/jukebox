package com.archiekuo.jukebox.data

import com.archiekuo.jukebox.R

/**
 * Built-in categories. Tags are Jamendo tags; the app asks Jamendo for songs that
 * match as many of the tags as possible. Users can add their own categories too.
 */
object Categories {
    private fun c(
        group: String,
        id: String,
        name: String,
        tags: String,
        vocal: String = "any",
        speed: String = "",
        image: Int = 0,
    ) = Cat(
        id = id,
        group = group,
        name = name,
        tags = if (tags.isBlank()) emptyList() else tags.split(","),
        vocal = vocal,
        speed = speed,
        image = image,
    )

    val builtin: List<Cat> = listOf(
        // ---- 人聲 ----
        c("人聲", "vocal_male", "男聲", "", "male", image = R.drawable.fav_2),
        c("人聲", "vocal_female", "女聲", "", "female", image = R.drawable.ch_2),
        c("人聲", "vocal_duet", "男女對唱", "duet", "vocal", image = R.drawable.fav_6),
        c("人聲", "vocal_choir", "合唱", "choir,chorus", "vocal", image = R.drawable.ch_5),
        c("人聲", "vocal_band", "樂團", "band,rock", "vocal", image = R.drawable.cat_pop),
        c("人聲", "instrumental", "純音樂", "", "instrumental", image = R.drawable.ch_3),

        // ---- 心情 ----
        c("心情", "mood_happy", "活潑", "happy,energetic,fun", image = R.drawable.feat_1),
        c("心情", "mood_calm", "平靜", "calm,relaxing,peaceful", image = R.drawable.cat_healing),
        c("心情", "mood_sad", "悲傷", "sad,melancholic"),
        c("心情", "mood_romantic", "浪漫", "romantic,love", image = R.drawable.ch_4),
        c("心情", "mood_motivation", "勵志", "motivational,inspiring,uplifting"),
        c("心情", "mood_nostalgic", "懷舊", "nostalgic,retro", image = R.drawable.ch_6),
        c("心情", "mood_mysterious", "神秘", "mysterious,dark"),
        c("心情", "mood_warm", "溫暖", "warm,cozy,soft"),
        c("心情", "mood_angry", "憤怒", "angry,aggressive"),

        // ---- 曲風 ----
        c("曲風", "genre_jazz", "爵士", "jazz", image = R.drawable.cat_jazz),
        c("曲風", "genre_blues", "藍調", "blues", image = R.drawable.fav_4),
        c("曲風", "genre_rock", "搖滾", "rock"),
        c("曲風", "genre_punk", "龐克", "punk"),
        c("曲風", "genre_metal", "金屬", "metal"),
        c("曲風", "genre_classical", "古典", "classical", image = R.drawable.cat_classic),
        c("曲風", "genre_electronic", "電子", "electronic", image = R.drawable.ch_1),
        c("曲風", "genre_folk", "民謠", "folk"),
        c("曲風", "genre_country", "鄉村", "country"),
        c("曲風", "genre_hiphop", "嘻哈", "hiphop"),
        c("曲風", "genre_rnb", "R&B", "rnb"),
        c("曲風", "genre_soul", "靈魂", "soul"),
        c("曲風", "genre_reggae", "雷鬼", "reggae"),
        c("曲風", "genre_latin", "拉丁", "latin"),
        c("曲風", "genre_world", "世界音樂", "world"),
        c("曲風", "genre_easy", "輕音樂", "easylistening,lounge", image = R.drawable.cat_easy),
        c("曲風", "genre_ambient", "氛圍", "ambient"),

        // ---- 情境 ----
        c("情境", "ctx_run", "跑步", "energetic,running,workout", speed = "high+veryhigh"),
        c("情境", "ctx_gym", "健身", "workout,energetic,gym", speed = "high+veryhigh"),
        c("情境", "ctx_bike", "騎車", "energetic,driving,roadtrip"),
        c("情境", "ctx_yoga", "瑜伽", "yoga,meditation,calm"),
        c("情境", "ctx_walk", "散步", "walking,acoustic,peaceful"),
        c("情境", "ctx_drive", "開車", "driving,roadtrip,rock"),
        c("情境", "ctx_commute", "通勤", "chill,lounge"),
        c("情境", "ctx_study", "讀書", "studying,focus,calm", "instrumental"),
        c("情境", "ctx_work", "工作", "focus,background,corporate", "instrumental"),
        c("情境", "ctx_focus", "專注", "focus,concentration,ambient", "instrumental"),
        c("情境", "ctx_chores", "做家事", "happy,upbeat,pop"),
        c("情境", "ctx_cook", "做菜", "jazz,bossanova,acoustic"),
        c("情境", "ctx_cafe", "咖啡廳", "coffee,cafe,jazz,lounge"),
        c("情境", "ctx_sleep", "睡前", "sleep,lullaby,ambient", speed = "verylow+low", image = R.drawable.fav_1),
        c("情境", "ctx_nap", "午休", "relaxing,calm,nap"),
        c("情境", "ctx_meditation", "冥想", "meditation,ambient,newage", "instrumental"),
        c("情境", "ctx_rain", "下雨天", "rain,sad,calm"),
        c("情境", "ctx_party", "派對", "party,dance,edm"),
        c("情境", "ctx_gather", "聚會", "happy,folk,acoustic"),
        c("情境", "ctx_date", "約會", "romantic,jazz,rnb"),
        c("情境", "ctx_travel", "旅行", "travel,world,folk", image = R.drawable.fav_5),
        c("情境", "ctx_camp", "露營", "acoustic,folk,nature"),
        c("情境", "ctx_xmas", "聖誕", "christmas,xmas"),
        c("情境", "ctx_summer", "夏天", "summer,reggae,latin"),
        c("情境", "ctx_beach", "海邊", "beach,tropical,reggae"),
    )
}
