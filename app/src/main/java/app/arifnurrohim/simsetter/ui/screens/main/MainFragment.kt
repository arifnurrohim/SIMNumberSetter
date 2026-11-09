package app.arifnurrohim.simsetter.ui.screens.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import app.arifnurrohim.simsetter.R
import app.arifnurrohim.simsetter.databinding.FragmentMainBinding
import app.arifnurrohim.simsetter.ui.screens.main.MainViewModel.*
import app.arifnurrohim.simsetter.utils.extensions.hideIme
import app.arifnurrohim.simsetter.utils.extensions.onChanged
import app.arifnurrohim.simsetter.utils.extensions.onClicked
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.NullPointerException

class MainFragment: Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding
        ?: throw NullPointerException("Unable to access binding before onViewCreated or after onDestroyView")

    private val viewModel by viewModel<MainViewModel>()

    private val bottomPadding by lazy {
        resources.getDimension(R.dimen.margin_16).toInt()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupInsets()
        setupState()
        setupToolbar()
        setupSaveButton()
        setupCloseButton()
        setupInput()
        setupSimSelector()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainToolbar) { view, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.updatePadding(top = topInset)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.includeMainLoaded.root) { view, insets ->
            val bottomInset = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            ).bottom
            view.updatePadding(bottom = bottomPadding + bottomInset)
            insets
        }
    }

    private fun setupToolbar() = with(binding.mainToolbar) {
        MenuInflater(requireContext()).inflate(R.menu.menu, menu)
        setOnMenuItemClickListener {
            viewModel.onMenuItemClicked(requireContext(), it.itemId)
            true
        }
    }

    private fun setupSaveButton() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        with(binding.includeMainLoaded.includeMainLoadedCardEdit.mainLoadedCardEditInputSave) {
            onClicked().collect {
                it.hideIme()
                viewModel.onSaveClicked()
            }
        }
    }

    private fun setupCloseButton() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        with(binding.includeMainError.mainErrorClose) {
            onClicked().collect {
                requireActivity().finish()
            }
        }
    }

    private fun setupInput() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        with(binding.includeMainLoaded.includeMainLoadedCardEdit.mainLoadedCardEditEdit){
            onChanged().collect {
                viewModel.onNumberChanged(it?.toString() ?: "")
            }
        }
    }

    private fun setupSimSelector() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        with(binding.includeMainLoaded.includeMainLoadedCardEdit.mainLoadedCardEditSim) {
            setOnItemClickListener { _, _, position, _ ->
                viewModel.onPhoneSelected(position)
            }
        }
    }

    private fun setupState() {
        handleState(viewModel.state.value)
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.state.collect {
                handleState(it)
            }
        }
    }

    private fun handleState(state: State) {
        binding.includeMainLoading.root.isVisible = state is State.Loading
        binding.includeMainLoaded.root.isVisible = state is State.Loaded
        binding.includeMainError.root.isVisible = state is State.Error
        when(state) {
            is State.Loading -> setupWithLoading(state.loadType)
            is State.Error -> setupWithError(state.errorType)
            is State.Loaded -> setupWithLoaded(state)
        }
    }

    private fun setupWithLoading(loadType: LoadType) = with(binding.includeMainLoading) {
        mainLoading.setText(loadType.contentRes)
    }

    private fun setupWithError(errorType: ErrorType) = with(binding.includeMainError) {
        mainErrorContent.setText(errorType.messageRes)
    }

    private fun setupWithLoaded(state: State.Loaded) = with(binding.includeMainLoaded) {
        val formattedNumber = state.number.ifEmpty {
            getString(R.string.main_loaded_card_edit_number_empty)
        }
        includeMainLoadedCardEdit.mainLoadedCardEditCurrent.text =
            getString(R.string.main_loaded_card_edit_content, formattedNumber)
        includeMainLoadedCardEdit.mainLoadedCardEditEdit.setText(
            state.number,
            TextView.BufferType.EDITABLE
        )
        setupSimSelector(state.phoneId, state.phoneCount)
    }

    private fun setupSimSelector(phoneId: Int, phoneCount: Int) =
        with(binding.includeMainLoaded.includeMainLoadedCardEdit) {
            mainLoadedCardEditSimInput.isVisible = phoneCount > 1
            if (phoneCount <= 1) return@with
            val labels = (1..phoneCount).map {
                getString(R.string.main_loaded_card_edit_sim_item, it)
            }
            mainLoadedCardEditSim.setAdapter(
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, labels)
            )
            mainLoadedCardEditSim.setText(labels[phoneId], false)
        }

}