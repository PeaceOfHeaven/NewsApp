package evich.newsapp.news;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import evich.newsapp.R;

public class DummyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dummy);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 300);
    }
}
