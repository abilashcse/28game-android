package com.agames.thuruppugulan.ui.main.game.popup;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;


import com.agames.thuruppugulan.R;
import com.agames.thuruppugulan.databinding.ChooseBidDialogBinding;
import com.agames.thuruppugulan.ui.main.utils.ViewUtils;

public class ChooseBidDialog extends Dialog implements View.OnClickListener {

    private final Context ctx;
    private ChooseBidDialogBinding ui;
    public interface OnBidSelectionListener {
        void onBidSelected(int bid);
        void onThaniSelected();
        void onPassed();
    }

    private OnBidSelectionListener mListener;

    public ChooseBidDialog(Context ctx, OnBidSelectionListener listener) {
        super(ctx);
        this.ctx = ctx;
        this.mListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        ui = ChooseBidDialogBinding.inflate(getLayoutInflater());
        setContentView(ui.getRoot());
        Window window = this.getWindow();
        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);

    }

    public void showBidDialog() {
        this.setCancelable(false);
        this.show();
        Button[] bidButtons = {ui.bid14, ui.bid15, ui.bid16, ui.bid17, ui.bid18, ui.bid19, ui.bid20,
                ui.bid21, ui.bid22, ui.bid23, ui.bid24, ui.bid25, ui.bid26, ui.bid27, ui.bid28,
                ui.thani, ui.pass};
        for (int i=0; i< bidButtons.length; i++) {
            bidButtons[i].setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.thani:
                ViewUtils.showToast(ctx,"You selected Thani");
                mListener.onThaniSelected();
                dismiss();
                break;
            case R.id.pass:
                ViewUtils.showToast(ctx,"You selected to Pass");
                mListener.onPassed();
                dismiss();
                break;
            default:
                int selectedNumber = Integer.parseInt(((TextView)view).getText().toString().trim());
                ViewUtils.showToast(ctx,"You selected "+selectedNumber);
                mListener.onBidSelected(selectedNumber);
                dismiss();
        }
    }
}
