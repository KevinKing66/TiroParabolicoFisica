package com.king.kevin.tiroparabolico.core.utils

import java.util.Locale

fun Double.toDisplay(decimals: Int = 2): String = "%.${decimals}f".format(Locale.getDefault(), this)
