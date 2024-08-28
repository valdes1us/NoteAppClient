package com.valdesius.noteapp.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.valdesius.noteapp.models.Note;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LocalStorageHelper {
    private static final String PREFS_NAME = "note_app_prefs";
    private static final String NOTES_KEY = "notes_key";
    private SharedPreferences sharedPreferences;
    private Gson gson;

    public LocalStorageHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public List<Note> getNotes() {
        String json = sharedPreferences.getString(NOTES_KEY, null);
        Type type = new TypeToken<ArrayList<Note>>(){}.getType();
        return json == null ? new ArrayList<>() : gson.fromJson(json, type);
    }

    public void saveNotes(List<Note> notes) {
        String json = gson.toJson(notes);
        sharedPreferences.edit().putString(NOTES_KEY, json).apply();
    }

    public void deleteNote(Note note) {
        List<Note> notes = getNotes();
        notes.remove(note);
        saveNotes(notes);
    }

    public void updateNote(Note updatedNote) {
        List<Note> notes = getNotes();
        for (int i = 0; i < notes.size(); i++) {
            Note note = notes.get(i);
            if (note.getNote_id() == updatedNote.getNote_id()) {
                notes.set(i, updatedNote);
                break;
            }
        }
        saveNotes(notes);
    }

    public void deleteNote(int noteId) {
        List<Note> notes = getNotes();
        for (int i = 0; i < notes.size(); i++) {
            if (notes.get(i).getNote_id() == noteId) {
                notes.remove(i);
                break;
            }
        }
        saveNotes(notes);
    }

}
