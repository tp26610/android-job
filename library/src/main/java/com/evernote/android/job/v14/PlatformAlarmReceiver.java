/*
 * Copyright 2007-present Evernote Corporation.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.evernote.android.job.v14;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.evernote.android.job.util.JobCat;
import com.evernote.android.job.util.JobUtil;

import net.vrallev.android.cat.CatLog;

/**
 * @author rwondratschek
 */
public class PlatformAlarmReceiver extends WakefulBroadcastReceiver {

    /*package*/ static final String EXTRA_JOB_ID = "EXTRA_JOB_ID";

    private static final String ACTION_RUN_JOB = "com.evernote.android.job.v14.RUN_JOB";
    private static final String ACTION_RUN_JOB_OLD = "net.vrallev.android.job.v14.RUN_JOB";

    private static final CatLog CAT = new JobCat("PlatformAlarmReceiver");

    /*package*/ static Intent createIntent(int jobId) {
        return new Intent(ACTION_RUN_JOB).putExtra(EXTRA_JOB_ID, jobId);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !(ACTION_RUN_JOB.equals(intent.getAction()) || ACTION_RUN_JOB_OLD.equals(intent.getAction()))) {
            return;
        }

        Intent serviceIntent = PlatformAlarmService.createIntent(context, intent.getIntExtra(EXTRA_JOB_ID, -1));

        if (JobUtil.hasWakeLockPermission(context)) {
            try {
                startWakefulService(context, serviceIntent);
            } catch (Exception e) {
                /*
                 * Saw a SecurityException, although WAKE_LOCK permission is granted.
                 * https://gist.github.com/vRallev/715777806e0abe3777bc
                 *
                 * The wake lock is acquired after the service was started, so it's not necessary
                 * to start the service another time.
                 */
                CAT.e(e);
            }

        } else {
            context.startService(serviceIntent);
        }
    }
}
