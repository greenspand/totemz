package ro.cluj.totemz

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import java.util.*

/**
 * Created by Sorin Albu-Irimies on 8/27/2016.
 */
class BaseFragAdapter(fm: FragmentManager, var fragments: ArrayList<Fragment>) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment? {
        return this.fragments[position]
    }

    override fun getCount(): Int {
        return fragments.size
    }
}

