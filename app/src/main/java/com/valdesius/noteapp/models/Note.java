package com.valdesius.noteapp.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes")
public class Note {
    @PrimaryKey(autoGenerate = true)
    private int note_id;
    private String title;
    private String content;
    private String backgroundColor;
    private String fontColor;
    private float fontSize;
    private String fontStyle; // Новое поле для стиля шрифта
    private String drawingPath;

    // Конструкторы, геттеры и сеттеры

    public Note(int note_id, String title, String content, String backgroundColor, String fontColor, float fontSize, String fontStyle) {
        this.note_id = note_id;
        this.title = title;
        this.content = content;
        this.backgroundColor = backgroundColor;
        this.fontColor = fontColor;
        this.fontSize = fontSize;
        this.fontStyle = fontStyle;
    }

    public Note(String title, String content, String backgroundColor, String fontColor, float fontSize, String fontStyle) {
        this.title = title;
        this.content = content;
        this.backgroundColor = backgroundColor;
        this.fontColor = fontColor;
        this.fontSize = fontSize;
        this.fontStyle = fontStyle;
    }

    public Note() {
    }
    // Добавьте другие конструкторы, если необходимо

    public int getNote_id() {
        return note_id;
    }

    public void setNote_id(int note_id) {
        this.note_id = note_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    public float getFontSize() {
        return fontSize;
    }

    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
    }

    public String getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(String fontStyle) {
        this.fontStyle = fontStyle;
    }

    public String getDrawingPath() {
        return drawingPath;
    }

    public void setDrawingPath(String drawingPath) {
        this.drawingPath = drawingPath;
    }
}
