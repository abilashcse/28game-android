package com.agames.thuruppugulan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agames.thuruppugulan.domain.Card
import com.agames.thuruppugulan.ui.theme.TableColors

@Composable
fun PlayingCard(
    card: Card,
    modifier: Modifier = Modifier,
    width: Dp = 56.dp,
    height: Dp = 84.dp,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val suitColor = if (card.suit.isRed) TableColors.redSuit else TableColors.blackSuit
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .size(width, height)
            .shadow(if (selected) 8.dp else 2.dp, shape)
            .clip(shape)
            .background(TableColors.cardFace)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) TableColors.gold else TableColors.cream.copy(alpha = 0.4f),
                shape = shape,
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(enabled = enabled, onClick = onClick)
                } else {
                    Modifier
                },
            )
            .padding(6.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = card.rank.label,
                color = suitColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
            Text(
                text = card.suit.symbol,
                color = suitColor,
                fontSize = 16.sp,
            )
        }
        Text(
            text = card.suit.symbol,
            color = suitColor.copy(alpha = 0.9f),
            fontSize = 28.sp,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
fun CardBack(
    modifier: Modifier = Modifier,
    width: Dp = 56.dp,
    height: Dp = 84.dp,
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .size(width, height)
            .clip(shape)
            .background(TableColors.gold.copy(alpha = 0.35f))
            .border(1.dp, TableColors.gold, shape),
        contentAlignment = Alignment.Center,
    ) {
        Text("♠", color = TableColors.cream, fontSize = 22.sp)
    }
}
