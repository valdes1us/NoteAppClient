package com.valdesius.noteapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int noteId = intent.getIntExtra("note_id", -1);
        // Здесь вы можете отобразить уведомление о напоминании или выполнить другие действия
    }
}

