package com.choistec.cms.scannerReg;

import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

public class CmsJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters job) {
        Log.d("BJY"," onStartJob");
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        Log.d("BJY"," onStopJob");
        return false;
    }
}
