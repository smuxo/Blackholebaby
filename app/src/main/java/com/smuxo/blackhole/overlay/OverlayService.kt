package com.smuxo.blackhole.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.smuxo.blackhole.character.Character
import com.smuxo.blackhole.character.DyrAnimation
import com.smuxo.blackhole.character.SpriteAnimator
import com.smuxo.blackhole.network.ApiClient
import com.smuxo.blackhole.network.ConfigLoader
import com.smuxo.blackhole.skills.SkillRouter
import com.smuxo.blackhole.ui.CharacterContent
import com.smuxo.blackhole.ui.InputBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var characterView: ComposeView
    private lateinit var characterParams: WindowManager.LayoutParams
    private var inputView: ComposeView? = null
    private lateinit var inputParams: WindowManager.LayoutParams
    private lateinit var owner: OverlayLifecycleOwner
    private lateinit var character: Character
    private lateinit var animator: SpriteAnimator
    private var apiClient: ApiClient? = null
    private lateinit var skillRouter: SkillRouter
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    private var frameSizePx: Int = 0
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startAsForeground()

        owner = OverlayLifecycleOwner()
        owner.performRestore()
        owner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        owner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        owner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val metrics = resources.displayMetrics
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
        frameSizePx = (72f * metrics.density).toInt()

        animator = SpriteAnimator(this)
        character = Character()
        character.setPosition(
            screenWidth / 2f - frameSizePx / 2f,
            screenHeight / 2f - frameSizePx / 2f
        )

        val config = ConfigLoader.load(this)
        val modelConfig = config.models.firstOrNull()
        skillRouter = SkillRouter(this)
        if (modelConfig != null) {
            apiClient = ApiClient(modelConfig, screenWidth, screenHeight)
        }

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        characterParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        characterParams.gravity = Gravity.TOP or Gravity.START
        characterParams.x = character.x.toInt()
        characterParams.y = character.y.toInt()

        characterView = ComposeView(this)
        characterView.setViewTreeLifecycleOwner(owner)
        characterView.setViewTreeViewModelStoreOwner(owner)
        characterView.setViewTreeSavedStateRegistryOwner(owner)
        characterView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        characterView.setContent {
            CharacterContent(
                character = character,
                animator = animator,
                onTap = { toggleInput() }
            )
        }

        windowManager.addView(characterView, characterParams)

        character.onPositionChanged = { nx, ny ->
            characterParams.x = nx.toInt()
            characterParams.y = ny.toInt()
            if (::windowManager.isInitialized && ::characterView.isInitialized) {
                windowManager.updateViewLayout(characterView, characterParams)
            }
        }

        inputParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        inputParams.gravity = Gravity.BOTTOM
        inputParams.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE

        scope.launch {
            while (isActive) {
                character.step()
                delay(16L)
            }
        }
    }

    private fun toggleInput() {
        val existing = inputView
        if (existing != null) {
            windowManager.removeView(existing)
            inputView = null
            return
        }
        val view = ComposeView(this)
        view.setViewTreeLifecycleOwner(owner)
        view.setViewTreeViewModelStoreOwner(owner)
        view.setViewTreeSavedStateRegistryOwner(owner)
        view.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        view.setContent {
            InputBar(onSend = { text ->
                handleUserInput(text)
                toggleInput()
            })
        }
        inputView = view
        windowManager.addView(view, inputParams)
    }

    private fun handleUserInput(text: String) {
        val client = apiClient ?: return
        character.playAnimation(DyrAnimation.ACTION)
        scope.launch {
            val response = client.send(text)
            if (response != null) {
                character.say(response.say)
                character.playAnimation(DyrAnimation.fromKey(response.animation))
                if (response.moveToX != null && response.moveToY != null) {
                    val clampedX = response.moveToX.coerceIn(0f, (screenWidth - frameSizePx).toFloat())
                    val clampedY = response.moveToY.coerceIn(0f, (screenHeight - frameSizePx).toFloat())
                    character.moveTo(clampedX, clampedY)
                }
                skillRouter.execute(response.action, response.value)
            } else {
                character.playAnimation(DyrAnimation.SURPRISE)
                character.say("Что-то сломалось")
            }
        }
    }

    private fun startAsForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "dyr_overlay_channel"
            val channel = NotificationChannel(
                channelId,
                "Дыр на экране",
                NotificationManager.IMPORTANCE_MIN
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
            val notification = Notification.Builder(this, channelId)
                .setContentTitle("Дыр")
                .setContentText("Живёт на экране")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build()
            startForeground(1, notification)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        owner.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        owner.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        owner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        val existingInput = inputView
        if (existingInput != null && ::windowManager.isInitialized) {
            windowManager.removeView(existingInput)
        }
        if (::windowManager.isInitialized && ::characterView.isInitialized) {
            windowManager.removeView(characterView)
        }
    }
}
