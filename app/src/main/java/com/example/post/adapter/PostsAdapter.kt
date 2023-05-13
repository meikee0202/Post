package com.example.post.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.post.R
import com.example.post.entities.Posts
import kotlinx.android.synthetic.main.item_rv_posts.view.*
import kotlin.collections.ArrayList

class PostsAdapter() :
//extends the RecyclerView.Adapter class and uses the PostsViewHolder as its view holder.
    RecyclerView.Adapter<PostsAdapter.PostsViewHolder>() {


    var listener: OnItemClickListener? = null
    var arrList = ArrayList<Posts>()

    //when the adapter needs to create a new ViewHolder
    //It inflates the layout for the item views and returns a new PostsViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsViewHolder {
        return PostsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_rv_posts, parent, false)
        )
    }

    override fun getItemCount(): Int {
        //returns the number of items in the RecyclerView.
        return arrList.size
    }

    fun setData(arrPostsList: List<Posts>) {
        //sets the data for the adapter
        arrList = arrPostsList as ArrayList<Posts>
    }

    fun setOnClickListener(listener1: OnItemClickListener) {
        listener = listener1
    }

    //holder, the PostViewHolder that should be updated
    //position, the position of the item within the adapter's data set.
    override fun onBindViewHolder(holder: PostsViewHolder, position: Int) {

        //set the position to each holder view elements
        holder.itemView.tvTitle.text = arrList[position].title
        holder.itemView.tvDesc.text = arrList[position].postText
        holder.itemView.tvDateTime.text = arrList[position].dateTime

        //for display the image from gallery
        if (arrList[position].imgPath!= null) {
            // BitmapFactory.decodeFile(), takes in the path of an image file and returns a Bitmap object representing the image.
            holder.itemView.imgPost.setImageBitmap(BitmapFactory.decodeFile(arrList[position].imgPath))
            holder.itemView.imgPost.visibility = View.VISIBLE
        } else {
            holder.itemView.imgPost.visibility = View.GONE
        }

        //onClickListener is added to the cardView element of the holder
        holder.itemView.cardView.setOnClickListener {

            //triggers the onClicked function of the listener object (if it is not null) with the id field of the Posts object at the given position
            listener!!.onClicked(arrList[position].id!!)
        }

    }

    // ViewHolder class for the adapter.
    class PostsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    }

    //interface for the click listener used in the adapter
    interface OnItemClickListener {
        fun onClicked(postId: Int)
    }

}