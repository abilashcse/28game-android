package com.agames.thuruppugulan;

import android.os.Bundle;

import com.agames.thuruppugulan.base.BaseActivity;
import com.agames.thuruppugulan.ui.main.MainFragment;
import com.agames.thuruppugulan.ui.main.game.Player;
import com.agames.thuruppugulan.ui.main.game.TableFragment;

//http://achex.ca/dev/example_interactive.php
//thuruppukali-stage@00001023
// dnr2s35#13443%242nasfiyugkb
//http://achex.ca/dev/general_testing.html
// stage: wss://cloud.achex.ca/00001023
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
        if (createTable) {
            player.playerPosition = 1;
            player.isDealer = true;
        }
        if (!(getCurrentFragment() instanceof TableFragment)) {
            TableFragment newFragment = TableFragment.newInstance(player);
            replaceFragment(newFragment);
        }
    }


}
