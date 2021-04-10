package com.example.tcgokotlin.ModulOptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class OptionsViewModelFactory(private val optionsRepo: OptionsInterfaces.OptionsRepoI): ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(OptionsInterfaces.OptionsRepoI::class.java).newInstance(optionsRepo)
    }
}