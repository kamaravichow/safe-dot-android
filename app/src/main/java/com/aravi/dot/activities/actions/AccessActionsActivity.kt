package com.aravi.dot.activities.actions

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.aravi.dot.databinding.ActivityAccessActionsBinding
import me.aravi.commons.base.BaseActivity

class AccessActionsActivity : BaseActivity() {

    private lateinit var binding: ActivityAccessActionsBinding

    override val contentView: View
        get() {
            binding = ActivityAccessActionsBinding.inflate(layoutInflater)
            return binding.root
        }

    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        setSupportActionBar(binding.toolbar)
        setBack()
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}
