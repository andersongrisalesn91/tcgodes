package com.example.tcgokotlin.ModulChat.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.tcgokotlin.ModulChat.ChatDataSource
import com.example.tcgokotlin.ModulChat.ChatRepository
import com.example.tcgokotlin.ModulChat.ChatViewModel
import com.example.tcgokotlin.ModulChat.ChatViewModelFactory
import com.example.tcgokotlin.ModulOptions.OptionsRepo
import com.example.tcgokotlin.ModulOptions.OptionsViewModel
import com.example.tcgokotlin.ModulOptions.OptionsViewModelFactory
import com.example.tcgokotlin.ModulOptions.remote.OptionsDataSource
import com.example.tcgokotlin.R

class ChatFragment : Fragment() {

    private val viewModelChat by viewModels<ChatViewModel> { ChatViewModelFactory(ChatRepository(ChatDataSource(requireContext()))) }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance() = ChatFragment()
    }
}