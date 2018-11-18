package ro.cluj.totemz.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import ro.cluj.totemz.R

class UserFragment : Fragment() {

  companion object {
    fun newInstance() = UserFragment()
  }

  private lateinit var viewModel: UserViewModel

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? = inflater.inflate(R.layout.user_fragment, container, false)

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    viewModel = ViewModelProviders.of(this).get(UserViewModel::class.java)
    // TODO: Use the ViewModel
  }
}
