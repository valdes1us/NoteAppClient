package com.valdesius.noteapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.valdesius.noteapp.models.Note;
import com.valdesius.noteapp.models.NoteDao;
import com.valdesius.noteapp.models.NoteDatabase;

import java.util.List;

public class NoteViewModel extends ViewModel {

    private NoteDao noteDao;
    private LiveData<List<Note>> allNotes;

    public NoteViewModel() {
        noteDao = NoteDatabase.getDatabase(MyApplication.getAppContext()).noteDao();
        allNotes = noteDao.getAllNotes();
    }

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }
}
