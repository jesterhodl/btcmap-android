package area

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import org.btcmap.databinding.ItemAreaElementBinding
import org.btcmap.databinding.ItemMapBinding
import org.osmdroid.util.BoundingBox

class AreaAdapter(
    private val listener: Listener,
) : ListAdapter<AreaAdapter.Item, AreaAdapter.ItemViewHolder>(DiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VIEW_TYPE_MAP else VIEW_TYPE_ELEMENT
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ItemViewHolder {
        val binding = when (viewType) {
            VIEW_TYPE_MAP -> {
                ItemMapBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false,
                )
            }

            VIEW_TYPE_ELEMENT -> {
                ItemAreaElementBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false,
                )
            }

            else -> throw Exception()
        }

        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(
            item = getItem(position),
            listener = listener,
        )
    }

    sealed class Item {

        data class Map(
            val boundingBox: BoundingBox,
            val boundingBoxPaddingPx: Int,
        ) : Item()

        data class Element(
            val id: String,
            val icon: Drawable,
            val name: String,
            val status: String,
            val statusColor: Int,
        ) : Item()
    }

    class ItemViewHolder(
        private val binding: ViewBinding,
    ) : RecyclerView.ViewHolder(
        binding.root,
    ) {

        fun bind(
            item: Item,
            listener: Listener,
        ) {
            if (item is Item.Map && binding is ItemMapBinding) {
                binding.apply {
                    map.post {
                        map.zoomToBoundingBox(
                            item.boundingBox,
                            false,
                            item.boundingBoxPaddingPx,
                        )
                    }

                    root.setOnClickListener { listener.onMapClick() }
                }
            }

            if (item is Item.Element && binding is ItemAreaElementBinding) {
                binding.apply {
                    icon.setImageDrawable(item.icon)
                    title.text = item.name
                    subtitle.text = item.status
                    subtitle.setTextColor(item.statusColor)
                    root.setOnClickListener { listener.onElementClick(item) }
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Item>() {

        override fun areItemsTheSame(
            oldItem: Item,
            newItem: Item,
        ): Boolean {
            return if (oldItem is Item.Element && newItem is Item.Element) {
                oldItem.id == newItem.id
            } else oldItem is Item.Map && newItem is Item.Map
        }

        override fun areContentsTheSame(
            oldItem: Item,
            newItem: Item,
        ): Boolean {
            if (oldItem is Item.Map && newItem is Item.Map) {
                return true
            }

            return newItem == oldItem
        }
    }

    interface Listener {

        fun onMapClick()

        fun onElementClick(item: Item.Element)
    }

    companion object {
        const val VIEW_TYPE_MAP = 0
        const val VIEW_TYPE_ELEMENT = 1
    }
}