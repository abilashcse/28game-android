package com.agames.thuruppugulan;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.agames.thuruppugulan.base.BaseActivity;
import com.agames.thuruppugulan.ui.main.MainFragment;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow();
        }
    }
}
