package com.agames.thuruppugulan.ui.main.game;


import com.agames.thuruppugulan.databinding.TableFragmentBinding;

public class ThuruppuKalli {
    private TableFragmentViewModel viewModel;

    private final TableFragmentBinding ui;

    // Position goes like
    // 1 - 2 - 3 - 4
    // 2 - 3 - 4 - 1
    // 3 - 4 - 1 - 2
    // 4 - 1 - 2 - 3
    public int myPosition;


    public ThuruppuKalli(TableFragmentBinding ui, int myPosition, TableFragmentViewModel viewModel) {
        this.ui = ui;
        this.myPosition = myPosition;
        this.viewModel = viewModel;
    }

    public void drawFirstSet() {

        if (viewModel.players[myPosition].isDealer) {

        }
    }
    public void shuffleDeck() {
        viewModel.shuffleDeck();
    }

    public void drawCards() {
        viewModel.drawCards();
    }
}
