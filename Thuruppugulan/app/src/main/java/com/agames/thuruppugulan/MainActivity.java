package com.agames.thuruppugulan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.agames.thuruppugulan.base.BaseActivity;
import com.agames.thuruppugulan.ui.main.MainFragment;
import com.agames.thuruppugulan.ui.main.game.Player;
import com.agames.thuruppugulan.ui.main.game.TableFragment;

import net.fitken.rose.Rose;

//http://achex.ca/dev/example_interactive.php
public class MainActivity extends BaseActivity {

    Player player;
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

    public void loadTableFragment(boolean createTable) {
        player = new Player();
        player.playerPosition = 1;
        player.isDealer = true;
        if (!(getCurrentFragment() instanceof TableFragment)) {
            TableFragment newFragment = TableFragment.newInstance(player);
            replaceFragment(newFragment);
        }
    }


}
