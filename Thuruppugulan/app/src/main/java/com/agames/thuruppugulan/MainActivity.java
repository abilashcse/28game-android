package com.agames.thuruppugulan;

import android.os.Bundle;

import com.agames.thuruppugulan.base.BaseActivity;
import com.agames.thuruppugulan.model.GameUser;
import com.agames.thuruppugulan.ui.main.MainFragment;
import com.agames.thuruppugulan.ui.main.game.Player;
import com.agames.thuruppugulan.ui.main.game.TableFragment;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.util.Random;

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
        Logger.addLogAdapter(new AndroidLogAdapter());
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
            player.user = new GameUser();
            player.user.setUserName("Player 1");
            player.playerPosition = 0;
            player.isDealer = true;
        } else {
            player.user = new GameUser();
            int x = new Random().nextInt(99);
            player.user.setUserName("Player "+x);
        }
        if (!(getCurrentFragment() instanceof TableFragment)) {
            TableFragment newFragment = TableFragment.newInstance(player, createTable);
            replaceFragment(newFragment);
        }
    }


}
