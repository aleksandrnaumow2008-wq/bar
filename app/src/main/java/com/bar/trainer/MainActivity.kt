package com.bar.trainer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bar.trainer.data.ProgressRepository
import com.bar.trainer.ui.screens.MainScreen
import com.bar.trainer.ui.theme.BarTrainerTheme
import com.bar.trainer.viewmodel.TrainerViewModel
import com.bar.trainer.viewmodel.TrainerViewModelFactory
import com.bar.trainer.worker.ReminderWorker

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = ProgressRepository(applicationContext)
        ReminderWorker.scheduleDailyReminder(applicationContext)

        setContent {
            BarTrainerTheme {
                val trainerViewModel: TrainerViewModel = viewModel(
                    factory = TrainerViewModelFactory(repository)
                )
                MainScreen(viewModel = trainerViewModel)
            }
        }
    }
}
