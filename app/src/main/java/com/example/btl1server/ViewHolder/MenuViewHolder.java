package com.example.btl1server.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.btl1server.Common.Common;
import com.example.btl1server.Interface.ItemClickListener;
import com.example.btl1server.R;

public class MenuViewHolder extends RecyclerView.ViewHolder implements
        View.OnClickListener,
        OnCreateContextMenuListener
{
    public TextView txtMenuName;
    public ImageView imageView;
    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public MenuViewHolder(View itemView){
        super(itemView);
        txtMenuName = (TextView)itemView.findViewById(R.id.menu_name);
        imageView = (ImageView)itemView.findViewById(R.id.menu_image);
        itemView.setOnClickListener(this);
        itemView.setOnCreateContextMenuListener(this);

    }
    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v,getAdapterPosition(),false);
    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        contextMenu.setHeaderTitle("Lựa chọn thao tác");
        contextMenu.add(0,0,getAdapterPosition(), Common.UPDATE);
        contextMenu.add(0,1,getAdapterPosition(),Common.DELETE);

    }
}
