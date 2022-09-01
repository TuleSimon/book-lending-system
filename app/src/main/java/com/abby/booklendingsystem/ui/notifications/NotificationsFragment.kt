package com.abby.booklendingsystem.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.abby.booklendingsystem.MainActivity
import com.abby.booklendingsystem.R
import com.abby.booklendingsystem.adaper.recyclerview.NotificationRecyclerViewAdapter
import com.abby.booklendingsystem.databinding.FragmentNotificationsBinding
import com.abby.booklendingsystem.enums.NetworkResult
import com.abby.booklendingsystem.utils.Utils
import com.abby.booklendingsystem.utils.show
import es.dmoral.toasty.Toasty
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator
import kotlinx.coroutines.launch

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    val notificationsViewModel by activityViewModels<NotificationsViewModel>()
    val adapters by lazy {
        NotificationRecyclerViewAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        doNotifications()
        initRecyclerView()
        return root
    }



    private fun doNotifications() = lifecycleScope.launch{


        notificationsViewModel.notifications.observe(viewLifecycleOwner){
            binding.booksRecyclerview.hideShimmer()
            when(it){

                is NetworkResult.Loading ->{
                    binding.booksRecyclerview.showShimmer()
                }
                is NetworkResult.Error -> {
                    binding.errorlayout.root.show()
                    binding.errorlayout.retryButton.setOnClickListener {
                        getNotificationss()
                    }
                }
                is NetworkResult.Success -> {
                    val data = it.data
                    data?.apply {
                        if(this.isEmpty()){
                            binding.errorlayout.root.show()
                            binding.errorlayout.imageView.setImageResource(R.drawable.ic_notifications_black_24dp)
                            binding.errorlayout.textView.text = "No data Yet"
                            binding.errorlayout.retryButton.setOnClickListener {
                                getNotificationss()
                            }
                            return@apply
                        }
                        adapters.setItems(this)
                        if(this.toMutableList().filter { noti -> noti.read==false }.isNotEmpty()) {
                            notificationsViewModel.updateRead()
                            (requireActivity() as MainActivity).hideBadge()
                        }
                    }

                }
            }
        }

        if(adapters.itemCount==0)
            getNotificationss()
        else
            (requireActivity() as MainActivity).hideBadge()
    }

    private fun getNotificationss() =lifecycleScope.launch{
        notificationsViewModel.getNotifications()
    }

    private fun initRecyclerView(){
        val layout = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        binding.booksRecyclerview.layoutManager = layout
        binding.booksRecyclerview.adapter = adapters
        binding.booksRecyclerview.itemAnimator = SlideInLeftAnimator()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}