package me.aravi.commons.base

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar

abstract class BaseActivity : AppCompatActivity() {
    abstract val contentView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        onBeforeCreate()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val root = contentView
        setContentView(root)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, bars.bottom)
            insets
        }
        onViewReady(savedInstanceState, intent)
    }

    protected open fun onBeforeCreate() = Unit

    abstract fun onViewReady(savedInstanceState: Bundle?, intent: Intent?)

    fun statusBarHeight(resources: Resources): Int {
        val id = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (id > 0) resources.getDimensionPixelSize(id) else 0
    }

    fun openActivity(clazz: Class<*>) {
        startActivity(Intent(this, clazz))
    }

    fun snackbar(view: View, message: String, length: Int) {
        Snackbar.make(view, message, length).show()
    }

    fun transparentStatusBar() = Unit

    fun edge2Edge() {
        enableEdgeToEdge()
    }

    fun setBack() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}
