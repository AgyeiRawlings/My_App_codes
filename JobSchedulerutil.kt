import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Build

fun scheduleRestartJob() {
    val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
    val jobInfo = JobInfo.Builder(1, ComponentName(this, RestartJobService::class.java))
        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
        .setPersisted(true)
        .setBackoffCriteria(1000, JobInfo.BACKOFF_POLICY_LINEAR)
        .build()

    jobScheduler.schedule(jobInfo)
}
