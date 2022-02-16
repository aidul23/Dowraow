package com.aidul23.dowraow.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.aidul23.dowraow.R
import com.aidul23.dowraow.constants.Constant.KEY_NAME
import com.aidul23.dowraow.constants.Constant.KEY_WEIGHT
import com.aidul23.dowraow.databinding.FragmentSettingsBinding
import com.aidul23.dowraow.databinding.FragmentSetupBinding
import com.aidul23.dowraow.viewmodels.MainViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadFieldsFromSharedPref()
        
        binding.btnApplyChanges.setOnClickListener {
            val success = applyChangesToSharedPref()
            if(success) {
                Snackbar.make(view, "Saved Changes",Snackbar.LENGTH_LONG).show()
            } else {
                Snackbar.make(view, "Please fill all the field",Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun loadFieldsFromSharedPref() {
        val name = sharedPreferences.getString(KEY_NAME,"Dev")
        val weight = sharedPreferences.getFloat(KEY_WEIGHT, 80f)

        binding.etName.setText(name)
        binding.etWeight.setText(weight.toString())

    }

    private fun applyChangesToSharedPref(): Boolean {
        val nameText = binding.etName.text.toString()
        val weightText = binding.etWeight.text.toString()

        if(nameText.isEmpty() || weightText.isEmpty()) {
            return false
        }

        sharedPreferences.edit()
            .putString(KEY_NAME,nameText)
            .putFloat(KEY_WEIGHT,weightText.toFloat())
            .apply()
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}