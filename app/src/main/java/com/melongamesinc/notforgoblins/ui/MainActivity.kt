package com.melongamesinc.notforgoblins.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.melongamesinc.notforgoblins.ui.theme.NotForGoblinsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotForGoblinsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "menu"
                    ) {
                        composable("menu") {
                            MainMenuScreen(
                                onStartGame = { navController.navigate("game") },
                                onOpenShop = { navController.navigate("shop") },
                                onExit = { finish() }
                            )
                        }

                        composable("game") {
                            GameScreen(
                                onBackToMenu = {
                                    navController.popBackStack("menu", inclusive = false)
                                }
                            )
                        }

                        composable("shop") {
                            ShopScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
