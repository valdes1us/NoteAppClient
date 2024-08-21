package com.valdesius.noteapp.helpers;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.valdesius.noteapp.R;
import com.valdesius.noteapp.models.Note;

import java.util.List;

public class NoteListRecyclerViewHelper extends RecyclerView.Adapter<NoteListRecyclerViewHelper.NoteListViewHolder> {
    private List<Note> noteListItems;
    private Context context;

    public NoteListRecyclerViewHelper(List<Note> noteListItems, Context context) {
        this.noteListItems = noteListItems;
        this.context = context;
    }

    @NonNull
    @Override
    public NoteListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_list, parent, false);
        return new NoteListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteListViewHolder holder, int position) {
        Note note = this.noteListItems.get(position);

        holder.noteTitle.setText(note.getTitle());
        holder.noteBody.setText(note.getBody());

    }

    @Override
    public int getItemCount() {
        return noteListItems.size();
    }

    public class NoteListViewHolder extends RecyclerView.ViewHolder {
        public TextView noteTitle, noteBody;
        private LinearLayout noteItemLayout;

        public NoteListViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTitle = itemView.findViewById(R.id.note_title);
            noteBody = itemView.findViewById(R.id.note_body);
        }
    }
}
