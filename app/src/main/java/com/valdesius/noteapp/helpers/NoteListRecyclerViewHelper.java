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

import com.valdesius.noteapp.MainActivity;
import com.valdesius.noteapp.NoteDetailsActivity;
import com.valdesius.noteapp.R;
import com.valdesius.noteapp.models.Note;

import java.util.List;

public class NoteListRecyclerViewHelper extends RecyclerView.Adapter<NoteListRecyclerViewHelper.NoteViewHolder> {
    private List<Note> noteList;
    private Context context;


    public NoteListRecyclerViewHelper(List<Note> noteList, Context context) {
        this.noteList = noteList;
        this.context = context;
    }
    public void updateNoteList(List<Note> newNoteList) {
        this.noteList = newNoteList;
        notifyDataSetChanged(); // Обновляем отображение RecyclerView
    }

    @Override
    public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_list, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NoteViewHolder holder, int position) {
        Note note = noteList.get(position);
        holder.titleTextView.setText(note.getTitle());
        holder.contentTextView.setText(note.getContent());

        // Обработка клика на элементе списка
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, NoteDetailsActivity.class);
                intent.putExtra("note_id", note.getNote_id());
                intent.putExtra("title", note.getTitle());
                intent.putExtra("content", note.getContent());

                ((MainActivity) context).startActivityForResult(intent, MainActivity.EDIT_NOTE_REQUEST); // Код для редактирования заметки
            }
        });
    }


    @Override
    public int getItemCount() {
        return noteList.size();
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView contentTextView;

        public NoteViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.note_title);
            contentTextView = itemView.findViewById(R.id.note_body);
        }
    }
}
