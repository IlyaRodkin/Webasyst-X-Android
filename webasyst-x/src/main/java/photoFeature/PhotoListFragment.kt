package com.webasyst.x.photoFeature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.webasyst.x.databinding.FragPhotoListBinding
import com.webasyst.x.installations.Installation
import com.webasyst.x.main.MainFragment

class PhotoListFragment : Fragment() {
    private lateinit var binding: FragPhotoListBinding
    private val installation by lazy { arguments?.getSerializable(MainFragment.INSTALLATION) as Installation? }
    private val viewModel by lazy {
        ViewModelProvider(
            this,
            PhotoListViewModel.Factory(
                requireActivity().application,
                installation?.id,
                installation?.url,
            )
        ).get(PhotoListViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragPhotoListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = PhotoListAdapter(installation?.url?:"")
        binding.recyclerViewPhotos.adapter = adapter
        viewModel.photosList.observe(viewLifecycleOwner) { list -> adapter.submitList(list) }
    }
}
