package io.github.fuzhengqu.cabincanvas.data

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap

/** A small, dependency-free icon vocabulary; mapped to Material icons in the design system. */
enum class CarGlyph {
    VOICE, NAVI, MUSIC, PHONE, APPS, SETTINGS, HOME, WORK, CALENDAR, WEATHER,
    NOTIFICATION, SEARCH, EDIT, SPLIT, BACK, CLOSE, MORE, PLAY, PAUSE, NEXT, PREV,
    VOLUME, BLUETOOTH, WIFI, BATTERY, SIGNAL, MIC, ADD, DELETE, CHECK, CHEVRON,
    STAR, CAR, MAP, QUEUE, FULLSCREEN, EXIT, INFO, LOCK, TUNE, ROUTE, PARKING,
    COFFEE, SHOPPING, RADIO, VIDEO, CHAT, CONTACTS, HISTORY, CALL, CALL_END,
    SPEAKER, MUTE, BRIGHTNESS, WALLPAPER, GRID, CALIBRATE, BUG, SHARE, REFRESH,
    USB, LINK, PERSON, DARK, LIGHT, POWER,
}

enum class CarAppCategory(val label: String) {
    ALL("全部"), NAVI("导航"), MUSIC("音乐"), VIDEO("视频"), TOOL("工具"), MINE("我的")
}

@Immutable
data class CarAppEntry(
    val label: String,
    val packageName: String,
    val icon: ImageBitmap? = null,
    val tint: Color,
    val category: CarAppCategory,
)

@Immutable
data class CarMedia(
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val accent: Color,
    val artwork: ImageBitmap? = null,
    val packageName: String? = null,
)

@Immutable
data class CarContact(
    val name: String,
    val number: String,
    val letter: String,
    val color: Color,
)

@Immutable
data class CarCallEntry(
    val contact: CarContact,
    val label: String,
    val date: Long,
)

@Immutable
data class CarNotification(
    val app: String,
    val title: String,
    val body: String,
    val time: String,
    val color: Color,
    val canReply: Boolean = false,
    val key: String? = null,
    val packageName: String? = null,
)

@Immutable
data class CarSuggestion(
    val title: String,
    val subtitle: String,
    val glyph: CarGlyph,
    val tint: Color,
)

@Immutable
data class CarForecast(
    val day: String,
    val temp: String,
    val condition: String,
)

@Immutable
data class CarWeather(
    val temperature: Int,
    val condition: String,
    val high: Int,
    val low: Int,
    val city: String,
    val forecast: List<CarForecast>,
)

@Immutable
data class CarRoute(
    val destination: String,
    val etaMinutes: Int,
    val distanceKm: Float,
    val progress: Float,
)

enum class CarCardType(val label: String, val glyph: CarGlyph) {
    MAP("地图卡", CarGlyph.MAP),
    MEDIA("媒体卡", CarGlyph.MUSIC),
    PHONE("电话卡", CarGlyph.PHONE),
    SUGGESTION("建议卡", CarGlyph.STAR),
    CALENDAR("日程卡", CarGlyph.CALENDAR),
    CLOCK("时钟卡", CarGlyph.WEATHER),
    HOME_WORK("家和公司", CarGlyph.HOME),
    BLANK("空白卡", CarGlyph.ADD),
}

enum class CarCardSize(val label: String) {
    SMALL("1×1"), MEDIUM("2×1"), LARGE("2×2")
}

@Immutable
data class CarCardConfig(
    val type: CarCardType,
    val enabled: Boolean = true,
    val size: CarCardSize = CarCardSize.MEDIUM,
)

object FakeCarData {
    val media = listOf(
        CarMedia("夜航星", "不才", "夜航星", 254_000, Color(0xFF2F6BFF)),
        CarMedia("起风了", "买辣椒也用券", "起风了", 325_000, Color(0xFF19A974)),
        CarMedia("Lemon", "米津玄師", "STRAY SHEEP", 255_000, Color(0xFFFFB020)),
    )

    val lyrics = listOf(
        "也许我命中注定 要一个人走",
        "穿过人海 穿过群山 穿过黑夜",
        "直到看见 属于我的那束光",
        "夜航星 请为我指引方向",
    )

