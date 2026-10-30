package com.quizearn.app.ui.screens.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quizearn.app.data.repository.QuizRepositoryImpl
import com.quizearn.app.ui.components.EmptyStateView
import com.quizearn.app.ui.components.QuizEarnTextField
import com.quizearn.app.ui.screens.home.CategoryIconKey
import com.quizearn.app.ui.screens.home.QuizCategoryDisplay
import com.quizearn.app.ui.theme.CardShape
import com.quizearn.app.ui.theme.ElectricIndigo
import com.quizearn.app.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    onCategoryClick: (categoryId: String) -> Unit,
    onBack: () -> Unit,
    quizRepository: QuizRepositoryImpl = QuizRepositoryImpl.getInstance(),
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val allCategories = remember { quizRepository.getCategoryDisplays() }
    var searchQuery by remember { mutableStateOf("") }

    val filteredCategories = remember(searchQuery, allCategories) {
        if (searchQuery.isBlank()) {
            allCategories
        } else {
            allCategories.filter {
                it.name.contains(searchQuery.trim(), ignoreCase = true) ||
                it.difficulty.contains(searchQuery.trim(), ignoreCase = true)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Quiz Categories",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = spacing.md)
        ) {
            // Search Input Field
            QuizEarnTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Search Categories",
                placeholder = "Search (e.g. Science, Tech, Sports)...",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.padding(vertical = spacing.sm)
            )

            Spacer(modifier = Modifier.height(spacing.xs))

            if (filteredCategories.isEmpty()) {
                EmptyStateView(
                    title = "No Categories Found",
                    message = "No quiz category matches \"$searchQuery\". Try searching for another topic.",
                    actionButtonText = "Clear Search",
                    onActionClick = { searchQuery = "" }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredCategories, key = { it.id }) { category ->
                        CategoryListItem(
                            category = category,
                            onClick = { onCategoryClick(category.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(spacing.lg))
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryListItem(
    category: QuizCategoryDisplay,
    onClick: () -> Unit
) {
    val spacing = LocalSpacing.current
    val accentColor = remember(category.accentColorHex) {
        Color(android.graphics.Color.parseColor(category.accentColorHex))
    }

    val icon: ImageVector = when (category.iconKey) {
        CategoryIconKey.SCIENCE -> Icons.Default.Science
        CategoryIconKey.TECH -> Icons.Default.Code
        CategoryIconKey.HISTORY -> Icons.Default.Public
        CategoryIconKey.SPORTS -> Icons.Default.SportsSoccer
        CategoryIconKey.ARTS -> Icons.Default.Palette
        CategoryIconKey.GENERAL -> Icons.Default.HelpOutline
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .clickable(onClick = onClick),
        shape = CardShape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = category.name,
                        tint = accentColor,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = category.difficulty,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "${category.totalQuestions} Questions Available",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Surface(
                shape = CircleShape,
                color = ElectricIndigo.copy(alpha = 0.12f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start Quiz",
                        tint = ElectricIndigo,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
