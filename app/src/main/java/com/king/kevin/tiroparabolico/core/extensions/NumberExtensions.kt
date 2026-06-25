package com.king.kevin.tiroparabolico.core.extensions

import java.util.Locale

/**
 * Extensión para formatear un Double a String con una cantidad específica de decimales
 * para su visualización en la interfaz de usuario.
 */
fun Double.toDisplay(decimals: Int = 2): String = "%.${decimals}f".format(Locale.getDefault(), this)
