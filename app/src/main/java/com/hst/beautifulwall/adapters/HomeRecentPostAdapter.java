package com.hst.beautifulwall.adapters;

import android.app.Activity;
import android.content.Context;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hst.beautifulwall.R;
import com.hst.beautifulwall.listeners.ListItemClickListener;
import com.hst.beautifulwall.models.content.Posts;

import java.util.ArrayList;

public class HomeRecentPostAdapter extends RecyclerView.Adapter<HomeRecentPostAdapter.ViewHolder> {

    private Context mContext;
    private Activity mActivity;

    private ArrayList<Posts> mContentList;
    private ListItemClickListener mItemClickListener;

    public HomeRecentPostAdapter(Context mContext, Activity mActivity, ArrayList<Posts> mContentList) {
        this.mContext = mContext;
        this.mActivity = mActivity;
        this.mContentList = mContentList;
    }

    public void setItemClickListener(ListItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_recycler, parent, false);
        return new ViewHolder(view, viewType, mItemClickListener);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private CardView cardView;
        private ImageView imgPost;
        private TextView tvTitle;
        private ImageButton btnFav;
        private ListItemClickListener itemClickListener;


        public ViewHolder(View itemView, int viewType, ListItemClickListener itemClickListener) {
            super(itemView);

            this.itemClickListener = itemClickListener;
            // Find all views ids
            cardView = (CardView) itemView.findViewById(R.id.card_view_top);
            imgPost = (ImageView) itemView.findViewById(R.id.post_img);
            tvTitle = (TextView) itemView.findViewById(R.id.title_text);
            btnFav = (ImageButton) itemView.findViewById(R.id.btn_fav);

            btnFav.setOnClickListener(this);
            cardView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(getLayoutPosition(), view);
            }
        }
    }

    @Override
    public int getItemCount() {
        return (null != mContentList ? mContentList.size() : 0);

    }

    @Override
    public void onBindViewHolder(HomeRecentPostAdapter.ViewHolder mainHolder, int position) {
        final Posts model = mContentList.get(position);

        // setting data over views
        String imgUrl = model.getImageUrl();
        if (imgUrl != null && !imgUrl.isEmpty()) {
            Glide.with(mContext)
                    .load(imgUrl)
                    .into(mainHolder.imgPost);
        }

        if (model.isFavorite()) {
            mainHolder.btnFav.setImageResource(R.drawable.ic_fav);
        } else {
            mainHolder.btnFav.setImageResource(R.drawable.ic_un_fav);
        }

        mainHolder.tvTitle.setText(Html.fromHtml(model.getTitle()));
    }
}