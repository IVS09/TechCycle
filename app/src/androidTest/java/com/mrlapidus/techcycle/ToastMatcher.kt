package com.mrlapidus.techcycle.utils

import android.os.IBinder
import android.view.WindowManager
import androidx.test.espresso.Root
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class ToastMatcher : TypeSafeMatcher<Root>() {
    override fun describeTo(description: Description) {
        description.appendText("is toast or toast-like window")
    }

    override fun matchesSafely(root: Root): Boolean {
        val type = root.windowLayoutParams?.get()?.type
        if (type == WindowManager.LayoutParams.TYPE_TOAST ||
            type == WindowManager.LayoutParams.TYPE_APPLICATION ||
            type == WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        ) {
            val windowToken: IBinder = root.decorView.windowToken
            val appToken: IBinder = root.decorView.applicationWindowToken
            return windowToken === appToken
        }
        return false
    }
}
