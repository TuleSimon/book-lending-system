package com.abby.booklendingsystem.adaper.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.abby.booklendingsystem.R
import com.abby.booklendingsystem.databinding.CreditCardViewBinding
import com.abby.booklendingsystem.databinding.NotificationRowBinding
import com.abby.booklendingsystem.interfaces.CreditCardClick
import com.abby.booklendingsystem.model.CardsModel
import com.abby.booklendingsystem.model.NotificationModel
import com.abby.booklendingsystem.ui.viewbook.ViewBookModel
import com.abby.booklendingsystem.utils.setIfNotNull
import kotlinx.coroutines.launch


class NotificationRecyclerViewAdapter( ) : RecyclerView.Adapter<NotificationRecyclerViewAdapter.CardViewHolder>() {

    inner class CardViewHolder(val binding: NotificationRowBinding):RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: NotificationModel){
                binding.notTitle.setIfNotNull(notification.title)
            binding.notBody.setIfNotNull(notification.message)
            if(notification.read==false){
                binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context,R.color.grey_light))
            }
        }

    }
    var notification = mutableListOf<NotificationModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        return from(parent)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val notification = notification[position]
        holder.bind(notification)
    }

    fun from(parent: ViewGroup):CardViewHolder{
        val inflater = LayoutInflater.from(parent.context)
        val view =NotificationRowBinding.inflate(inflater,parent,false)
        return CardViewHolder(view)
    }

    override fun getItemCount(): Int {
        return notification.size
    }

    fun setItems(booklist: List<NotificationModel>){
        notification = booklist.toMutableList()
        notifyDataSetChanged()
    }

    fun removeItem(cardss: NotificationModel) {
        val index = notification.indexOf(cardss)
        notification.removeAt(index)
        notifyItemRemoved(index)
    }

}

