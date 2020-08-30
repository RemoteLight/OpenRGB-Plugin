package de.lars.openrgbwrapper.models

data class Mode(val name: String, val value: Int, val flags: Int, val speedMin: Int, val speedMax: Int, val colorMin: Int, val colorMax: Int, val speed: Int, val direction: Int, val colorMode: Int, val colors: Array<Color>) {

}
