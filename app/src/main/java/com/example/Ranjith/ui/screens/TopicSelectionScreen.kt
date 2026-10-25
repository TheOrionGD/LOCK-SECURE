package com.rithika.lock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rithika.lock.data.model.InterviewCategory
import com.rithika.lock.data.model.InterviewDifficulty
import com.rithika.lock.data.model.Topic
import com.rithika.lock.data.model.TopicData
import com.rithika.lock.ui.components.TopicCard

@Composable
fun TopicSelectionScreen(
    initialCategoryFilter: InterviewCategory? = null,
    onTopicSelected: (topicId: String, difficulty: InterviewDifficulty) -> Unit
) {
    var selectedTabIndex by remember {
        mutableIntStateOf(
            when (initialCategoryFilter) {
                InterviewCategory.TECHNICAL -> 1
                InterviewCategory.NON_TECHNICAL -> 2
                else -> 0
            }
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedTopicForStart by remember { mutableStateOf<Topic?>(null) }
    var selectedDifficulty by remember { mutableStateOf(InterviewDifficulty.FRESHER) }

    val allTopics = TopicData.topics

    val filteredTopics = allTopics.filter { topic ->
        val matchesCategory = when (selectedTabIndex) {
            1 -> topic.category == InterviewCategory.TECHNICAL
            2 -> topic.category == InterviewCategory.NON_TECHNICAL
            else -> true
        }
        val matchesSearch = topic.title.contains(searchQuery, ignoreCase = true) ||
                topic.description.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Select Interview Topic",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Choose a technical or non-technical area to practice",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search topics (Java, HR, OS, DBMS...)") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("All Topics", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("Technical", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTabIndex == 2,
                onClick = { selectedTabIndex = 2 },
                text = { Text("Non-Technical", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredTopics) { topic ->
                TopicCard(
                    topic = topic,
                    onClick = { selectedTopicForStart = topic }
                )
            }
        }
    }

    // Difficulty Selection Modal Dialog
    if (selectedTopicForStart != null) {
        val topic = selectedTopicForStart!!
        AlertDialog(
            onDismissRequest = { selectedTopicForStart = null },
            title = {
                Text(
                    text = "Start ${topic.title} Interview",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Choose Difficulty Level:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    InterviewDifficulty.entries.forEach { diff ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedDifficulty = diff }
                                .padding(vertical = 6.dp)
                        ) {
                            RadioButton(
                                selected = (selectedDifficulty == diff),
                                onClick = { selectedDifficulty = diff }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = diff.displayName,
                                fontSize = 14.sp,
                                fontWeight = if (selectedDifficulty == diff) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val topicId = topic.id
                        val diff = selectedDifficulty
                        selectedTopicForStart = null
                        onTopicSelected(topicId, diff)
                    }
                ) {
                    Text("Begin Interview")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { selectedTopicForStart = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
