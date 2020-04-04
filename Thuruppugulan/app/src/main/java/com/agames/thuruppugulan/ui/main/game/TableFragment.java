package com.agames.thuruppugulan.ui.main.game;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.agames.thuruppugulan.R;
import com.agames.thuruppugulan.base.BaseFragment;
import com.agames.thuruppugulan.databinding.TableFragmentBinding;
import com.agames.thuruppugulan.model.GameUser;
import com.agames.thuruppugulan.ui.main.GameState;
import com.agames.thuruppugulan.ui.main.utils.ViewUtils;
import com.agames.thuruppugulan.webrequest.WebSocketConnection;
import com.fevziomurtekin.customprogress.Type;
import com.orhanobut.logger.Logger;

import java.util.Objects;

import agency.tango.android.avatarview.IImageLoader;
import agency.tango.android.avatarview.loader.PicassoLoader;

public class TableFragment extends BaseFragment implements View.OnClickListener, ThuruppuKalli.OnGameListener {

    private IImageLoader imageLoader;
    private TableFragmentViewModel mViewModel;
    public static final String CREATE_TABLE = "create table";
    public static final String DEFAULT_PROFILE_PIC = "https://cdn3.iconfinder.com/data/icons/business-avatar-1/512/8_avatar-128.png";

    private Player me;
    private TableFragmentBinding binding;
    private ThuruppuKalli game;
    private boolean createTable;

    private TableFragment() {

   }

   public static TableFragment newInstance(Player me, boolean createTable) {
        TableFragment fragment = new TableFragment();
        fragment.me = me;
        fragment.createTable = createTable;
        return fragment;
   }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = TableFragmentBinding.inflate(getLayoutInflater());
        mRootView = binding.getRoot();

        imageLoader = new PicassoLoader();

        imageLoader.loadImage(binding.player1, DEFAULT_PROFILE_PIC, "1Player");
        imageLoader.loadImage(binding.player2, DEFAULT_PROFILE_PIC, "2Player");
        imageLoader.loadImage(binding.player3, DEFAULT_PROFILE_PIC, "3Player");
        imageLoader.loadImage(binding.player4, DEFAULT_PROFILE_PIC, "4Player");

        binding.shuffleButton.setOnClickListener(this);
        binding.drawCards.setOnClickListener(this);
        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(TableFragmentViewModel.class);
        mViewModel.me = me;
        if (game == null) {
            game = new ThuruppuKalli(binding, me.playerPosition, mViewModel, this);
        }
        binding.loadingLayout.setVisibility(View.VISIBLE);
        binding.progress.settype(Type.CUBE);
        binding.progress.show();
        if (createTable) {
            mViewModel.createTable = true;
            game.createTable();
        } else {
            game.joinTable();
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.shuffle_button:
                game.shuffleDeck();
                break;
            case R.id.draw_cards:
                binding.shuffleButton.setVisibility(View.GONE);
                game.drawCards();
                break;

        }
    }

    @Override
    public void onCreatedTable(String tableId) {
        Logger.d("onCreatedTable "+tableId);
        game.state = GameState.FRIENDS_JOINED;
        if (ThuruppuKalli.NO_SOCKET) {
            //Hard coding started
            mViewModel.players[1] = new Player();
            mViewModel.players[1].user = new GameUser();
            mViewModel.players[1].user.setUserName("Player 2");
            mViewModel.players[1].playerPosition = 1;
            mViewModel.players[2] = new Player();
            mViewModel.players[2].user = new GameUser();
            mViewModel.players[2].user.setUserName("Player 3");
            mViewModel.players[2].playerPosition = 1;
            mViewModel.players[3] = new Player();
            mViewModel.players[3].user = new GameUser();
            mViewModel.players[3].user.setUserName("Player 4");
            mViewModel.players[3].playerPosition = 1;
            //Hard coding end
        } else {
            Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    binding.loadingLayout.setVisibility(View.GONE);
                    if (me.isDealer) {
                        binding.shuffleDrawOptions.setVisibility(View.VISIBLE);
                    }
                }
            });

        }

    }

    @Override
    public void onJoinedTable(String tableId) {
        Logger.d("onJoinedTable " + tableId);
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                game.state = GameState.FRIENDS_JOINED;
                binding.progress.setVisibility(View.GONE);
                binding.loadingLayoutLoadingMessage.setText(String.format("Waiting for %sto Draw Cards",
                        mViewModel.getDealerPlayer().user.getUserName()));
            }
        });
    }

    @Override
    public void onGameCreationFailure(final Throwable throwable) {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.loadingLayout.setVisibility(View.GONE);
                ViewUtils.showToast(getContext(), "Error in Game creation");
                throwable.printStackTrace();
            }
        });
    }

    @Override
    public void onStop() {
        WebSocketConnection.getInstance().close();
        super.onStop();
    }
}
