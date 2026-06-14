package com.issueissyu.fe

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.issueissyu.fe.core.notification.PushDestination
import com.issueissyu.fe.core.notification.clearPushExtras
import com.issueissyu.fe.core.notification.parsePushDestination
import com.issueissyu.fe.core.notification.readPushAlarmId
import com.issueissyu.fe.domain.repository.AlarmRepository
import com.issueissyu.fe.ui.App
import com.issueissyu.fe.ui.theme.IssueissyuTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var alarmRepository: AlarmRepository

    private var pendingPush by mutableStateOf<PushDestination?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handlePushIntent(intent)
        setContent {
            IssueissyuTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    App(
                        pendingPush = pendingPush,
                        onPendingPushHandled = { pendingPush = null },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handlePushIntent(intent)
    }

    private fun handlePushIntent(intent: Intent?) {
        if (intent == null) return
        intent.parsePushDestination()?.let { pendingPush = it }
        intent.readPushAlarmId()?.let { alarmId ->
            lifecycleScope.launch {
                alarmRepository.confirmAlarm(alarmId).onFailure { error ->
                    val message = error.message?.takeIf { it.isNotBlank() }
                        ?: "알림 확인에 실패했습니다."
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        intent.clearPushExtras()
    }
}