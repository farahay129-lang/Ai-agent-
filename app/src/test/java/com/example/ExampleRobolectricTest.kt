package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.GeminiBrainService
import com.example.model.ActionType
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("JARVIS AI", appName)
    }

    @Test
    fun `test offline fallback voice command parsing for WhatsApp`() {
        val brain = GeminiBrainService()
        val action = brain.parseLocally("WhatsApp kholo")
        assertEquals(ActionType.WHATSAPP_OPEN, action.actionType)
        assertEquals("WhatsApp", action.platform)
    }

    @Test
    fun `test offline fallback voice command parsing for Screen Scroll`() {
        val brain = GeminiBrainService()
        val action = brain.parseLocally("Scroll down")
        assertEquals(ActionType.SCREEN_SCROLL_DOWN, action.actionType)
    }

    @Test
    fun `test offline fallback voice command parsing for Camera and Storage`() {
        val brain = GeminiBrainService()
        assertEquals(ActionType.CAMERA_OPEN, brain.parseLocally("Camera kholo").actionType)
        assertEquals(ActionType.STORAGE_INFO, brain.parseLocally("Storage check karo").actionType)
    }

    @Test
    fun `test offline message command extracts recipient`() {
        val brain = GeminiBrainService()
        val action = brain.parseLocally("Rahul ko message bhejo ki main aa raha hoon")
        assertEquals(ActionType.WHATSAPP_MESSAGE, action.actionType)
        assertEquals("Rahul", action.recipientName)
    }
}
