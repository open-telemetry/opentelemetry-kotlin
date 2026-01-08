package io.opentelemetry.kotlin.telescope

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun TelescopeShopScreen(navController: NavController) {
    val telescopes = listOf(
        Telescope("Orion SkyQuest", painterResource(id = R.drawable.telescope), 299.99),
        Telescope("Celestron NexStar", painterResource(id = R.drawable.telescope), 499.99),
        Telescope("Pirate monocular", painterResource(id = R.drawable.telescope), 99.99),
        Telescope("Meade LX90", painterResource(id = R.drawable.telescope), 799.99)
    )

    var cart by remember { mutableStateOf(setOf<Telescope>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text("Telescope Shop", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(telescopes) { telescope ->
                TelescopeItem(
                    telescope,
                    cart.contains(telescope)
                ) {
                    cart = if (cart.contains(telescope))
                        cart - telescope
                    else
                        cart + telescope
                }
            }
        }

        val subtotal = cart.sumOf { it.price }

        Button(
            onClick = {
                navController.navigate("confirmation")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            enabled = cart.isNotEmpty()
        ) {
            Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Checkout (${cart.size}) - $${"%.2f".format(subtotal)}")
        }
    }
}

@Composable
fun TelescopeItem(telescope: Telescope, isInCart: Boolean, onToggleCart: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = telescope.image,
            contentDescription = null,
            modifier = Modifier
                .weight(0.2f)
                .aspectRatio(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(0.45f)) {
            Text(text = telescope.name, style = MaterialTheme.typography.bodyLarge)
            Text(text = "$${telescope.price}", style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = onToggleCart, modifier = Modifier.weight(0.35f)) {
            Text(if (isInCart) "Remove" else "Add")
        }
    }
}

data class Telescope(val name: String, val image: Painter, val price: Double)
