package com.hfs.alipayall.adapter;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hfs.alipayall.bean.Item;
import com.hfs.alipayall.R;

import java.util.List;

/**
 * @author HuangFusheng
 * @date 2019-11-09
 * description TitleAdapter
 */
public class TitleAdapter extends RecyclerView.Adapter<TitleAdapter.ViewHolder> {

    private List<Item.SubItem> mSubItems;
    private boolean mEditMode = false;

    public TitleAdapter(List<Item.SubItem> subItems, OnItemClickListener onItemClickListener) {
        mSubItems = subItems;
        mOnItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.demo_subitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final Item.SubItem subItem = mSubItems.get(position);
        holder.mTvTitle.setText(subItem.getName());
        holder.mIvOpt.setImageResource(R.drawable.icon_delete_commonly);
        holder.mIvOpt.setVisibility(mEditMode ? View.VISIBLE : View.GONE);
        holder.itemView.setBackgroundColor(mEditMode ? Color.parseColor("#F5F5F5") : Color.WHITE);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(subItem, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mSubItems == null ? 0 : mSubItems.size();
    }

    public void setEditMode(boolean edit) {
        mEditMode = edit;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mTvTitle;
        private ImageView mIvOpt;

        public ViewHolder(View itemView) {
            super(itemView);

            mTvTitle = itemView.findViewById(R.id.demo_subitem_text);
            mIvOpt = itemView.findViewById(R.id.iv_app_opt);
        }
    }

    private OnItemClickListener mOnItemClickListener;


    public interface OnItemClickListener {
        void onItemClick(Item.SubItem subItem, int position);
    }
}
