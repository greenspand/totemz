package ro.cluj.totemz

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import java.util.*


class BaseFragAdapter(fm: FragmentManager, var fragments: ArrayList<Fragment>) : FragmentPagerAdapter(fm) {


    override fun getItem(position: Int): Fragment? {
        return this.fragments[position]
    }

    override fun getCount(): Int {
        // Show pages.
        return fragments.size
    }
}

