package com.agames.thuruppugulan.domain

/**
 * 28 / Thuruppukali ranking: high to low J-9-A-10-K-Q-8-7.
 */
enum class Rank(val label: String, val points: Int) {
    JACK("J", 3),
    NINE("9", 2),
    ACE("A", 1),
    TEN("10", 1),
    KING("K", 0),
    QUEEN("Q", 0),
    EIGHT("8", 0),
    SEVEN("7", 0);

    companion object {
        /** Total points in a full 32-card deck. */
        const val DECK_POINT_TOTAL: Int = 28
        const val MIN_BID: Int = 14
        const val MAX_BID: Int = 28
    }
}
