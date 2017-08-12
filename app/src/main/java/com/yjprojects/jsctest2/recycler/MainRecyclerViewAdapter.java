package com.yjprojects.jsctest2.recycler;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.yjprojects.jsctest2.MainActivity;
import com.yjprojects.jsctest2.R;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by jyj on 2017-08-05.
 */

public class MainRecyclerViewAdapter extends RecyclerView.Adapter<MainRecyclerViewHolder> implements ViewHolderClickListener{
    private Context context;
    public List<BaseListClass> list;


    public MainRecyclerViewAdapter(List<BaseListClass> list){
        this.list = list;
    }


    @Override
    public MainRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View layoutView = LayoutInflater.from(context).inflate(R.layout.recyclerview_main_item, null);
        MainRecyclerViewHolder holder = new MainRecyclerViewHolder(layoutView, this);
        return holder;
    }

    @Override
    public void onViewClicked(View view, int position) {
        ((MainActivity) context).onViewClicked(list.get(position).getId(), list.get(position));
    }

    @Override
    public void onPopupMenuClicked(MenuItem menuItem, int position) {
        switch(menuItem.getItemId()){
            case R.id.option_more_info:
                Log.d("TAGS", "clickedmenu");
                break;
        }
    }

    @Override
    public void onBindViewHolder(MainRecyclerViewHolder holder, int position) {
        BaseListClass data = list.get(position);
        holder.title.setText(data.getTitle());
        Date date = data.getDate();
        DateFormat format1 = DateFormat.getDateInstance(DateFormat.FULL);

        holder.subtitle.setText(format1.format(date));
        if(data.getId() != null) {
            Picasso.with(context).load(Uri.fromFile(new File(data.getId())))
                    .error(R.drawable.ic_crop_original_black_48dp)
                    .placeholder(R.drawable.ic_crop_original_black_48dp)
                    .centerCrop()
                    .resize(160, 160)
                    .into(holder.image);

        }
        else holder.image.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}


class MainRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, PopupMenu.OnMenuItemClickListener{

    public ImageView image;
    public TextView title;
    public TextView subtitle;
    public ImageButton optionButton;

    private ViewHolderClickListener listener;

    public MainRecyclerViewHolder(View itemView, ViewHolderClickListener listener) {
        super(itemView);

        image = (ImageView) itemView.findViewById(R.id.item_image);
        title = (TextView) itemView.findViewById(R.id.item_title);
        subtitle = (TextView) itemView.findViewById(R.id.item_subtitle);
        optionButton = (ImageButton) itemView.findViewById(R.id.item_optionButton);

        itemView.setOnClickListener(this);
        optionButton.setOnClickListener(this);

        this.listener = listener;

    }

    @Override
    public void onClick(View view) {
        //when option buttton clicked -> open popup menu, call onMenuItemClick
        if(view.getId() == R.id.item_optionButton) showPopupMenu(view.getContext(), view);
        else listener.onViewClicked(view, getAdapterPosition());
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        listener.onPopupMenuClicked(item, getAdapterPosition());
        return true;
    }

    public void showPopupMenu(Context context, View view){
        PopupMenu popup = new PopupMenu(context, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.option_button_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }
}