    val contacts = listOf(
        CarContact("妈妈", "138 0000 1024", "妈", Color(0xFFE32E27)),
        CarContact("爸爸", "138 0000 2048", "爸", Color(0xFF2F6BFF)),
        CarContact("小李", "139 0000 4096", "李", Color(0xFF19A974)),
        CarContact("王工", "137 0000 8192", "王", Color(0xFFFFB020)),
        CarContact("物业", "400 000 1234", "物", Color(0xFF8B5CF6)),
        CarContact("救援", "400 000 9999", "救", Color(0xFF0EA5E9)),
    )

    val notifications = listOf(
        CarNotification("微信", "小李", "晚上一起吃饭吗？我订了位置。", "2 分钟前", Color(0xFF19A974), true),
        CarNotification("高德地图", "路况提醒", "前方 3 公里事故，建议绕行。", "5 分钟前", Color(0xFF2F6BFF)),
        CarNotification("网易云音乐", "每日推荐", "根据你的口味，今天有 30 首新歌。", "18 分钟前", Color(0xFFE32E27)),
        CarNotification("日历", "15:30 产品评审", "会议室 A · 提前 10 分钟出发", "1 小时前", Color(0xFFFFB020)),
        CarNotification("哔哩哔哩", "关注的 UP 主更新了", "《一台 2021 年的朗逸，能有多智能？》", "2 小时前", Color(0xFF0EA5E9)),
        CarNotification("系统", "CarLife 已连接", "USB 连接稳定，流分辨率 1280×720。", "刚刚", Color(0xFF6B7280)),
    )

    val suggestions = listOf(
        CarSuggestion("去公司", "预计 12 分钟 · 6.4 公里", CarGlyph.WORK, Color(0xFF2F6BFF)),
        CarSuggestion("回家", "预计 18 分钟 · 9.1 公里", CarGlyph.HOME, Color(0xFF19A974)),
        CarSuggestion("附近的咖啡", "3 家 · 最近 450 米", CarGlyph.COFFEE, Color(0xFFFFB020)),
    )

    val weather = CarWeather(
        temperature = 26,
        condition = "多云",
        high = 29,
        low = 21,
        city = "上海",
        forecast = listOf(
            CarForecast("今天", "26°", "多云"),
            CarForecast("明天", "28°", "晴"),
            CarForecast("周六", "24°", "小雨"),
        ),
    )

    val route = CarRoute("公司", 12, 6.4f, 0.42f)

    val fallbackApps = listOf(
        CarAppEntry("高德地图", "com.autonavi.minimap", null, Color(0xFF2F6BFF), CarAppCategory.NAVI),
        CarAppEntry("网易云音乐", "com.netease.cloudmusic", null, Color(0xFFE32E27), CarAppCategory.MUSIC),
        CarAppEntry("微信", "com.tencent.mm", null, Color(0xFF19A974), CarAppCategory.MINE),
        CarAppEntry("抖音", "com.ss.android.ugc.aweme", null, Color(0xFF111318), CarAppCategory.VIDEO),
        CarAppEntry("哔哩哔哩", "tv.danmaku.bili", null, Color(0xFF0EA5E9), CarAppCategory.VIDEO),
        CarAppEntry("Spotify", "com.spotify.music", null, Color(0xFF1DB954), CarAppCategory.MUSIC),
        CarAppEntry("汽水音乐", "com.luna.music", null, Color(0xFF8B5CF6), CarAppCategory.MUSIC),
        CarAppEntry("电话", "com.android.dialer", null, Color(0xFF2F6BFF), CarAppCategory.TOOL),
        CarAppEntry("设置", "com.android.settings", null, Color(0xFF6B7280), CarAppCategory.TOOL),
        CarAppEntry("相机", "com.android.camera", null, Color(0xFF111318), CarAppCategory.TOOL),
        CarAppEntry("相册", "com.android.gallery", null, Color(0xFFFFB020), CarAppCategory.TOOL),
        CarAppEntry("时钟", "com.android.deskclock", null, Color(0xFF0EA5E9), CarAppCategory.TOOL),
    )

    val voiceCommands = listOf("导航回家", "播放我喜欢的音乐", "给妈妈打电话", "打开应用", "今天天气怎么样")

    val calendar = listOf(
        "10:00 产品周会" to "会议室 A",
        "15:30 车机 UI 评审" to "会议室 B",
        "19:00 健身" to "徐汇健身房",
    )

    val parkedCards = listOf(
        "停车位置" to "B2 · 128 号",
        "车窗" to "已关闭",
        "续航/油量" to "62%",
    )
}
