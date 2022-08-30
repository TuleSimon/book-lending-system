package com.abby.booklendingsystem.adaper.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.abby.booklendingsystem.databinding.CreditCardViewBinding
import com.abby.booklendingsystem.interfaces.CreditCardClick
import com.abby.booklendingsystem.model.CardsModel
import com.abby.booklendingsystem.ui.viewbook.ViewBookModel
import kotlinx.coroutines.launch


class CardsRecyclerViewAdapter(var ViewModel:ViewBookModel,var fragment:Fragment, var click:CreditCardClick?=null
    ) : RecyclerView.Adapter<CardsRecyclerViewAdapter.CardViewHolder>() {

    inner class CardViewHolder(val binding: CreditCardViewBinding):RecyclerView.ViewHolder(binding.root) {

        fun bind(cards: CardsModel){
            binding.card.setCardData(cards.holder.toString(),
                cards.cardnumber.toString(),cards.cvv.toString(),cards.expiry.toString()
                )

            click?.apply {
                binding.card.setOnClickListener {
                    this.click(cards)
                }
            }

            val selectedCard = ViewModel.selectedPaymentMethod

            selectedCard.observe(fragment.viewLifecycleOwner){
                if(it?.id!=cards.id){
                    binding.selectCard.isChecked=false
                }
            }

            binding.selectCard.setOnCheckedChangeListener { _, isChecked ->
                if(isChecked){
                    fragment.lifecycleScope.launch {
                        ViewModel.selectCard(cards)
                    }
                }
            }
        }
    }
    var cards = mutableListOf<CardsModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        return from(parent)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val cards = cards[position]
        holder.bind(cards)
    }

    fun from(parent: ViewGroup):CardViewHolder{
        val inflater = LayoutInflater.from(parent.context)
        val view =CreditCardViewBinding.inflate(inflater,parent,false)
        return CardViewHolder(view)
    }

    override fun getItemCount(): Int {
        return cards.size
    }

    fun setItems(booklist: List<CardsModel>){
        cards = booklist.toMutableList()
        notifyDataSetChanged()
    }

    fun removeItem(cardss: CardsModel) {
        val index = cards.indexOf(cardss)
        cards.removeAt(index)
        notifyItemRemoved(index)
    }

}

