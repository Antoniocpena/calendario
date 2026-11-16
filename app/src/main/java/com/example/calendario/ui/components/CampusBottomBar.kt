package com.example.calendario.ui.components

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.calendario.ui.theme.CampusLightBlue
import com.example.calendario.ui.theme.CampusSurface

enum class CampusTab(val route: String, val label: String, val symbol: CampusSymbol) {
    SCHEDULE("schedule", "Horario", CampusSymbol.CALENDAR),
    MAP("map", "Mapa", CampusSymbol.MAP),
    ALERTS("alerts", "Alertas", CampusSymbol.ALERTS),
    PROFILE("profile", "Perfil", CampusSymbol.PROFILE)
}

@Composable
fun CampusBottomBar(selectedRoute: String?, onSelectTab: (CampusTab) -> Unit, modifier: Modifier = Modifier) {
    NavigationBar(modifier = modifier, containerColor = CampusSurface) {
        CampusTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selectedRoute == tab.route,
                onClick = { onSelectTab(tab) },
                icon = { CampusIcon(tab.symbol, null) },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(indicatorColor = CampusLightBlue)
            )
        }
    }
}
