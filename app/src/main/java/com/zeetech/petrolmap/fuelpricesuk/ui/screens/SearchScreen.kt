package com.zeetech.petrolmap.fuelpricesuk.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeetech.petrolmap.fuelpricesuk.data.local.UKGeocodingHelper
import com.zeetech.petrolmap.fuelpricesuk.data.model.SearchLocation
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolBackground
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolBorder
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolBrandGreen
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolMutedText
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolPrimaryText
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolSecondaryText
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolSurface
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolSurfaceElevated
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.applePressEffect
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    initialQuery: String = "",
    recentSearches: List<SearchLocation>,
    onSelectLocation: (SearchLocation) -> Unit,
    onUseCurrentLocation: () -> Unit,
    onClearLocation: () -> Unit = {},
    onClearRecentSearches: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler {
        onBack()
    }

    var searchQuery by remember(initialQuery) { mutableStateOf(initialQuery) }
    var searchResults by remember { mutableStateOf<List<SearchLocation>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(searchQuery) {
        if (searchQuery.trim().length >= 2) {
            isSearching = true
            searchJob?.cancel()
            searchJob = scope.launch {
                delay(250) // debounce
                val res = UKGeocodingHelper.search(searchQuery)
                searchResults = res
                isSearching = false
            }
        } else {
            searchResults = emptyList()
            isSearching = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PetrolBackground)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(38.dp)
                    .background(PetrolSurfaceElevated, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PetrolPrimaryText,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Search location",
                    color = PetrolPrimaryText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "UK nationwide forecourts",
                    color = PetrolSecondaryText,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { newValue ->
                searchQuery = newValue
                if (newValue.trim().isEmpty()) {
                    onClearLocation()
                }
            },
            placeholder = { Text("Postcode, town or city", color = PetrolMutedText, fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = PetrolBrandGreen,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = {
                        searchQuery = ""
                        onClearLocation()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = PetrolSecondaryText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = PetrolBrandGreen,
                        strokeWidth = 2.dp
                    )
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp)),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = PetrolSurfaceElevated,
                unfocusedContainerColor = PetrolSurfaceElevated,
                focusedBorderColor = PetrolBrandGreen,
                unfocusedBorderColor = PetrolBorder,
                focusedTextColor = PetrolPrimaryText,
                unfocusedTextColor = PetrolPrimaryText,
                cursorColor = PetrolBrandGreen
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // "Use current location" button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(PetrolSurfaceElevated)
                .border(1.dp, PetrolBorder, RoundedCornerShape(14.dp))
                .applePressEffect(scaleOnPress = 0.98f, onClick = onUseCurrentLocation)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(PetrolBrandGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = null,
                    tint = PetrolBrandGreen,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Use current location",
                        color = PetrolPrimaryText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PetrolBrandGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Precise",
                            color = PetrolBrandGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "Detect GPS coordinates",
                    color = PetrolMutedText,
                    fontSize = 12.sp
                )
            }

            Icon(
                imageVector = Icons.Default.NorthEast,
                contentDescription = null,
                tint = PetrolBrandGreen,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Live Search Results
            if (searchResults.isNotEmpty()) {
                item {
                    Text(
                        text = "MATCHES FOR \"${searchQuery.uppercase()}\"",
                        color = PetrolBrandGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(searchResults) { loc ->
                    LocationResultRow(
                        location = loc,
                        isRecent = false,
                        onClick = { onSelectLocation(loc) }
                    )
                }
            } else if (searchQuery.length >= 2 && !isSearching) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Location not found. Try another search.",
                            color = PetrolSecondaryText,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Recent Searches Section
            if (searchQuery.isEmpty() && recentSearches.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RECENT SEARCHES",
                            color = PetrolMutedText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = onClearRecentSearches) {
                            Text(
                                text = "Clear all",
                                color = PetrolBrandGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                items(recentSearches) { loc ->
                    LocationResultRow(
                        location = loc,
                        isRecent = true,
                        onClick = { onSelectLocation(loc) }
                    )
                }
            }

            // Popular Hubs Section
            if (searchQuery.isEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "POPULAR MOTORWAYS & HUBS",
                        color = PetrolMutedText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UKGeocodingHelper.getPopularHubs().forEach { hub ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(PetrolSurfaceElevated)
                                    .border(1.dp, PetrolBorder, RoundedCornerShape(16.dp))
                                    .applePressEffect(scaleOnPress = 0.95f) { onSelectLocation(hub) }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = hub.name,
                                    color = PetrolPrimaryText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Coverage Info Box
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(PetrolSurface.copy(alpha = 0.7f))
                            .border(1.dp, PetrolBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = PetrolBrandGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "UK Fuel Pricing Coverage",
                                color = PetrolPrimaryText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Search any UK postcode, town, or city to find local petrol prices. Data is normalized and updated via verified forecourt feeds.",
                                color = PetrolMutedText,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun LocationResultRow(
    location: SearchLocation,
    isRecent: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PetrolSurfaceElevated)
            .border(1.dp, PetrolBorder, RoundedCornerShape(12.dp))
            .applePressEffect(scaleOnPress = 0.98f, onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isRecent) Icons.Default.History else Icons.Default.LocationOn,
            contentDescription = null,
            tint = if (isRecent) PetrolMutedText else PetrolBrandGreen,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = location.name,
                    color = PetrolPrimaryText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (!location.isUk) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PetrolBorder)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Outside UK",
                            color = PetrolSecondaryText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            if (location.subtitle.isNotEmpty()) {
                Text(
                    text = location.subtitle,
                    color = PetrolSecondaryText,
                    fontSize = 12.sp
                )
            }
        }

        Icon(
            imageVector = Icons.Default.NorthEast,
            contentDescription = null,
            tint = PetrolMutedText,
            modifier = Modifier.size(16.dp)
        )
    }
}
