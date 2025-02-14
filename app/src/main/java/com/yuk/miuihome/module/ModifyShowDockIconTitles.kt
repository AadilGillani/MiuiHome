package com.yuk.miuihome.module

import android.content.Context
import com.yuk.miuihome.utils.OwnSP
import com.yuk.miuihome.utils.ktx.findClass
import com.yuk.miuihome.utils.ktx.hookAfterMethod
import com.yuk.miuihome.utils.ktx.setReturnConstant
import de.robv.android.xposed.XposedHelpers
import kotlin.math.roundToInt

class ModifyShowDockIconTitles {

    fun init() {
        if (OwnSP.ownSP.getBoolean("showDockIconTitles", false)) {
            "com.miui.home.launcher.DeviceConfig".setReturnConstant(
                "isHotseatsAppTitleHided",
                result = false
            )

            "com.miui.home.launcher.DeviceConfig".hookAfterMethod(
                "calcHotSeatsHeight",
                Context::class.java,
                Boolean::class.java
            ) {
                val context: Context = it.args[0] as Context
                val height = it.result as Int
                val sIsImmersiveNavigationBar = XposedHelpers.getStaticBooleanField(
                    "com.miui.home.launcher.DeviceConfig".findClass(), "sIsImmersiveNavigationBar"
                )
                if (sIsImmersiveNavigationBar) it.result =
                    (height + 8 * context.resources.displayMetrics.density).roundToInt()
            }
        }
    }
}