@file:OptIn(ExperimentalMaterialApi::class, ExperimentalMaterialApi::class)

package com.lydia

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lydia.medrem.ui.screens.reminders.RemindersListViewModel

@Composable
fun MetricsScreen(
    viewModel: RemindersListViewModel = hiltViewModel(),
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    
    val reminders by viewModel.reminders.collectAsState(initial = emptyList())
    val adherenceHistory by viewModel.adherenceHistory.collectAsState()
    val weeklyRate by viewModel.weeklyAdherenceRate.collectAsState()
    val monthlyRate by viewModel.monthlyAdherenceRate.collectAsState()
    val streak by viewModel.adherenceStreak.collectAsState()
    
    var feedback by remember { mutableStateOf("") }
    
    // Load feedback initially
    LaunchedEffect(key1 = true) {
        feedback = viewModel.generateFeedback()
    }
    
    // Update feedback when adherence data changes
    LaunchedEffect(key1 = adherenceHistory, key2 = streak) {
        feedback = viewModel.generateFeedback()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medication Analytics") },
                backgroundColor = MaterialTheme.colors.primary
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Adherence Overview Card
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Today's Adherence",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )
                    
                    val currentAdherence = adherenceHistory.lastOrNull()?.adherencePercentage ?: 0f
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AdherenceCircleProgress(
                            percentage = currentAdherence / 100f,
                            color = when {
                                currentAdherence >= 80f -> Color.Green
                                currentAdherence >= 60f -> Color.Blue
                                else -> Color.Red
                            }
                        )
                    }
                    
                    Text(
                        text = "${currentAdherence.toInt()}%",
                        style = MaterialTheme.typography.h4,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Adherence Metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AdherenceMetricBox(
                            title = "Weekly",
                            value = "${weeklyRate.toInt()}%",
                            color = when {
                                weeklyRate >= 80f -> Color.Green.copy(alpha = 0.15f)
                                weeklyRate >= 60f -> Color.Blue.copy(alpha = 0.15f)
                                else -> Color.Red.copy(alpha = 0.15f)
                            },
                            textColor = when {
                                weeklyRate >= 80f -> Color.Green
                                weeklyRate >= 60f -> Color.Blue
                                else -> Color.Red
                            }
                        )
                        
                        AdherenceMetricBox(
                            title = "Monthly",
                            value = "${monthlyRate.toInt()}%",
                            color = when {
                                monthlyRate >= 80f -> Color.Green.copy(alpha = 0.15f)
                                monthlyRate >= 60f -> Color.Blue.copy(alpha = 0.15f)
                                else -> Color.Red.copy(alpha = 0.15f)
                            },
                            textColor = when {
                                monthlyRate >= 80f -> Color.Green
                                monthlyRate >= 60f -> Color.Blue
                                else -> Color.Red
                            }
                        )
                    }
                }
            }
            
            // Streak Card
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                elevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Current Streak",
                            style = MaterialTheme.typography.subtitle1
                        )
                        Text(
                            text = "$streak days",
                            style = MaterialTheme.typography.h5,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Streak Icon",
                        modifier = Modifier.size(48.dp),
                        tint = if (streak > 0) Color.Green else Color.Gray
                    )
                }
            }
            
            // Personalized Feedback Card
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Feedback & Insights",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Divider()
                    
                    Text(
                        text = feedback,
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
            
            // Medication Summary
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Medication Summary",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Divider()
                    
                    val total = reminders.size
                    val completed = reminders.count { it.isDone }
                    val pending = total - completed
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MedicationMetric(
                            title = "Total",
                            value = total.toString(),
                            color = Color.Gray.copy(alpha = 0.2f)
                        )
                        
                        MedicationMetric(
                            title = "Completed",
                            value = completed.toString(),
                            color = Color.Green.copy(alpha = 0.2f)
                        )
                        
                        MedicationMetric(
                            title = "Pending",
                            value = pending.toString(),
                            color = Color.Red.copy(alpha = 0.2f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AdherenceCircleProgress(
    percentage: Float,
    color: Color
) {
    val sweepAngle = 360 * percentage
    
    Canvas(
        modifier = Modifier
            .size(180.dp)
            .padding(16.dp)
    ) {
        // Background circle
        drawArc(
            color = Color.LightGray.copy(alpha = 0.3f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = 24f, cap = StrokeCap.Round)
        )
        
        // Progress arc
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = 24f, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun AdherenceMetricBox(
    title: String,
    value: String,
    color: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontWeight = FontWeight.Medium,
                    color = textColor,
                    fontSize = 14.sp
                )
            )
            
            Text(
                text = value,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontSize = 22.sp
                )
            )
        }
    }
}

@Composable
fun MedicationMetric(
    title: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Text(
            text = value,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        )
        
        Text(
            text = title,
            style = TextStyle(
                fontSize = 12.sp
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MetricsScreen()
}