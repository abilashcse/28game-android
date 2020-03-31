package com.agames.thuruppugulan.ui.main.game;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.agames.thuruppugulan.R;
import com.agames.thuruppugulan.base.BaseFragment;
import com.agames.thuruppugulan.databinding.TableFragmentBinding;
import com.agames.thuruppugulan.ui.main.MainViewModel;
import com.agames.thuruppugulan.ui.main.game.props.Deck;

import agency.tango.android.avatarview.IImageLoader;
import agency.tango.android.avatarview.loader.PicassoLoader;

public class TableFragment extends BaseFragment implements View.OnClickListener {

    private IImageLoader imageLoader;
    private TableFragmentViewModel mViewModel;
    public static final String CREATE_TABLE = "create table";
    public static final String DEFAULT_PROFILE_PIC = "https://cdn3.iconfinder.com/data/icons/business-avatar-1/512/8_avatar-128.png";

    private Player me;
    private TableFragmentBinding binding;
    private ThuruppuKalli game;


    private TableFragment() {

   }

   public static TableFragment newInstance(Player me) {
        TableFragment fragment = new TableFragment();
        fragment.me = me;
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
        mViewModel.players[me.playerPosition] = me;
        if (game == null) {
            game = new ThuruppuKalli(binding, me.playerPosition, mViewModel);
        }

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.shuffle_button:
                game.shuffleDeck();
                break;
            case R.id.draw_cards:
                game.drawCards();
                break;

        }
    }
}
