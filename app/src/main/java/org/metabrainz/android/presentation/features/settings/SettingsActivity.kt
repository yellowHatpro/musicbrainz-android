package org.metabrainz.android.presentation.features.settings

import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import org.metabrainz.android.App
import org.metabrainz.android.R
import org.metabrainz.android.databinding.ActivityPreferencesBinding
import org.metabrainz.android.presentation.UserPreferences.PREFERENCE_LISTENING_ENABLED
import org.metabrainz.android.presentation.UserPreferences.preferenceListeningEnabled
import java.util.*

class SettingsActivity : AppCompatActivity() {

    var preferenceChangeListener: Preference.OnPreferenceChangeListener? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityPreferencesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar!!.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.app_bg)))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1){
            ACTION_NOTIFICATION_LISTENER_SETTINGS = Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
        }

        preferenceChangeListener = Preference.OnPreferenceChangeListener { preference: Preference, newValue: Any ->
            if (preference.key == PREFERENCE_LISTENING_ENABLED) {
                val enabled = newValue as Boolean
                if (enabled && !App.context!!.isNotificationServiceAllowed) {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Grant Media Control Permissions")
                    builder.setMessage("The listen service requires the special Notification " +
                            "Listener Service Permission to run. Please grant this permission to" +
                            " MusicBrainz for Android if you want to use the service.")
                    builder.setPositiveButton("Proceed") { dialog: DialogInterface?, which: Int -> startActivity(Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
                    builder.setNegativeButton("Cancel") { dialog: DialogInterface?, which: Int ->
                        preferenceListeningEnabled = false
                        (preference as SwitchPreference).isChecked = false
                    }
                    builder.create().show()
                } else if (!enabled) App.context!!.stopListenService()
                return@OnPreferenceChangeListener true
            }
            false
        }
    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)
        if (fragment is SettingsFragment) {
            fragment.setPreferenceChangeListener(preferenceChangeListener)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private var ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
    }
}