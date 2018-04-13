package dev.olog.msc.presentation.about

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.TypedValue
import android.view.Gravity
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.ViewSwitcher
import dev.olog.msc.R
import dev.olog.msc.presentation.base.BaseActivity
import dev.olog.msc.utils.k.extension.dimen
import dev.olog.msc.utils.k.extension.subscribe
import kotlinx.android.synthetic.main.activity_about.*
import javax.inject.Inject

class AboutActivity : BaseActivity() {

    @Inject lateinit var adapter: AboutActivityAdapter
    @Inject lateinit var presenter: AboutActivityPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter

        switcher.setFactory(factory)
        switcher.setCurrentText(getString(R.string.about))
        setInAnimation()

        presenter.observeData()
                .subscribe(this, adapter::updateDataSet)

    }

    override fun onAttachFragment(fragment: Fragment?) {
        super.onAttachFragment(fragment)
        setInAnimation()
    }

    override fun onResume() {
        super.onResume()
        back.setOnClickListener { onBackPressed() }
    }

    override fun onPause() {
        super.onPause()
        back.setOnClickListener(null)
    }

    override fun onBackPressed() {
        setOutAnimation()
        val stack = supportFragmentManager.backStackEntryCount
        if (stack == 1){
            switcher.setText(getString(R.string.about))
        }

        super.onBackPressed()
    }

    private val factory = ViewSwitcher.ViewFactory {
        val textView = TextView(this@AboutActivity)
        textView.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
        )
        textView.gravity = Gravity.CENTER
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, dimen(R.dimen.common_header_size).toFloat())
        textView.typeface = Typeface.DEFAULT_BOLD
        textView.setTextColor(ColorStateList.valueOf(
                ContextCompat.getColor(this@AboutActivity, R.color.text_color_primary)))
        textView
    }

    private fun setInAnimation(){
        setSwitcherAnimation(R.anim.slide_in_bottom, R.anim.slide_out_bottom)
    }

    private fun setOutAnimation(){
        setSwitcherAnimation(R.anim.slide_in_top, R.anim.slide_out_top)
    }

    private fun setSwitcherAnimation(inAnimation: Int, outAnimation: Int){
        val inAnim = AnimationUtils.loadAnimation(this, inAnimation)
        val outAnim = AnimationUtils.loadAnimation(this, outAnimation)
        switcher.inAnimation = inAnim
        switcher.outAnimation = outAnim
    }

}