package scratchcard.cbt.com.scratchcard;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.Toast;

import scratchcard.cbt.com.scratchcard.view.Scratchcard;
import scratchcard.cbt.com.scratchcard.view.Scratchcard.OnScratchCardCompleteListener;

public class MainActivity extends AppCompatActivity {
    private Scratchcard mScratchcard;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mScratchcard = (Scratchcard) findViewById(R.id.id_scratchcard);
        mScratchcard.setOnScratchCardCompleteListener(new OnScratchCardCompleteListener() {
            @Override
            public void complete() {
                Toast.makeText(getApplicationContext(),"用户已经刮得差不多了",Toast.LENGTH_SHORT).show();
            }
        });
    }

}
