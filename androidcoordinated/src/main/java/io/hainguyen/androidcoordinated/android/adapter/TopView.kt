package org.de_studio.diary.android.adapter

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.facebook.ads.AdChoicesView
import com.facebook.ads.MediaView
import com.facebook.ads.NativeAd
import io.hainguyen.androidcoordinated.R
import org.de_studio.diary.R
import org.de_studio.diary.utils.extensionFunction.find
import timber.log.Timber

/**
 * Created by HaiNguyen on 10/13/17.
 */
sealed class TopView(val layoutRes: Int, val viewId: Int){
    abstract fun bindView(view: View)

    class Ad(private val ad: NativeAd): TopView(R.layout.native_ad_facebook_entries, R.id.native_ad_unit){
        override fun bindView(view: View) {
            val adIcon: ImageView = view.find(R.id.native_ad_icon)
            val adTitle: TextView = view.find(R.id.native_ad_title)
            val adMedia: MediaView = view.find(R.id.native_ad_media)
            val adBody: TextView = view.find(R.id.native_ad_body)
            val adAction: Button = view.find(R.id.native_ad_call_to_action)
            val adChoiceContainer: LinearLayout = view.find(R.id.ad_choices_container)


            adTitle.text = ad.adTitle
            adBody.text = ad.adBody
            adAction.text = ad.adCallToAction
            NativeAd.downloadAndDisplayImage(ad.adIcon, adIcon)
            val adChoicesView = AdChoicesView(view.context, ad, true)
            adChoiceContainer.removeAllViews()
            adChoiceContainer.addView(adChoicesView)
            val clickableViews = arrayListOf(adTitle, adAction, view)
            ad.registerViewForInteraction(view, clickableViews)
            adMedia.setNativeAd(ad)
        }
    }

    class Title(private val title: String) : TopView(R.layout.items_group_title, R.id.group_title) {
        override fun bindView(view: View) {
            val titleText: TextView = view.find(R.id.group_title)
            titleText.text = title
        }
    }
}