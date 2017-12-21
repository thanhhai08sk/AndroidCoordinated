package io.hainguyen.androidcoordinated.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.text.method.ArrowKeyMovementMethod
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast



/**
 * Created by HaiNguyen on 9/12/17.
 */

fun View.onClick(func: () -> Unit) {
    setOnClickListener { func.invoke() }
}

fun View.onLongClick(func: () -> Unit) {
    setOnLongClickListener {
        func.invoke()
        true
    }
}


fun View.showPopupMenu(menuRes: Int, onMenuClick: (menuId: Int) -> Unit) {
    val popup = PopupMenu(context, this)
    popup.setOnMenuItemClickListener { item ->
        onMenuClick.invoke(item.itemId)
        true
    }
    popup.inflate(menuRes)
    popup.show()
}

fun Activity.getContentView(): ViewGroup = findViewById(android.R.id.content) as ViewGroup


fun Context.inflateView(viewId: Int, parent: ViewGroup): View =
        LayoutInflater.from(this).inflate(viewId, parent, false)

fun ViewGroup.inflateView(viewId: Int): View =
        context.inflateView(viewId, this)

fun Activity.getColorCompat(id: Int): Int = ContextCompat.getColor(this, id)

fun Fragment.getColorCompat(id: Int): Int = activity.getColorCompat(id)
fun DialogFragment.setKeyboardAlwaysShow(focusView: View) {
    focusView.requestFocus()
    dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
}
fun Context.getColorCompat(id: Int) : Int = ContextCompat.getColor(this, id)
fun Context.dpToPixel(dp: Int): Int = (resources.displayMetrics.density * dp).toInt()


fun Activity.showKeyboard(editText: EditText) {
    editText.requestFocusFromTouch()
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED)
}

fun setViewsGone(vararg views: View) {
    views.forEach { it.gone() }
}

fun setViewsVisible(vararg views: View) {
    views.forEach { it.visible() }
}

fun EditText.disableEditting() {
//    isEnabled = false
    movementMethod = LinkMovementMethod.getInstance()
    isFocusable = false
    isClickable = true
    refreshLinks()
}

fun EditText.refreshLinks() {
    autoLinkMask = Linkify.WEB_URLS
    Linkify.addLinks(this, Linkify.WEB_URLS)
}

fun EditText.enableEditting() {
    isEnabled = true
    isFocusableInTouchMode = true
    movementMethod = ArrowKeyMovementMethod.getInstance()
}

fun View.gone() {
    visibility = View.GONE
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.visible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun ImageView.setColor(context: Context, colorId: Int) {
    setColorFilter(ContextCompat.getColor(context, colorId))
}

fun ImageView.setImageDrawableCompat(drawableId: Int) {
    setImageDrawable(ContextCompat.getDrawable(context, drawableId))
}


fun DrawerLayout.onOpened(doOnOpen: () -> Unit) {
    addDrawerListener(object : DrawerLayout.DrawerListener {
        override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
        override fun onDrawerOpened(drawerView: View) {
            doOnOpen.invoke()
        }
        override fun onDrawerClosed(drawerView: View) {}
        override fun onDrawerStateChanged(newState: Int) {}
    })
}

fun isBuildHigherThanKitkat(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP

fun EditText.pointCursorToEnd() {
    setSelection(text.length)
}

fun AppCompatActivity.finishWithTransition() {
    supportFinishAfterTransition()
}

fun Fragment.toast(messenger: String) {
    Toast.makeText(activity, messenger, Toast.LENGTH_SHORT).show()
}

fun Activity.toastLong(message: String) {
    Toast.makeText(this,message, Toast.LENGTH_LONG).show()
}

fun Activity.toast(message: String) {
    Toast.makeText(this,message, Toast.LENGTH_SHORT).show()
}

fun Context.toast(stringId: Int) {
    Toast.makeText(this, getString(stringId), Toast.LENGTH_SHORT).show()
}
fun Activity.startActivityWithTransition(intent: Intent) {
    ActivityCompat.startActivity(this, intent, ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle())
}

fun Fragment.toast(messengerRes: Int) {
    Toast.makeText(context, context.getString(messengerRes), Toast.LENGTH_SHORT).show()
}