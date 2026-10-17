package com.agames.thuruppugulan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agames.thuruppugulan.ui.theme.TableColors

@Composable
fun LobbyScreen(
    onPlayOffline: (String) -> Unit,
    onHostOnline: (String) -> Unit,
    onJoinOnline: (String, String) -> Unit,
) {
    var name by remember { mutableStateOf("Player") }
    var tableId by remember { mutableStateOf("") }
    var showJoin by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TableColors.feltDeep),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 48.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Thuruppugulan",
                color = TableColors.gold,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "28 · Lelam · Compose Multiplatform",
                color = TableColors.cream.copy(alpha = 0.75f),
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(36.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(20) },
                label = { Text("Your name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors(),
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onPlayOffline(name.ifBlank { "You" }) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TableColors.gold,
                    contentColor = TableColors.feltDeep,
                ),
            ) {
                Text("Play 4-player (you + 3 bots)", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onHostOnline(name.ifBlank { "Host" }) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TableColors.felt,
                    contentColor = TableColors.cream,
                ),
            ) {
                Text("Host online table")
            }

            TextButton(onClick = { showJoin = !showJoin }) {
                Text(
                    if (showJoin) "Hide join" else "Join with table ID",
                    color = TableColors.cream,
                )
            }

            if (showJoin) {
                OutlinedTextField(
                    value = tableId,
                    onValueChange = { tableId = it.filter { ch -> ch.isDigit() }.take(4) },
                    label = { Text("Table ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (tableId.length == 4) {
                            onJoinOnline(name.ifBlank { "Guest" }, tableId)
                        }
                    },
                    enabled = tableId.length == 4,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TableColors.gold,
                        contentColor = TableColors.feltDeep,
                    ),
                ) {
                    Text("Join table")
                }
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TableColors.cream,
    unfocusedTextColor = TableColors.cream,
    focusedBorderColor = TableColors.gold,
    unfocusedBorderColor = TableColors.cream.copy(alpha = 0.4f),
    focusedLabelColor = TableColors.gold,
    unfocusedLabelColor = TableColors.cream.copy(alpha = 0.7f),
    cursorColor = TableColors.gold,
)
