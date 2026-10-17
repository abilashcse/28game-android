package com.agames.thuruppugulan.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agames.thuruppugulan.ui.theme.TableColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BidSheet(
    options: List<Int>,
    canPass: Boolean,
    onBid: (Int) -> Unit,
    onPass: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = TableColors.felt,
        contentColor = TableColors.cream,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Your bid", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                options.forEach { bid ->
                    Button(
                        onClick = { onBid(bid) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TableColors.gold,
                            contentColor = TableColors.feltDeep,
                        ),
                    ) {
                        Text("$bid")
                    }
                }
            }
            if (canPass) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onPass) {
                    Text("Pass", color = TableColors.cream)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
