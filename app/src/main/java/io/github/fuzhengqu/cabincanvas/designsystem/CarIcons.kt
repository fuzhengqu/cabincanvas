package io.github.fuzhengqu.cabincanvas.designsystem

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Brightness6
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.CallSplit
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Contacts
import androidx.compose.material.icons.rounded.Crop
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocalParking
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Usb
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import io.github.fuzhengqu.cabincanvas.data.CarGlyph

fun carIconVector(glyph: CarGlyph): ImageVector = when (glyph) {
    CarGlyph.VOICE -> Icons.Rounded.Mic
    CarGlyph.NAVI -> Icons.Rounded.Navigation
    CarGlyph.MUSIC -> Icons.Rounded.MusicNote
    CarGlyph.PHONE -> Icons.Rounded.Phone
    CarGlyph.APPS -> Icons.Rounded.Apps
    CarGlyph.SETTINGS -> Icons.Rounded.Settings
    CarGlyph.HOME -> Icons.Rounded.Home
    CarGlyph.WORK -> Icons.Rounded.Work
    CarGlyph.CALENDAR -> Icons.Rounded.CalendarMonth
    CarGlyph.WEATHER -> Icons.Rounded.WbSunny
    CarGlyph.NOTIFICATION -> Icons.Rounded.Notifications
    CarGlyph.SEARCH -> Icons.Rounded.Search
    CarGlyph.EDIT -> Icons.Rounded.Edit
    CarGlyph.SPLIT -> Icons.Rounded.CallSplit
    CarGlyph.BACK -> Icons.Rounded.ArrowBack
    CarGlyph.CLOSE -> Icons.Rounded.Close
    CarGlyph.MORE -> Icons.Rounded.MoreVert
    CarGlyph.PLAY -> Icons.Rounded.PlayArrow
    CarGlyph.PAUSE -> Icons.Rounded.Pause
    CarGlyph.NEXT -> Icons.Rounded.SkipNext
    CarGlyph.PREV -> Icons.Rounded.SkipPrevious
    CarGlyph.VOLUME -> Icons.Rounded.VolumeUp
    CarGlyph.BLUETOOTH -> Icons.Rounded.Bluetooth
    CarGlyph.WIFI -> Icons.Rounded.Wifi
    CarGlyph.BATTERY -> Icons.Rounded.BatteryFull
    CarGlyph.SIGNAL -> Icons.Rounded.SignalCellularAlt
    CarGlyph.MIC -> Icons.Rounded.Mic
    CarGlyph.ADD -> Icons.Rounded.Add
    CarGlyph.DELETE -> Icons.Rounded.Delete
    CarGlyph.CHECK -> Icons.Rounded.Check
    CarGlyph.CHEVRON -> Icons.Rounded.KeyboardArrowRight
    CarGlyph.STAR -> Icons.Rounded.Star
    CarGlyph.CAR -> Icons.Rounded.DirectionsCar
    CarGlyph.MAP -> Icons.Rounded.Map
    CarGlyph.QUEUE -> Icons.Rounded.QueueMusic
    CarGlyph.FULLSCREEN -> Icons.Rounded.Fullscreen
    CarGlyph.EXIT -> Icons.Rounded.FullscreenExit
    CarGlyph.INFO -> Icons.Rounded.Info
    CarGlyph.LOCK -> Icons.Rounded.Lock
    CarGlyph.TUNE -> Icons.Rounded.Tune
    CarGlyph.ROUTE -> Icons.Rounded.Navigation
    CarGlyph.PARKING -> Icons.Rounded.LocalParking
    CarGlyph.COFFEE -> Icons.Rounded.LocalCafe
    CarGlyph.SHOPPING -> Icons.Rounded.ShoppingBag
    CarGlyph.RADIO -> Icons.Rounded.Radio
    CarGlyph.VIDEO -> Icons.Rounded.VideoLibrary
    CarGlyph.CHAT -> Icons.Rounded.Chat
    CarGlyph.CONTACTS -> Icons.Rounded.Contacts
    CarGlyph.HISTORY -> Icons.Rounded.History
    CarGlyph.CALL -> Icons.Rounded.Call
    CarGlyph.CALL_END -> Icons.Rounded.CallEnd
    CarGlyph.SPEAKER -> Icons.Rounded.VolumeUp
    CarGlyph.MUTE -> Icons.Rounded.VolumeOff
    CarGlyph.BRIGHTNESS -> Icons.Rounded.Brightness6
    CarGlyph.WALLPAPER -> Icons.Rounded.Wallpaper
    CarGlyph.GRID -> Icons.Rounded.GridView
    CarGlyph.CALIBRATE -> Icons.Rounded.Crop
    CarGlyph.BUG -> Icons.Rounded.BugReport
    CarGlyph.SHARE -> Icons.Rounded.Share
    CarGlyph.REFRESH -> Icons.Rounded.Refresh
    CarGlyph.USB -> Icons.Rounded.Usb
    CarGlyph.LINK -> Icons.Rounded.Link
    CarGlyph.PERSON -> Icons.Rounded.Person
    CarGlyph.DARK -> Icons.Rounded.DarkMode
    CarGlyph.LIGHT -> Icons.Rounded.LightMode
    CarGlyph.POWER -> Icons.Rounded.PowerSettingsNew
}

@Composable
fun CarIcon(
    glyph: CarGlyph,
    modifier: Modifier = Modifier,
    size: Dp = LocalCarDimens.current.icon,
    tint: androidx.compose.ui.graphics.Color = LocalCarPalette.current.textPrimary,
    contentDescription: String? = null,
) {
    Icon(
        imageVector = carIconVector(glyph),
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier.size(size),
    )
}
