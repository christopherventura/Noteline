package com.chrisventura.apps.noteline.UI;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.chrisventura.apps.noteline.Data.ContractNotes;
import com.chrisventura.apps.noteline.R;

/**
 * Created by ventu on 18/5/2017.
 */

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    Context context;
    Cursor c;
    OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        String getCurrentCategoryName();
        void onCategoryClick(int position);
        boolean onCategoryLongClick(int position);
        boolean onContextItemClick(MenuItem item, String categoryId, String categoryName);
    }

    public CategoryAdapter(Context context, Cursor c, OnCategoryClickListener listener) {
        this.context = context;
        this.c = c;
        this.listener = listener;
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.category_item, parent, false);

        return new CategoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {
        c.moveToPosition(position);
        String name = c.getString(c.getColumnIndex(
                ContractNotes.CategoriesEntries.NAME
        ));

        holder.mName.setText(name);
        String current = listener.getCurrentCategoryName();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (current != null && name.equals(current)) {
                holder.mName.setTextColor(context.getResources()
                        .getColor(R.color.colorAccent,
                                context.getTheme()));
            } else {
                holder.mName.setTextColor(context.getResources()
                        .getColor(R.color.colorPrimaryText,
                                context.getTheme()));
            }
        }
    }

    @Override
    public int getItemCount() {
        if (c != null) {
            return c.getCount();
        }
        return 0;
    }

    public Cursor getCursor() {
        return c;
    }

    public void swapCursor(Cursor cursor) {
        if (cursor != null) {
            c = cursor;
            notifyDataSetChanged();
        }
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener,
            View.OnLongClickListener, View.OnCreateContextMenuListener {
        TextView mName;

        public CategoryViewHolder(View itemView) {
            super(itemView);
            mName = (TextView) itemView.findViewById(R.id.category_name);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onCategoryClick(getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            return listener.onCategoryLongClick(getAdapterPosition());
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            MenuItem edit = menu.add(v.getId(),R.id.action_edit_category, 0, R.string.edit);
            MenuItem delete = menu.add(v.getId(), R.id.action_delete_category, 1, R.string.delete);
            MenuItem.OnMenuItemClickListener onMenuItemClickListener = new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    c.moveToPosition(getAdapterPosition());

                    String id = c.getString(c.getColumnIndex(
                            ContractNotes.CategoriesEntries.ID
                    ));

                    String name = c.getString(c.getColumnIndex(
                            ContractNotes.CategoriesEntries.NAME
                    ));

                    if (name.equals("Uncategorized")) {
                        Toast.makeText(context, R.string.this_category_is_not_editable,
                                Toast.LENGTH_LONG).show();
                        return false;
                    }
                    return listener.onContextItemClick(item, id, name);
                }
            };

            edit.setOnMenuItemClickListener(onMenuItemClickListener);
            delete.setOnMenuItemClickListener(onMenuItemClickListener);
        }
    }
}
