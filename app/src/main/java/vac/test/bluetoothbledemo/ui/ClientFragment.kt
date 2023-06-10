package vac.test.bluetoothbledemo.ui

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import vac.test.bluetoothbledemo.State.ClientState
import vac.test.bluetoothbledemo.databinding.FragmentClientBinding
import vac.test.bluetoothbledemo.intent.ClientIntent
import vac.test.bluetoothbledemo.intent.ConnectIntent
import vac.test.bluetoothbledemo.vm.ClientViewModel
import vac.test.bluetoothbledemo.vm.ConnectViewModel

class ClientFragment : BaseFragment<FragmentClientBinding>() {

    companion object {
        fun newInstance() = ClientFragment()
    }

    private lateinit var scanFragment: ScanFragment
    private lateinit var connectFragment: ConnectFragment

    private lateinit var clientViewModel: ClientViewModel

    override val bindingInflater: (LayoutInflater, ViewGroup?, Bundle?) -> FragmentClientBinding
        get() = { layoutInflater, viewgroup, _ ->
            FragmentClientBinding.inflate(layoutInflater,viewgroup, false)
        }

    private var times=0


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        scanFragment = ScanFragment.newInstance()
        connectFragment = ConnectFragment.newInstance()

        childFragmentManager.beginTransaction()
            .replace(binding.clientfragment.id,scanFragment)
            .commit()

        clientViewModel = ViewModelProvider(requireActivity()).get(ClientViewModel::class.java)
        observeViewModel()
    }


    private fun observeViewModel() {
        lifecycleScope.launch {
            clientViewModel.clientState
                .flowWithLifecycle(lifecycle,Lifecycle.State.STARTED)
                .collect{
                    Log.i("pkg","clientfragment ${it}")
                    when(it){
                        is ClientState.ScanMode ->{
                            replaceFragment(scanFragment)
                        }
                        is ClientState.ConnectMode ->{
                            replaceFragment(connectFragment)
                        }
                        else ->{

                        }
                    }
                }
        }
    }

    private fun replaceFragment(fragment:Fragment){
        parentFragmentManager.beginTransaction()
            .replace(binding.clientfragment.id,fragment)
            .commit()
    }
}