package com.fsck.k9.ui.managefolders

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import com.fsck.k9.ui.R
import com.fsck.k9.ui.folders.FolderNameFormatter
import com.fsck.k9.ui.observeNotNull
import com.takisoft.preferencex.PreferenceFragmentCompat
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class FolderSettingsFragment : PreferenceFragmentCompat() {
    private val viewModel: FolderSettingsViewModel by viewModel()
    private val folderNameFormatter: FolderNameFormatter by inject { parametersOf(requireActivity()) }

    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        // Set empty preferences resource while data is being loaded
        setPreferencesFromResource(R.xml.empty_preferences, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val arguments = arguments ?: error("Arguments missing")
        val accountUuid = arguments.getString(EXTRA_ACCOUNT) ?: error("Missing argument '$EXTRA_ACCOUNT'")
        val folderId = arguments.getLong(EXTRA_FOLDER_ID)

        viewModel.getFolderSettingsLiveData(accountUuid, folderId)
            .observeNotNull(viewLifecycleOwner) { folderSettingsResult ->
                when (folderSettingsResult) {
                    is FolderNotFound -> navigateBack()
                    is FolderSettingsData -> initPreferences(folderSettingsResult)
                }
            }
    }

    private fun navigateBack() {
        findNavController().popBackStack()
    }

    private fun initPreferences(folderSettings: FolderSettingsData) {
        preferenceManager.preferenceDataStore = folderSettings.dataStore
        setPreferencesFromResource(R.xml.folder_settings_preferences, null)

        setCategoryTitle(folderSettings)
    }

    private fun setCategoryTitle(folderSettings: FolderSettingsData) {
        val folderDisplayName = folderNameFormatter.displayName(folderSettings.folder)
        findPreference<Preference>(PREFERENCE_TOP_CATEGORY)!!.title = folderDisplayName
    }

    companion object {
        const val EXTRA_ACCOUNT = "account"
        const val EXTRA_FOLDER_ID = "folderId"

        private const val PREFERENCE_TOP_CATEGORY = "folder_settings"
    }
}
