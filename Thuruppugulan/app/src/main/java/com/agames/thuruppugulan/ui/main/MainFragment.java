package com.agames.thuruppugulan.ui.main;

import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.agames.thuruppugulan.MainActivity;
import com.agames.thuruppugulan.R;
import com.agames.thuruppugulan.base.BaseFragment;


public class MainFragment extends BaseFragment implements View.OnClickListener {

    private MainViewModel mViewModel;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mRootView =  inflater.inflate(R.layout.main_fragment, container, false);
        TextView createTable = mRootView.findViewById(R.id.create_table);
        TextView joinTable = mRootView.findViewById(R.id.join_table);
        createTable.setOnClickListener(this);
        joinTable.setOnClickListener(this);
        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        // TODO: Use the ViewModel
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.create_table:
                ((MainActivity) getActivity()).loadTableFragment(true);
                break;
        }
    }
}
