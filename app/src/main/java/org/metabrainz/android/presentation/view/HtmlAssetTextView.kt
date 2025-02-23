package org.metabrainz.android.presentation.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.HtmlCompat
import org.metabrainz.android.util.Utils

class HtmlAssetTextView(context: Context, attrs: AttributeSet?) : AppCompatTextView(context, attrs) {
    fun setAsset(asset: String?) {
        text = HtmlCompat.fromHtml(Utils.stringFromAsset(context, asset), HtmlCompat.FROM_HTML_MODE_LEGACY)
    }
}