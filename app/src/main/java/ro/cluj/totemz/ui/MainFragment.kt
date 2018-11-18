package ro.cluj.totemz.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.transaction
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.main_fragment.*
import ro.cluj.totemz.R
import ro.cluj.totemz.ui.user.UserFragment

const val PARAM_USER_NAME = "param-user-name"

class MainFragment : Fragment() {

  companion object {
    fun newInstance() = MainFragment()
  }

  private lateinit var fragmentViewModel: MainFragmentViewModel

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? = inflater.inflate(R.layout.main_fragment, container, false)

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    fragmentViewModel = ViewModelProviders.of(this).get(MainFragmentViewModel::class.java)
    // TODO: Use the ViewModel
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    bottomNavigation.setOnNavigationItemSelectedListener {
      when (it.itemId) {
        R.id.action_chat_groups -> {
        }
        R.id.action_map -> {
        }
        R.id.action_user -> {
          childFragmentManager.transaction {
            replace(R.id.frag_container, UserFragment.newInstance(), "user-fragment")
          }
        }
      }
      return@setOnNavigationItemSelectedListener true
    }
  }
}
