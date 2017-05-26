package com.chrisventura.apps.noteline.UI;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;
import com.chrisventura.apps.noteline.Data.ContractNotes;
import com.chrisventura.apps.noteline.R;
import com.chrisventura.apps.noteline.Utils.StringUtils;

/**
 * Created by ventu on 16/5/2017.
 */

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesViewHolder> {

    Context context;
    Cursor c;
    OnNoteClickListener listener;
    MultiSelector multiSelector;

    private static int TITLE_MAX_LENGHT = 37;

    public interface OnNoteClickListener {
        void onNoteClick(String idNote);
        void onLongClick();
        void onSelectedNoteItem();
        void onEmptyCursor();
        void onPoblatedCursor();
    }

    public NotesAdapter(Context c, OnNoteClickListener l) {
        context = c;
        listener = l;
        multiSelector = new MultiSelector();
    }

    @Override
    public NotesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.note_item_list, parent, false);
        return new NotesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(NotesViewHolder holder, final int position) {
        c.moveToPosition(position);

        String title = c.getString(c.getColumnIndex(ContractNotes.NotesEntries.TITLE));
        String body = c.getString(c.getColumnIndex(ContractNotes.NotesEntries.BODY));
        String category = c.getString(c.getColumnIndex(ContractNotes.NotesEntries.CATEGORY_NAME));
        int checked = c.getInt(c.getColumnIndex(ContractNotes.NotesEntries.CHECKED));

        holder.mTitle.setText(getShort(title));
        holder.mBody.setText(StringUtils.removeHtmlTags(body));
        if (!category.equals(
                context.getResources()
                        .getString(R.string.uncategorized))) {
            holder.mCategory.setText(category);
        } else {
            holder.mCategory.setText("");
        }

        if (checked > 0) {
            holder.mChecked.setVisibility(View.VISIBLE);
        } else {
            holder.mChecked.setVisibility(View.GONE);
        }

    }

    public Cursor getCursor() {
        return c;
    }

    @Override
    public int getItemCount() {
        if (c != null) {
            if(c.getCount() == 0) {
                listener.onEmptyCursor();
            } else {
                listener.onPoblatedCursor();
            }
            return c.getCount();
        }
        listener.onEmptyCursor();
        return 0;
    }

    public void swapCursor(Cursor nc) {
        if (nc != null) {
            c = nc;
            getItemCount();
            notifyDataSetChanged();
        }
    }

    private String getShort(String string) {
        if (!TextUtils.isEmpty(string) && string.length() > TITLE_MAX_LENGHT) {
            return string.substring(0, TITLE_MAX_LENGHT).concat("...");
        }
        return string;
    }

    protected class NotesViewHolder extends SwappingHolder
            implements View.OnLongClickListener, View.OnClickListener {
        TextView mTitle;
        TextView mBody;
        TextView mCategory;
        ImageView mChecked;
        View mView;
        public NotesViewHolder(View itemView) {
            super(itemView, multiSelector);
            mTitle = (TextView) itemView.findViewById(R.id.item_noteTitleText);
            mBody = (TextView) itemView.findViewById(R.id.item_noteBodyText);
            mCategory = (TextView) itemView.findViewById(R.id.item_noteCategoryText);
            mChecked = (ImageView) itemView.findViewById(R.id.img_checked);
            mView = itemView;

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setSelectionModeBackgroundDrawable(context.getDrawable(R.drawable.noteitembackground));
            }
            setSelectionModeStateListAnimator(null);
        }

        @Override
        public boolean onLongClick(View v) {
            if (!multiSelector.isSelectable()) {
                listener.onLongClick();
                multiSelector.setSelectable(true);
            }
            multiSelector.setSelected(NotesViewHolder.this, true);
            listener.onSelectedNoteItem();
            return true;
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (!multiSelector.tapSelection(NotesViewHolder.this)) {
                c.moveToPosition(position);
                listener.onNoteClick(c.getString(c.getColumnIndex(ContractNotes.NotesEntries.ID)));
            } else {
                listener.onSelectedNoteItem();
            }
        }
    }
}
