package com.hst.beautifulwall.adapters;

import android.content.Context;
import androidx.viewpager.widget.PagerAdapter;
import androidx.cardview.widget.CardView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hst.beautifulwall.R;
import com.hst.beautifulwall.listeners.ListItemClickListener;
import com.hst.beautifulwall.models.content.Posts;

import java.util.ArrayList;

public class PostsPagerAdapter extends PagerAdapter {

    private Context mContext;

    private ArrayList<Posts> mItemList;
    private ListItemClickListener mItemClickListener;

    private LayoutInflater inflater;

    private boolean isFromDetail = false;

    public PostsPagerAdapter(Context mContext, ArrayList<Posts> mItemList) {
        this.mContext = mContext;
        this.mItemList = mItemList;
        inflater = LayoutInflater.from(mContext);
    }

    public PostsPagerAdapter(Context mContext, ArrayList<Posts> mItemList, boolean isFormDetail) {
        this.mContext = mContext;
        this.mItemList = mItemList;
        this.isFromDetail =isFormDetail;
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return mItemList.size();
    }


    @Override
    public Object instantiateItem(final ViewGroup view, final int position) {

        View rootView = inflater.inflate(R.layout.item_featured_pager_list, view, false);

        final TextView titleTextView = (TextView) rootView.findViewById(R.id.title_text);
        final ImageView imgPost = (ImageView) rootView.findViewById(R.id.post_img);
        final CardView cardView = (CardView) rootView.findViewById(R.id.card_view_top);
        final RelativeLayout pointLayout = rootView.findViewById(R.id.point_layout);

        final Posts model = mItemList.get(position);

        // setting data over views
        String imgUrl = model.getImageUrl();
        if (imgUrl != null && !imgUrl.isEmpty()) {
            Glide.with(mContext)
                    .load(imgUrl)
                    .into(imgPost);
        }

        if (model.getIsNeedPoint() && !isFromDetail) {
            pointLayout.setVisibility(View.VISIBLE);
        }
        else {
            pointLayout.setVisibility(View.GONE);
        }

        titleTextView.setText(Html.fromHtml(model.getTitle()));

        view.addView(rootView);

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(position, view);
                }
            }
        });

        return rootView;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    public void setItemClickListener(ListItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

}
