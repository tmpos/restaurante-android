package com.tmrestaurant.ui.screens

import androidx.compose.runtime.Composable

@Composable
fun PosScreen(onNavigateToMesas: () -> Unit = {}) = com.tmrestaurant.ui.screens.pos.PosScreen(onNavigateToMesas = onNavigateToMesas)
