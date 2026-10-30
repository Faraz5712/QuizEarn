package com.quizearn.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.quizearn.app.navigation.QuizEarnNavGraph
import com.quizearn.app.ui.theme.QuizEarnTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuizEarnTheme {
                Surface {
                    QuizEarnNavGraph()
                }
            }
        }
    }
}
