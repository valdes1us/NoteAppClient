package com.valdesius.noteapp;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.valdesius.noteapp.models.Note;
import com.valdesius.noteapp.models.NoteDao;
import com.valdesius.noteapp.models.NoteDatabase;

import java.util.List;

public class NoteViewModel extends AndroidViewModel {
    private NoteDao noteDao;
    private LiveData<List<Note>> allNotes;

    public NoteViewModel(Application application) {
        super(application);
        NoteDatabase database = NoteDatabase.getDatabase(application);
        noteDao = database.noteDao();
        allNotes = noteDao.getAllNotes();
    }

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    public void insert(Note note) {
        new Thread(() -> noteDao.insert(note)).start();
    }

    public void update(Note note) {
        new Thread(() -> noteDao.update(note)).start();
    }

    public void delete(Note note) {
        new Thread(() -> noteDao.delete(note)).start();
    }
}
