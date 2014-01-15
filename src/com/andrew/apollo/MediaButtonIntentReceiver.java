/*
 * Copyright (C) 2007 The Android Open Source Project Licensed under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.andrew.apollo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

import com.andrew.apollo.ui.activities.HomeActivity;

/**
 * Used to control headset playback.
 *   Single press: pause/resume
 *   Double press: next track
 *   Triple press: previous track
 *   Long press: voice search
 */
public class MediaButtonIntentReceiver extends BroadcastReceiver {
    private static final boolean DEBUG = false;
    private static final String TAG = "MediaButtonIntentReceiver";

    private static final int MSG_LONGPRESS_TIMEOUT = 1;
    private static final int MSG_HEADSET_CLICK = 2;

    private static final int LONG_PRESS_DELAY = 1000;

    private static final int DOUBLE_CLICK = 800;

    private static int mClickCounter = 0;
    private static long mLastClickTime = 0;

    private static boolean mDown = false;

    private static boolean mLaunched = false;

    private static Runnable mSingleClickTask;
    private static Runnable mDoubleClickTask;

    private static Handler mHandler = new Handler() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case MSG_LONGPRESS_TIMEOUT:
                    if (!mLaunched) {
                        final Context context = (Context)msg.obj;
                        final Intent i = new Intent();
                        i.setClass(context, HomeActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(i);
                        mLaunched = true;
                    }
                    break;

                case MSG_HEADSET_CLICK:
                    final Bundle data = msg.getData();
                    final long eventtime = data.getLong("eventtime");
                    final String command = data.getString("command");
                    final Context context = (Context)msg.obj;

                    if (eventtime - mLastClickTime < DOUBLE_CLICK || mClickCounter == 0) {
                        mClickCounter++;
                        if (mClickCounter == 1) {
                            mSingleClickTask = new Runnable() {
                                public void run() {
                                    final Intent i = new Intent(context,
                                            MusicPlaybackService.class);
                                    i.setAction(MusicPlaybackService.SERVICECMD);
                                    i.putExtra(MusicPlaybackService.CMDNAME,
                                            MusicPlaybackService.CMDTOGGLEPAUSE);
                                    context.startService(i);
                                    Log.d(TAG, "Single click fired. Toggling pause...");
                                    mClickCounter = 0;
                                }
                            };

                            if (DEBUG) {
                                Log.v(TAG, "Single click scheduled");
                            }

                            postDelayed(mSingleClickTask, DOUBLE_CLICK);
                        } else if (mClickCounter == 2) {
                            removeCallbacks(mSingleClickTask);
                            mSingleClickTask = null;
                            if (DEBUG) {
                                Log.v(TAG, "Single click canceled");
                                Log.v(TAG, "Double click scheduled");
                            }

                            mDoubleClickTask = new Runnable() {
                                public void run() {
                                    final Intent i = new Intent(context,
                                            MusicPlaybackService.class);
                                    i.setAction(MusicPlaybackService.SERVICECMD);
                                    i.putExtra(MusicPlaybackService.CMDNAME,
                                            MusicPlaybackService.CMDNEXT);
                                    context.startService(i);
                                    Log.d(TAG, "Double click fired. Skipping to next song...");
                                    mClickCounter = 0;
                                }
                            };
                            postDelayed(mDoubleClickTask, DOUBLE_CLICK);
                        } else if (mClickCounter == 3) {
                            removeCallbacks(mDoubleClickTask);
                            mDoubleClickTask = null;

                            if (DEBUG) {
                                Log.v(TAG, "Double click canceled");
                            }

                            final Intent i = new Intent(context, MusicPlaybackService.class);
                            i.setAction(MusicPlaybackService.SERVICECMD);
                            i.putExtra(MusicPlaybackService.CMDNAME,
                                    MusicPlaybackService.CMDPREVIOUS);
                            context.startService(i);
                            Log.d(TAG, "Triple click fired. Going to previous song...");
                            mClickCounter = 0;
                        }
                    } else {
                        Log.e(TAG, "This should really never happen as runnables should set click counter to 0 by now.");
                        mClickCounter = 0;
                    }
                    mLastClickTime = eventtime;
                    break;
            }
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String intentAction = intent.getAction();
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intentAction)) {
            final Intent i = new Intent(context, MusicPlaybackService.class);
            i.setAction(MusicPlaybackService.SERVICECMD);
            i.putExtra(MusicPlaybackService.CMDNAME, MusicPlaybackService.CMDPAUSE);
            context.startService(i);
        } else if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
            final KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event == null) {
                return;
            }

            final int keycode = event.getKeyCode();
            final int action = event.getAction();
            final long eventtime = event.getEventTime();

            String command = null;
            switch (keycode) {
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    command = MusicPlaybackService.CMDSTOP;
                    break;
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    command = MusicPlaybackService.CMDTOGGLEPAUSE;
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    command = MusicPlaybackService.CMDNEXT;
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    command = MusicPlaybackService.CMDPREVIOUS;
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    command = MusicPlaybackService.CMDPAUSE;
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    command = MusicPlaybackService.CMDPLAY;
                    break;
            }
            if (command != null) {
                if (action == KeyEvent.ACTION_DOWN) {
                    if (mDown) {
                        if ((MusicPlaybackService.CMDTOGGLEPAUSE.equals(command) || MusicPlaybackService.CMDPLAY
                                .equals(command))
                                && mLastClickTime != 0
                                && eventtime - mLastClickTime > LONG_PRESS_DELAY) {
                            mHandler.sendMessage(mHandler.obtainMessage(MSG_LONGPRESS_TIMEOUT,
                                    context));
                        }
                    } else if (event.getRepeatCount() == 0) {
                        // Only consider the first event in a sequence, not the
                        // repeat events,
                        // so that we don't trigger in cases where the first
                        // event went to
                        // a different app (e.g. when the user ends a phone call
                        // by
                        // long pressing the headset button)

                        // The service may or may not be running, but we need to
                        // send it
                        // a command.
                        final Intent i = new Intent(context, MusicPlaybackService.class);
                        i.setAction(MusicPlaybackService.SERVICECMD);
                        if (keycode == KeyEvent.KEYCODE_HEADSETHOOK) {
                            Message msg = mHandler.obtainMessage(MSG_HEADSET_CLICK, context);
                            Bundle data = new Bundle();
                            data.putLong("eventtime", eventtime);
                            data.putString("command", command);
                            msg.setData(data);
                            mHandler.sendMessage(msg);
                        } else {
                            i.putExtra(MusicPlaybackService.CMDNAME, command);
                            context.startService(i);
                        }
                        mLaunched = false;
                        mDown = true;
                    }
                } else {
                    mHandler.removeMessages(MSG_LONGPRESS_TIMEOUT);
                    mDown = false;
                }
                if (isOrderedBroadcast()) {
                    abortBroadcast();
                }
            }
        }
    }
}
