package com.cookbook.fenix.cookbook;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;


/**
 * Created by fenix on 01.07.2015.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeHolder> {

    public static final String TEST = "RecipeAdapter : ";
    public static LinkedList<Recipe> linkedList = null; //in sample private String[] mDataset;
    private SharedPreferences mSharedPreferences;
    private Context mContext;

    //TODO: Change constructor
    public RecipeAdapter(LinkedList<Recipe> data, Context context) {
        super();
        linkedList = data;
        this.mContext = context;
    }

    public RecipeAdapter(ArrayList<Recipe> data, Context context) {
        super();
        linkedList = new LinkedList<Recipe>(data);
        this.mContext = context;
    }

    public RecipeAdapter(Context context) {
        super();
        if (linkedList == null) {
            linkedList = new LinkedList<Recipe>();
        }
        this.mContext = context;
    }

    private static OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class RecipeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView imgIcon;
        public TextView txtTitle;
        public TextView txtPublisher;
        public TextView txtRank;


        public RecipeHolder(final View itemView) {
            super(itemView);
            imgIcon = (ImageView) itemView.findViewById(R.id.imageView);
            txtTitle = (TextView) itemView.findViewById(R.id.textView);
            txtPublisher = (TextView) itemView.findViewById(R.id.textView2);
            txtRank = (TextView) itemView.findViewById(R.id.textView3);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(v, getPosition());
            }
        }
    }

    @Override
    public RecipeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        //TODO: set layout parameters
        RecipeHolder rh = new RecipeHolder(v);
        return rh;
    }

    @Override
    public void onBindViewHolder(RecipeHolder holder, int position) {
        Recipe recipe = linkedList.get(position);
        holder.txtTitle.setText(recipe.getTitle());
        holder.txtPublisher.setText(recipe.getPublisher());

        Downloader.setBitmapFromCache(holder.imgIcon, recipe, position, null);
        holder.txtRank.setText("Rating = " + recipe.getSocialRank());
    }

    @Override
    public int getItemCount() {
        return linkedList.size();
    }

    public ArrayList<Recipe> getData() {
        return new ArrayList<Recipe>(linkedList);
    }

    public void alterItem(Integer pointer) {
        // suppression exception when arrayAdapter was cleared and task still running
        try {
            if (pointer != null) this.notifyItemChanged(pointer);
            else this.notifyDataSetChanged();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }

    }
}

class PreCachingLayoutManager extends GridLayoutManager {
    private int mLayoutSpace = 1000;
    private int extraLayoutSpace = -1;
    private Context context;

    public PreCachingLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
        this.context = context;
    }

    public PreCachingLayoutManager(Context context, int spanCount, int space) {
        super(context, spanCount);
        this.context = context;
        mLayoutSpace = space;
    }


    public void setExtraLayoutSpace(int extraLayoutSpace) {
        this.extraLayoutSpace = extraLayoutSpace;
    }

    @Override
    protected int getExtraLayoutSpace(RecyclerView.State state) {
        if (extraLayoutSpace > 0) {
            return extraLayoutSpace;
        }
        return mLayoutSpace;
    }
}

