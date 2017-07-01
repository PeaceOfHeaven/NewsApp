package evich.newsapp;

import android.app.Application;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.squareup.leakcanary.LeakCanary;

import java.util.concurrent.TimeUnit;

import evich.newsapp.dagger.component.ApplicationComponent;
import evich.newsapp.dagger.component.DaggerApplicationComponent;
import evich.newsapp.dagger.module.ApplicationModule;
import evich.newsapp.services.FirebaseJobService;

/**
 * Created by Nhat on 6/8/2017.
 */

public class NewsApplication extends Application {

    private ApplicationComponent mApplicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }

        mApplicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
        kickoffService();
    }

    public ApplicationComponent getApplicationComponent() {
        return mApplicationComponent;
    }

    private void kickoffService() {
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Job.Builder builder = dispatcher.newJobBuilder()
                .setService(FirebaseJobService.class) // the JobService that will be called
                .setTag(FirebaseJobService.UPDATE_NEWS);  // uniquely identifies the job

        final int periodicity = (int) TimeUnit.MINUTES.toSeconds(20); // Every 20 minutes periodicity expressed as seconds
        final int toleranceInterval = (int) TimeUnit.MINUTES.toSeconds(5); // a small(ish) window of time when triggering is OK

        builder.setTrigger(Trigger.executionWindow(periodicity, periodicity + toleranceInterval));
        builder.addConstraint(Constraint.ON_ANY_NETWORK);
        builder.setLifetime(Lifetime.FOREVER);
        builder.setRecurring(true);
        builder.setRetryStrategy(RetryStrategy.DEFAULT_LINEAR);
        dispatcher.mustSchedule(builder.build());
    }
}
