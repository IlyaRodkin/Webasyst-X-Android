package com.webasyst.x.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.webasyst.x.MainActivity
import com.webasyst.x.R
import com.webasyst.x.databinding.FragMainBinding
import com.webasyst.x.site.domainlist.DomainListFragment
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.frag_main.bottomNav

class MainFragment : Fragment() {
    private val args: MainFragmentArgs by navArgs()
    private val viewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(this).get(MainViewModel::class.java).also {
            it.installationSelected.postValue(args.installationId != null)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = DataBindingUtil.inflate<FragMainBinding>(
        inflater,
        R.layout.frag_main,
        container,
        false
    ).let { binding ->
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (args.installationId == null) {
            (requireActivity() as MainActivity).toolbar.setTitle(R.string.add_webasyst)
        }

        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.domains, R.id.x, R.id.y -> {
                    onTabChange(item.itemId)
                    true
                }
                else -> false
            }
        }

        onTabChange(bottomNav.selectedItemId)
    }

    private fun initDomainsFragment(): Fragment =
        DomainListFragment::class.java.newInstance().apply {
            arguments = Bundle().apply {
                putString(DomainListFragment.INSTALLATION_ID, args.installationId)
                putString(DomainListFragment.INSTALLATION_URL, args.installationUrl)

            }
        }

    private fun onTabChange(@IdRes id: Int) {
        val fragment = when(id) {
            R.id.domains -> initDomainsFragment()
            R.id.x -> ExampleFragment.newInstance("Hello X!")
            R.id.y -> ExampleFragment.newInstance("Hello Y!")
            else -> throw IllegalArgumentException("Tab not found")
        }
        loadFragment(fragment)
        if (args.installationId != null) {
            requireActivity().toolbar.title = bottomNav.menu.findItem(id).title
        }
    }

    private fun loadFragment(fragment: Fragment) {
        childFragmentManager
            .beginTransaction()
            .replace(R.id.tabContent, fragment)
            .commit()
    }
}
