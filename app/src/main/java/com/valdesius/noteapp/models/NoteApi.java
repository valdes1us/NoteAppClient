package com.valdesius.noteapp.models;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface NoteApi {
    @GET("/api/notes")
    Call<List<Note>> getAllNotes();
}