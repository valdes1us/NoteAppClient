package com.valdesius.noteapp.models;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface NoteApi {
    @POST("/api/notes")
    Call<Note> createNote(@Body Note note);

    @DELETE("/api/notes/{id}")
    Call<Void> deleteNote(@Path("id") int id);

    @PUT("/api/notes/{id}")
    Call<Note> updateNote(@Path("id") int id, @Body Note note);

    @GET("/api/notes")
    Call<List<Note>> getAllNotes();
}
