package com.yuk.miuihome

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.XModuleResources
import android.content.res.XResources
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import com.yuk.miuihome.HomeContext.drawableNameList
import com.yuk.miuihome.HomeContext.drawableNameNewList
import com.yuk.miuihome.HomeContext.isAlpha
import com.yuk.miuihome.HomeContext.versionCode
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam
import com.yuk.miuihome.utils.OwnSP
import com.yuk.miuihome.utils.dip2px
import com.yuk.miuihome.utils.ktx.setTryReplacement
import de.robv.android.xposed.callbacks.XC_LayoutInflated
import kotlin.concurrent.thread

class ResHook(private val hookedRes: InitPackageResourcesParam) {

    private val modRes = XModuleResources.createInstance(XposedInit.modulePath, hookedRes.res)
    private fun getResId(type: String, name: String): Int =
        modRes.getIdentifier(name, type, Config.packageName)

    companion object {
        private var hasLoad = false
    }

    fun init() {

        hookedRes.res.hookLayout(
            "com.miui.home",
            "layout",
            "layout_search_bar",
            object : XC_LayoutInflated() {
                override fun handleLayoutInflated(liparam: LayoutInflatedParam) {
                    // 替换资源圆角
                    val targetView = liparam.view
                    (if (isAlpha || versionCode >= 421153106L) drawableNameNewList else drawableNameList).forEach { drawableName ->
                        resetDockRadius(
                            hookedRes.res,
                            targetView.context,
                            drawableName
                        )
                    }
                }
            })

        thread {
            if (!hasLoad) {
                Thread.sleep(500) // 这里项目经理要求运行缓慢，好让客户充钱，让其速度得到明显提升。
                hasLoad = true
            }

            //后台卡片文字大小
            val backgroundTextSize = OwnSP.ownSP.getFloat("backgroundTextSize", 13f)
            hookedRes.res.setTryReplacement(
                Config.hookPackage,
                "dimen",
                "recents_task_view_header_title_text_size",
                modRes.fwd(
                    getResId("dimen", "sp${backgroundTextSize.toInt()}")
                )
            )

            //后台隐藏应用图标
            if (OwnSP.ownSP.getBoolean("buttonPadding", false)) {
                hookedRes.res.setTryReplacement(
                    Config.hookPackage,
                    "dimen",
                    "recents_task_view_header_button_padding",
                    modRes.fwd(
                        getResId("dimen", "sp100")
                    )
                )
            }

            //后台隐藏小窗应用
            if (OwnSP.ownSP.getBoolean("smallWindow", false)) {
                hookedRes.res.setTryReplacement(
                    Config.hookPackage,
                    "dimen",
                    "recent_tv_small_window_margin_start",
                    modRes.fwd(
                        getResId("dimen", "dp_100")
                    )
                )
            }

            //隐藏后台清理图标
            if (OwnSP.ownSP.getBoolean("cleanUp", false)) {
                hookedRes.res.setTryReplacement(
                    Config.hookPackage,
                    "drawable",
                    "btn_clear_all",
                    modRes.fwd(R.drawable.a)
                )
                hookedRes.res.setTryReplacement(
                    Config.hookPackage,
                    "drawable",
                    "notifications_clear_all",
                    modRes.fwd(R.drawable.a)
                )
            }

            //隐藏桌面应用名称
            if (OwnSP.ownSP.getBoolean("hideIconTitles", false)) {
                hookedRes.res.setTryReplacement(
                    Config.hookPackage,
                    "dimen",
                    "workspace_icon_text_size",
                    modRes.fwd(
                        getResId("dimen", "dp0")
                    )
                )
            }
            if (OwnSP.ownSP.getString("recentText", "YuKongADisable") != "YuKongADisable") {
                val message: String = OwnSP.ownSP.getString("recentText", "YuKongADisable").toString()
                hookedRes.res.setTryReplacement(
                    Config.hookPackage,
                    "string",
                    "recents_empty_message",
                    message
                )
            }
        }
    }

    private fun resetDockRadius(res: XResources, context: Context, drawableName: String) {
        if (OwnSP.ownSP.getBoolean(
                "dockSettings", false
            )
        ) {
            res.setReplacement(
                "com.miui.home",
                "drawable",
                drawableName,
                object : XResources.DrawableLoader() {
                    @SuppressLint("UseCompatLoadingForDrawables")
                    override fun newDrawable(xres: XResources, id: Int): Drawable {
                        val background = context.getDrawable(
                            xres.getIdentifier(
                                drawableName,
                                "drawable",
                                "com.miui.home"
                            )
                        ) as RippleDrawable
                        val backgroundShape = background.getDrawable(0) as GradientDrawable
                        backgroundShape.cornerRadius =
                            dip2px(
                                (OwnSP.ownSP.getFloat(
                                    "dockRadius",
                                    -1f
                                ) * 10).toInt()
                            ).toFloat()
                        backgroundShape.setStroke(0, 0)
                        background.setDrawable(0, backgroundShape)
                        return background
                    }
                })
        }
    }
}
