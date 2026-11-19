package io.github.fuzhengqu.cabincanvas.core

import io.github.fuzhengqu.cabincanvas.data.CarGlyph

enum class CarDestination(val label: String, val glyph: CarGlyph) {
    HOME("首页", CarGlyph.HOME),
    APPS("应用", CarGlyph.APPS),
    MEDIA("音乐", CarGlyph.MUSIC),
    PHONE("电话", CarGlyph.PHONE),
    SETTINGS("设置", CarGlyph.SETTINGS),
    VOICE("语音", CarGlyph.MIC),
}
