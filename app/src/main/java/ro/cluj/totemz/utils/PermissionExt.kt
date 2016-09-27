package ro.cluj.totemz.utils

import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat

/**
 * Invokes the given lambda if the application has access to given permission.
 * any errors with methods that requires this permission.
 */
fun Activity.withPermissionGranted(permission:String, lambda: () -> Unit): Unit {
    val permissionCheck = ContextCompat.checkSelfPermission(this, permission)
    when (permissionCheck) {
        PackageManager.PERMISSION_GRANTED -> lambda.invoke()
    }
}

