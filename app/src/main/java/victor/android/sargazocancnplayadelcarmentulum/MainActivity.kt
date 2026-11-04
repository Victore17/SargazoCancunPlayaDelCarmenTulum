package victor.android.sargazocancnplayadelcarmentulum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import victor.android.sargazocancnplayadelcarmentulum.ui.SargazoAppContent
import victor.android.sargazocancnplayadelcarmentulum.ui.theme.SargazoCancúnPlayaDelCarmenTulumTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SargazoCancúnPlayaDelCarmenTulumTheme {
                SargazoAppContent()
            }
        }
    }
}
