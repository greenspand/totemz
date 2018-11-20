package ro.cluj.totemz.ui.chat

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import ro.cluj.totemz.R

class ChatGroupsFragment : Fragment() {

    companion object {
        fun newInstance() = ChatGroupsFragment()
    }

    private lateinit var viewModel: ChatGroupsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.chat_groups_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ChatGroupsViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
